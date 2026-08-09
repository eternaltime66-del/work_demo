package org.wx.core.wxBase.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebConfigCorsTests {

    @Test
    void localDevelopmentOriginsIncludeIpv4HostnameAndIpv6Loopback() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://[::1]:*"
        ));

        assertThat(cors.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(cors.checkOrigin("http://127.0.0.1:5173")).isEqualTo("http://127.0.0.1:5173");
        assertThat(cors.checkOrigin("http://[::1]:5173")).isEqualTo("http://[::1]:5173");
        assertThat(cors.checkOrigin("https://example.com")).isNull();
    }

    @Test
    void fileProtocolCanBeAllowedExplicitlyForLocalDevelopment() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of("null"));
        cors.setAllowCredentials(true);

        assertThat(cors.checkOrigin("null")).isEqualTo("null");
        assertThat(cors.getAllowCredentials()).isTrue();
    }
}
