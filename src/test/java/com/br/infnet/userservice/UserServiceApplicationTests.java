package com.br.infnet.userservice;

import com.br.infnet.userservice.config.TestConfig;
import com.br.infnet.userservice.config.TestSecurityConfig;
import com.br.infnet.userservice.storage.S3Service;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
@Import({TestSecurityConfig.class, TestConfig.class})

class UserServiceApplicationTests {

    @MockitoBean
    private Keycloak keycloak;

    @MockitoBean
    private S3Service s3Service;

    @Test
    void contextLoads() {
    }

}
