package com.restful.dscatalog.restassured;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static com.nimbusds.jose.JWSAlgorithm.HS256;
import static com.restful.dscatalog.util.TokenUtil.obtainAccessToken;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.System.nanoTime;
import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.*;
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
class ProductControllerRestAssuredTest {

    @LocalServerPort
    private int port;

    private static final String ADMIN_USER = "maria@gmail.com";
    private static final String CLIENT_USER = "alex@gmail.com";

    private static final String PRODUCTS = "/api/v1/products";

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
        when(jwtDecoder.decode(eq("INVALID.TOKEN")))
                .thenThrow(new JwtException("Invalid token"));

        when(jwtDecoder.decode(argThat(t -> !"INVALID.TOKEN".equals(t))))
                .thenAnswer(inv -> {
                    String token = inv.getArgument(0);
                    JWTClaimsSet claims = SignedJWT.parse(token).getJWTClaimsSet();
                    var roles = claims.getStringListClaim("roles");

                    return Jwt.withTokenValue(token)
                            .header("alg", "HS256")
                            .subject(claims.getSubject())
                            .claim("roles", roles)
                            .build();
                });
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

    private static String nowIsoSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private static String jsonArrayOfLongs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return "[]";
        StringJoiner sj = new StringJoiner(",", "[", "]");
        ids.forEach(id -> sj.add(String.valueOf(id)));
        return sj.toString();
    }

    private static String jsonArrayOfStrings(List<String> values) {
        if (values == null || values.isEmpty()) return "[]";
        StringJoiner sj = new StringJoiner(",", "[", "]");
        values.forEach(v -> sj.add("\"" + v + "\""));
        return sj.toString();
    }

    private static String productPayload(
            String name,
            String description,
            double price,
            String isoDate,
            List<Long> categoryIds
    ) {
        String categories = jsonArrayOfLongs(categoryIds);
        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "price": %s,
                  "imgUrl": %s,
                  "date": "%s",
                  "categoryIds": %s
                }
                """.formatted(
                name,
                description,
                String.valueOf(price),
                null,
                isoDate,
                categories
        );
    }

    private static String productByNamesPayload(
            String name,
            String description,
            double price,
            List<String> categoryNames
    ) {
        String names = jsonArrayOfStrings(categoryNames);
        return """
                {
                  "name": "%s",
                  "description": "%s",
                  "price": %s,
                  "imgUrl": %s,
                  "categoryNames": %s
                }
                """.formatted(
                name,
                description,
                String.valueOf(price),
                null,
                names
        );
    }

    private static String issueJwt(String subjectEmail, String... roles) {
        try {
            var now = now();
            var claims = new JWTClaimsSet.Builder()
                    .issuer(ISSUER)
                    .subject(subjectEmail)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(1, ChronoUnit.HOURS)))
                    .claim("roles", roles)
                    .build();
            var jwt = new SignedJWT(new JWSHeader(HS256), claims);
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

    private Long ensureProductExistsAndGetId() {
        Integer size =
                given().spec(requestSpecification)
                        .header("Authorization", bearer(adminToken))
                        .when().get(PRODUCTS)
                        .then().statusCode(200)
                        .extract().path("content.size()");
        if (size == null || size == 0) {
            String name = "Product-" + nanoTime();
            return createProductAndReturnIdUsingPostDTO(
                    name,
                    "Desc " + name,
                    99.9,
                    nowIsoSeconds(),
                    List.of()
            );
        }
        return given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .when()
                .get(PRODUCTS)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("content[0].id");
    }

    private Long createProductAndReturnIdUsingPostDTO(
            String name,
            String description,
            double price,
            String isoDate,
            List<Long> categoryIds
    ) {
        return given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(productPayload(name, description, price, isoDate, categoryIds))
                .when()
                .post(PRODUCTS)
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
                .header("Authorization", bearer(adminToken))
                .when()
                .get(PRODUCTS)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .header("X-Page-Number", notNullValue())
                .header("X-Page-Size", notNullValue())
                .header("X-Total-Count", notNullValue())
                .body("$", hasKey("content"))
                .body("content.size()", greaterThanOrEqualTo(0));
    }

    @Test
    void findAllShouldRespectPaginationParamsAndHeaders() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get(PRODUCTS)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .header("X-Page-Number", equalTo("0"))
                .header("X-Page-Size", equalTo("5"))
                .body("$", hasKey("content"))
                .body("content.size()", lessThanOrEqualTo(5));
    }

    @Test
    void findAllWithoutPaginationShouldReturnList() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .when()
                .get(PRODUCTS + "/without-pagination")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    void findByIdShouldReturnProductWhenIdExists() {
        Long existingId = ensureProductExistsAndGetId();

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .pathParam("id", existingId)
                .when()
                .get(PRODUCTS + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(existingId.intValue()))
                .body("name", notNullValue())
                .body("price", notNullValue())
                .body("categories", notNullValue());
    }

    @Test
    void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
        long nonExistingId = 999_999L;

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .pathParam("id", nonExistingId)
                .when()
                .get(PRODUCTS + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void createShouldReturnCreatedWhenAdminLoggedAndValidPayload() {
        String name = "Product-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(productPayload(name, "Desc " + name, 123.45, nowIsoSeconds(), List.of()))
                .when()
                .post(PRODUCTS)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name));
    }

    @Test
    void createByNamesShouldReturnCreatedAndNormalizeCategories() {
        String name = "Product-ByNames-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(productByNamesPayload(
                        name,
                        "Desc " + name,
                        77.7,
                        List.of("  ELETRONICOS  ", "informatica", "Informatica")
                ))
                .when()
                .post(PRODUCTS + "/by-names")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", notNullValue())
                .body("name", equalTo(name))
                .body("categories", hasItems("Eletronicos", "Informatica"));
    }

    @Test
    void createShouldReturnBadRequestWhenAdminLoggedAndInvalidPayload() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .body(productPayload("", "", 0.0, nowIsoSeconds(), List.of()))
                .when()
                .post(PRODUCTS)
                .then()
                .statusCode(400)
                .body("findAll { it.field == 'name' || it.field == 'price' }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void createShouldReturnUnauthorizedWhenInvalidToken() {
        String name = "Product-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", "Bearer INVALID.TOKEN")
                .contentType(JSON)
                .body(productPayload(name, "Desc " + name, 55.5, nowIsoSeconds(), List.of()))
                .when()
                .post(PRODUCTS)
                .then()
                .statusCode(401);
    }

    @Test
    void createByNamesShouldReturnUnauthorizedWhenInvalidToken() {
        String name = "Product-UNAUTH-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", "Bearer INVALID.TOKEN")
                .contentType(JSON)
                .body(productByNamesPayload(name, "Desc " + name, 10.0, List.of("informatica")))
                .when()
                .post(PRODUCTS + "/by-names")
                .then()
                .statusCode(401);
    }

    @Test
    void updateShouldReturnOkWhenAdminLoggedAndValidPayload() {
        Long id = createProductAndReturnIdUsingPostDTO(
                "Product-To-Update-" + nanoTime(),
                "Desc",
                10.0,
                nowIsoSeconds(),
                List.of()
        );
        String newName = "Product-Updated-" + nanoTime();

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .contentType(JSON)
                .pathParam("id", id)
                .body(productPayload(newName, "New Desc", 99.99, nowIsoSeconds(), List.of()))
                .when()
                .put(PRODUCTS + "/{id}")
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
                .body(productPayload("Whatever", "Desc", 1.0, nowIsoSeconds(), List.of()))
                .when()
                .put(PRODUCTS + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void deleteShouldReturnOkWhenAdminLogged() {
        Long id = createProductAndReturnIdUsingPostDTO(
                "Product-To-Delete-" + nanoTime(),
                "Desc",
                10.0,
                nowIsoSeconds(),
                List.of()
        );

        given().spec(requestSpecification)
                .header("Authorization", bearer(adminToken))
                .pathParam("id", id)
                .when()
                .delete(PRODUCTS + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(id.intValue()))
                .body("name", notNullValue());
    }

    @Test
    void deleteShouldReturnUnauthorizedWhenInvalidToken() {
        Long id = createProductAndReturnIdUsingPostDTO(
                "Product-Delete-Unauthorized-" + nanoTime(),
                "Desc",
                10.0,
                nowIsoSeconds(),
                List.of()
        );

        given().spec(requestSpecification)
                .header("Authorization", "Bearer INVALID.TOKEN")
                .pathParam("id", id)
                .when()
                .delete(PRODUCTS + "/{id}")
                .then()
                .statusCode(401);
    }
}
