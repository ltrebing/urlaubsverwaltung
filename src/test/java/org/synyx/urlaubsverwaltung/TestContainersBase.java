package org.synyx.urlaubsverwaltung;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MariaDBContainer;

import static org.testcontainers.containers.MariaDBContainer.IMAGE;

@DirtiesContext
public abstract class TestContainersBase {

    static MariaDBContainer<?> mariaDB = new MariaDBContainer<>(IMAGE + ":10.4");
    static KeycloakContainer keycloak = new KeycloakContainer();

    @DynamicPropertySource
    static void mariaDBProperties(DynamicPropertyRegistry registry) {
        mariaDB.start();
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);

        keycloak.start();
        final String clientId = "clientId";
        final String clientSecret = "clienTSecret";
        final String realm = "master";

        KeycloakBuilder.builder()
            .serverUrl(keycloak.getAuthServerUrl())
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .username(keycloak.getAdminUsername())
            .password(keycloak.getAdminPassword())
            .build();

        registry.add("uv.security.oidc.issuer-uri", () -> keycloak.getAuthServerUrl() + "/realms/" + realm);
        registry.add("uv.security.oidc.client-id", () -> clientId);
        registry.add("uv.security.oidc.client-secret", () -> clientSecret);
        registry.add("uv.security.oidc.logout-path", () -> "/protocol/openid-connect/logout");
    }
}
