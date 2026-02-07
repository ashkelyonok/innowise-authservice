package org.ashkelyonok.authservice.service;

import lombok.RequiredArgsConstructor;
import org.ashkelyonok.authservice.model.entity.UserCredential;
import org.ashkelyonok.authservice.repository.UserCredentialRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserCredential credential = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(credential.getUsername())
                .password(credential.getPassword())
                .roles(credential.getRole().name())
                .build();
    }
}
