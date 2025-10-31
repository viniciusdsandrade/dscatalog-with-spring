package com.restful.dscatalog.restassured;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restful.dscatalog.entity.Role;
import com.restful.dscatalog.repository.RoleRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;

import static com.nimbusds.jose.JWSAlgorithm.HS256;
import static io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.System.nanoTime;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("h2")
@TestPropertySource(properties = {
        "security.test.jwt.secret=test-256-bit-secret-0123456789ABCDEF0123456789AB",
        "security.test.jwt.issuer=http://localhost/test"
})
@TestInstance(PER_CLASS)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserControllerRestAssured {

    @LocalServerPort
    private int port;

    private static final String USERS = "/api/v1/users";

    private static final byte[] TEST_SECRET =
            "test-256-bit-secret-0123456789ABCDEF0123456789AB".getBytes(UTF_8);
    private static final String ISSUER = "http://localhost/test";

    private RequestSpecification requestSpecification;

    @MockitoBean
    RoleRepository roleRepository;

    @BeforeEach
    void stubRoleRepo() {
        when(roleRepository.getReferenceById(anyLong()))
                .thenAnswer(inv -> new Role(inv.getArgument(0), "ROLE_CLIENT"));
    }

    @BeforeAll
    void beforeAllInitServer() {
        RestAssured.port = port;
        enableLoggingOfRequestAndResponseIfValidationFails();
        this.requestSpecification = new RequestSpecBuilder()
                .setPort(port)
                .build();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String uniqueEmail(String prefix) {
        return "%s-%d@example.com".formatted(prefix, nanoTime());
    }

    private static String issueJwt(String subjectEmail, String... roles) {
        try {
            var iat = now();
            var claims = new JWTClaimsSet.Builder()
                    .issuer(ISSUER)
                    .subject(subjectEmail)
                    .issueTime(Date.from(iat))
                    .expirationTime(Date.from(iat.plus(1, HOURS)))
                    .claim("username", subjectEmail)
                    .claim("roles", roles)
                    .build();
            var jwt = new SignedJWT(new JWSHeader(HS256), claims);
            jwt.sign(new MACSigner(TEST_SECRET));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to issue test JWT", e);
        }
    }

    private Long createUserAndReturnId(String email) {
        String body = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "%s",
                  "password": "Str0ng!Passw0rd"
                }""".formatted(email);

        return given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt(email, "CLIENT")))
                .contentType(JSON)
                .body(body)
                .when()
                .post(USERS)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", notNullValue())
                .body("email", equalToIgnoringCase(email))
                .extract()
                .jsonPath().getLong("id");
    }

    @Test
    void getAllUsers_ShouldReturnOk_WhenNoArgumentsGiven() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt("reader@example.com", "CLIENT")))
                .when()
                .get(USERS)
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
    void getAllUsers_ShouldRespectPaginationParams_AndHeaders() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt("reader@example.com", "CLIENT")))
                .queryParam("page", 0)
                .queryParam("size", 5)
                .when()
                .get(USERS)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .header("X-Page-Number", equalTo("0"))
                .header("X-Page-Size", equalTo("5"))
                .body("$", hasKey("content"))
                .body("content.size()", lessThanOrEqualTo(5));
    }

    @Test
    void findById_ShouldReturnUser_WhenIdExists() {
        Long id = createUserAndReturnId(uniqueEmail("findById"));

        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt("reader@example.com", "CLIENT")))
                .pathParam("id", id)
                .when()
                .get(USERS + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(id.intValue()))
                .body("email", notNullValue())
                .body("firstName", notNullValue())
                .body("lastName", notNullValue());
    }

    @Test
    void findById_ShouldReturnNotFound_WhenIdDoesNotExist() {
        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt("reader@example.com", "CLIENT")))
                .pathParam("id", 999_999L)
                .when()
                .get(USERS + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void createUser_ShouldReturnCreated_WhenValidPayload() {
        String email = uniqueEmail("create");
        String body = """
                {
                  "firstName": "Alice",
                  "lastName": "Wonder",
                  "email": "%s",
                  "password": "Str0ng!Passw0rd"
                }""".formatted(email);

        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt(email, "CLIENT")))
                .contentType(JSON)
                .body(body)
                .when()
                .post(USERS)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .body("id", notNullValue())
                .body("email", equalTo(email))
                .body("firstName", equalTo("Alice"))
                .body("lastName", equalTo("Wonder"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenInvalidPayload() {
        String body = """
                {
                  "firstName": "",
                  "lastName": "",
                  "email": "invalid-email",
                  "password": "123"
                }""";

        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt("reader@example.com", "CLIENT")))
                .contentType(JSON)
                .body(body)
                .when()
                .post(USERS)
                .then()
                .statusCode(400)
                .body("findAll { it.field == 'firstName' || it.field == 'lastName' || it.field == 'email' || it.field == 'password' }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    void updateUser_ShouldReturnOk_WhenOwnerLogged_AndValidPayload() {
        String originalEmail = uniqueEmail("owner");
        Long id = createUserAndReturnId(originalEmail);

        String newEmail = uniqueEmail("owner-updated");
        String body = """
                {
                  "firstName": "OwnerUpdated",
                  "lastName": "Surname",
                  "email": "%s"
                }""".formatted(newEmail);

        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt(originalEmail, "CLIENT")))
                .contentType(JSON)
                .pathParam("id", id)
                .body(body)
                .when()
                .put(USERS + "/{id}")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("id", equalTo(id.intValue()))
                .body("firstName", equalTo("OwnerUpdated"))
                .body("lastName", equalTo("Surname"))
                .body("email", equalTo(newEmail));
    }

    @Test
    void updateUser_ShouldReturnForbidden_WhenDifferentUserLogged() {
        String ownerEmail = uniqueEmail("owner2");
        Long id = createUserAndReturnId(ownerEmail);

        String attackerEmail = uniqueEmail("attacker");
        String body = """
                {
                  "firstName": "Hacker",
                  "lastName": "McHack",
                  "email": "%s"
                }""".formatted(uniqueEmail("new-email"));

        given().spec(requestSpecification)
                .header("Authorization", bearer(issueJwt(attackerEmail, "CLIENT")))
                .contentType(JSON)
                .pathParam("id", id)
                .body(body)
                .when()
                .put(USERS + "/{id}")
                .then()
                .statusCode(403);
    }

    @Test
    void updateUser_ShouldReturnUnauthorized_WhenNoTokenProvided() {
        String email = uniqueEmail("unauth");
        Long id = createUserAndReturnId(email);

        String body = """
                {
                  "firstName": "NoAuth",
                  "lastName": "User",
                  "email": "%s"
                }""".formatted(uniqueEmail("noauth-new"));

        given().spec(requestSpecification)
                .contentType(JSON)
                .pathParam("id", id)
                .body(body)
                .when()
                .put(USERS + "/{id}")
                .then()
                .statusCode(401);
    }
}
