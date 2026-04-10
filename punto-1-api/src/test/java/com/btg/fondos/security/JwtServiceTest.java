package com.btg.fondos.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.btg.fondos.config.JwtProperties;
import com.btg.fondos.domain.UserRole;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void createAndParseToken() {
        JwtProperties props = new JwtProperties();
        props.setSecret("dev-secret-must-be-at-least-32-bytes-long!!");
        props.setExpirationMinutes(60);
        JwtService jwtService = new JwtService(props);

        String token = jwtService.createAccessToken("cliente@btg.com", UserRole.client);

        assertThat(jwtService.parseToken(token)).isPresent();
        assertThat(jwtService.parseToken(token).get().getSubject()).isEqualTo("cliente@btg.com");
        assertThat(jwtService.parseToken(token).get().get("role", String.class)).isEqualTo("client");
    }

    @Test
    void invalidTokenReturnsEmpty() {
        JwtProperties props = new JwtProperties();
        props.setSecret("dev-secret-must-be-at-least-32-bytes-long!!");
        JwtService jwtService = new JwtService(props);

        assertThat(jwtService.parseToken("not-a-jwt")).isEmpty();
    }
}
