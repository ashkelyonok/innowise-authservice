package org.ashkelyonok.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.ashkelyonok.authservice.AbstractIntegrationTest;
import org.ashkelyonok.authservice.model.dto.request.AuthRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RefreshTokenRequestDto;
import org.ashkelyonok.authservice.model.dto.request.RegisterRequestDto;
import org.ashkelyonok.authservice.model.dto.request.TokenValidationRequestDto;
import org.ashkelyonok.authservice.model.dto.response.AuthResponseDto;
import org.ashkelyonok.authservice.model.dto.response.UserResponseDto;
import org.ashkelyonok.authservice.model.enums.Role;
import org.ashkelyonok.authservice.repository.RefreshTokenRepository;
import org.ashkelyonok.authservice.repository.UserCredentialRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserCredentialRepository credentialRepository;

    @Autowired
    private RefreshTokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureWireMockProperties(DynamicPropertyRegistry registry) {
        registry.add("application.config.user-service-url", () -> wireMockServer.baseUrl());
    }

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        credentialRepository.deleteAll();
        wireMockServer.resetAll();
    }

    private RegisterRequestDto createRegisterRequest() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setName("John");
        request.setSurname("Doe");
        request.setUsername("john_doe");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setEmail("john.doe@example.com");
        request.setPassword("StrongPass123!");
        request.setRole(Role.USER);
        return request;
    }

    private void registerUserHelper() throws Exception {
        RegisterRequestDto request = createRegisterRequest();

        UserResponseDto mockUserResponse = new UserResponseDto();
        mockUserResponse.setId(100L);
        mockUserResponse.setEmail(request.getEmail());

        wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/users"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                        .withBody(objectMapper.writeValueAsString(mockUserResponse))));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Thread.sleep(1000);
    }

    @Test
    @DisplayName("Should Register User: Calls UserService -> Saves to DB -> Returns Tokens")
    void register_Success() throws Exception {
        RegisterRequestDto request = createRegisterRequest();

        UserResponseDto mockUserResponse = new UserResponseDto();
        mockUserResponse.setId(100L);
        mockUserResponse.setEmail(request.getEmail());

        wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/users"))
                .withHeader("Content-Type", equalTo("application/json"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                        .withBody(objectMapper.writeValueAsString(mockUserResponse))));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        assertThat(credentialRepository.findByEmail(request.getEmail())).isPresent();
    }

    @Test
    @DisplayName("Should Fail Registration: If User Service returns 400")
    void register_Fail_UserServiceError() throws Exception {
        RegisterRequestDto request = createRegisterRequest();

        wireMockServer.stubFor(WireMock.post(urlEqualTo("/api/v1/users"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Validation Error in User Service")));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should Login Successfully")
    void login_Success() throws Exception {
        registerUserHelper();

        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Should Fail Login: User Not Found")
    void login_Fail_UserNotFound() throws Exception {
        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setUsername("ghost");
        loginRequest.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should Refresh Token Successfully")
    void refreshToken_Success() throws Exception {
        registerUserHelper();

        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("StrongPass123!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponseDto.class);

        Thread.sleep(1000);

        RefreshTokenRequestDto refreshReq = new RefreshTokenRequestDto();
        refreshReq.setRefreshToken(response.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Should Fail Refresh: Invalid Token Format")
    void refreshToken_Fail_InvalidFormat() throws Exception {
        RefreshTokenRequestDto refreshReq = new RefreshTokenRequestDto();
        refreshReq.setRefreshToken("invalid.token.format.here");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should Validate Token Successfully")
    void validateToken_Success() throws Exception {
        registerUserHelper();

        AuthRequestDto loginRequest = new AuthRequestDto();
        loginRequest.setUsername("john_doe");
        loginRequest.setPassword("StrongPass123!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponseDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AuthResponseDto.class);

        TokenValidationRequestDto validateReq = new TokenValidationRequestDto();
        validateReq.setToken(response.getAccessToken());

        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(true)))
                .andExpect(jsonPath("$.userId", is(100)))
                .andExpect(jsonPath("$.role", is("ROLE_USER")));
    }

    @Test
    @DisplayName("Should Validate Token: Return False for Invalid Token")
    void validateToken_Invalid() throws Exception {
        TokenValidationRequestDto validateReq = new TokenValidationRequestDto();
        validateReq.setToken("invalid.jwt.token");

        mockMvc.perform(post("/api/v1/auth/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid", is(false)))
                .andExpect(jsonPath("$.errorMessage").exists());
    }
}