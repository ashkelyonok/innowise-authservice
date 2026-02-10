package org.ashkelyonok.authservice.client;

import org.ashkelyonok.authservice.model.dto.request.RegisterRequestDto;
import org.ashkelyonok.authservice.model.dto.response.UserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client for communicating with the User Service.
 * Acts as a proxy to make HTTP calls looks like Java method calls.
 */
@FeignClient(name = "user-service", url = "${application.config.user-service-url}")
public interface UserServiceClient {

    /**
     * Calls POST /api/v1/users in User Service to create the profile.
     * Expects the created User ID in return.
     *
     * @param request The full registration request (Name, Surname, Email...)
     * @return The ID of the newly created user
     */
    @PostMapping("/api/v1/users")
    UserResponseDto createUser(@RequestBody RegisterRequestDto request);
}
