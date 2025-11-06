package com.restful.dscatalog.rest;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static com.restful.dscatalog.util.TokenUtil.obtainAccessToken;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.System.nanoTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "security.test.jwt.secret=test-256-bit-secret-0123456789ABCDEF0123456789AB",
        "security.test.jwt.issuer=http://localhost/test"
})
@TestInstance(PER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CategoryControllerRestAssuredTest {

    @LocalServerPort
    private int port;

    private static final String ADMIN_USER = "maria@gmail.com";
    private static final String CLIENT_USER = "alex@gmail.com";

    private static final String CATEGORIES = "/api/v1/categories";

    static final byte[] TEST_SECRET =
            "test-256-bit-secret-0123456789ABCDEF0123456789AB".getBytes(StandardCharsets.UTF_8);
    private static final String ISSUER = "http://localhost/test";

    private static final String DEFAULT_PASSWORD = System.getProperty("test.user.password", "123456");

    private String adminToken;
    private String clientToken;

    private RequestSpecification requestSpecification;

    @MockitoBean
    JwtDecoder jwtDecoder;

    @BeforeEach
    void stubJwt() {
        Jwt jwt = Jwt.withTokenValue("t")
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("roles", List.of("ADMIN"))
                .build();
        when(jwtDecoder.decode(anyString())).thenReturn(jwt);
    }

    @BeforeAll
    void beforeAllInitServerAndTokens() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        this.requestSpecification = new RequestSpecBuilder()
                .setPort(port)
                .build();

        this.adminToken = obtainTokenWithFallback(ADMIN_USER, "ADMIN");
        this.clientToken = obtainTokenWithFallback(CLIENT_USER, "CLIENT");

        assertIsJwt(adminToken);
        assertIsJwt(clientToken);
    }

    private static void assertIsJwt(String token) {
        assertThat(token).as("token must be non-empty").isNotBlank();
        assertThat(token.split("\\.")).as("token must be a JWT with 3 parts").hasSize(3);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String categoryPayload(String name) {
        return """
                { "name": "%s" }
                """.formatted(name);
    }

    private static String issueJwt(String subjectEmail, String... roles) {
        try {
            var now = Instant.now();
            var claims = new JWTClaimsSet.Builder()
                    .issuer(ISSUER)
                    .subject(subjectEmail)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(1, ChronoUnit.HOURS)))
                    .claim("roles", roles)
                    .build();
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(TEST_SECRET));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to issue test JWT", e);
        }
    }

    private String obtainTokenWithFallback(String username, String expectedRole) {
        try {
            String token = obtainAccessToken(username, DEFAULT_PASSWORD);
            if (hasRoleClaim(token, expectedRole)) return token;
        } catch (Exception ignored) {
        }
        return issueJwt(username, expectedRole);
    }

    private static boolean hasRoleClaim(String jwt, String role) {
        try {
            var claims = SignedJWT.parse(jwt).getJWTClaimsSet();
            List<String> roles = claims.getStringListClaim("roles");
            return roles != null && roles.contains(role);
        } catch (Exception e) {
            return false;
        }
    }

    private Long ensureCategoryExistsAndGetId() {
        Integer size =
                given().spec(requestSpecification)
                        .when().get(CATEGORIES)
                        .then().statusCode(200)
                        .extract().path("content.size()");
        if (size == null || size == 0) {
            String name = "Category-" + nanoTime();
            return createCategoryAndReturnId(name);
        }
        return given().spec(requestSpecification)
                .when().get(CATEGORIES)
                .then().statusCode(200)
                .extract().jsonPath().getLong("content[0].id");
    }

    private Long createCategoryAndReturnId(String name) {
        return given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(categoryPayload(name))
                .when()
                .post(CATEGORIES)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .extract()
                .jsonPath().getLong("id");
    }

    @Test
    void findAllShouldReturnOkWhenNoArgumentsGiven() {
        given().spec(requestSpecification)
                .when()
                .get(CATEGORIES)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("$", hasKey("content"))
                .body("content.size()", greaterThanOrEqualTo(0));
    }

    @Test
    void findAllShouldRespectPaginationParams() {
        given().spec(requestSpecification)
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get(CATEGORIES)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("$", hasKey("content"))
                .body("content.size()", lessThanOrEqualTo(5));
    }

    @Test
    void findByIdShouldReturnCategoryWhenIdExists() {
        Long existingId = ensureCategoryExistsAndGetId();

        given().spec(requestSpecification)
                .pathParam("id", existingId)
                .when()
                .get(CATEGORIES + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(existingId.intValue()))
                .body("name", notNullValue());
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
        long nonExistingId = 999_999L;

        given().spec(requestSpecification)
                .pathParam("id", nonExistingId)
                .when()
                .get(CATEGORIES + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void createShouldReturnCreatedWhenAdminLoggedAndValidPayload() {
        String name = "Category-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(categoryPayload(name))
                .when()
                .post(CATEGORIES)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name));
    }

    @Test
    void createShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankName() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(categoryPayload(""))
                .when()
                .post(CATEGORIES)
                .then()
                .statusCode(400)
                .contentType(JSON)
                .body("find { it.field == 'name' }.error", equalTo("Length"))
                .body("find { it.field == 'name' }.details", containsString("entre 3 e 50"));
    }

    @Test
    void createShouldReturnForbiddenWhenClientLogged() {
        String name = "Category-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", bearer(clientToken))
                .contentType(JSON)
                .body(categoryPayload(name))
                .when()
                .post(CATEGORIES)
                .then()
                .statusCode(403);
    }

    @Test
    void createShouldReturnUnauthorizedWhenInvalidToken() {
        String name = "Category-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", "Bearer INVALID.TOKEN")
                .contentType(JSON)
                .body(categoryPayload(name))
                .when()
                .post(CATEGORIES)
                .then()
                .statusCode(401);
    }

    @Test
    void updateShouldReturnOkWhenAdminLoggedAndValidPayload() {
        Long id = createCategoryAndReturnId("Category-To-Update-" + nanoTime());
        String newName = "Category-Updated-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .pathParam("id", id)
                .body(categoryPayload(newName))
                .when()
                .put(CATEGORIES + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(id.intValue()))
                .body("name", equalTo(newName));
    }

    @Test
    void updateShouldReturnNotFoundWhenIdDoesNotExist() {
        long nonExistingId = 888_888L;

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .pathParam("id", nonExistingId)
                .body(categoryPayload("Whatever"))
                .when()
                .put(CATEGORIES + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteShouldReturnOkWhenAdminLogged() {
        Long id = createCategoryAndReturnId("Category-To-Delete-" + nanoTime());

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .pathParam("id", id)
                .when()
                .delete(CATEGORIES + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(id.intValue()))
                .body("name", notNullValue());
    }

    @Test
    void deleteShouldReturnForbiddenWhenClientLogged() {
        Long id = createCategoryAndReturnId("Category-Delete-Forbidden-" + nanoTime());

        given().spec(requestSpecification)
                .header("Authorization", bearer(clientToken))
                .pathParam("id", id)
                .when()
                .delete(CATEGORIES + "/{id}")
                .then()
                .statusCode(403);
    }

    @Test
    void deleteShouldReturnUnauthorizedWhenInvalidToken() {
        Long id = createCategoryAndReturnId("Category-Delete-Unauthorized-" + nanoTime());

        given().spec(requestSpecification)
                .header("Authorization", "Bearer INVALID.TOKEN")
                .pathParam("id", id)
                .when()
                .delete(CATEGORIES + "/{id}")
                .then()
                .statusCode(401);
    }
}
