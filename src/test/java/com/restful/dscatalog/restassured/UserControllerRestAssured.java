package com.restful.dscatalog.restassured;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restful.dscatalog.entity.Role;
import com.restful.dscatalog.repository.RoleRepository;
import com.restful.dscatalog.util.AuthTokenProvider;
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
import java.util.UUID;

import static com.nimbusds.jose.JWSAlgorithm.HS256;
import static com.restful.dscatalog.util.JwtTestHelper.issueJwt;
import static io.restassured.RestAssured.enableLoggingOfRequestAndResponseIfValidationFails;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
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

    private static final byte[] INVALID_SECRET =
            "invalid-256-bit-secret-0123456789ABCDEF0123456789".getBytes(UTF_8);

    private static final String EXPECTED_ISSUER = "http://localhost/test";

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
                .setAccept(JSON)
                .build();
    }

    private static String uniqueEmail(String prefix) {
        return "%s-%s@example.com".formatted(prefix, UUID.randomUUID());
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
                .contentType(JSON)
                .body(body)
                .when()
                .post(USERS)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .header("Location", matchesPattern(".*/api/v1/users/\\d+"))
                .body("id", notNullValue())
                .body("email", equalToIgnoringCase(email))
                .extract()
                .jsonPath().getLong("id");
    }

    @Test
    void getAllUsers_ShouldReturnOk_WhenNoArgumentsGiven() {
        String token = AuthTokenProvider.getAccessToken("reader@example.com", "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
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
        String token = AuthTokenProvider.getAccessToken("reader@example.com", "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
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
    void getAllUsers_ShouldCoerceNegativePageToZero() {
        given().spec(requestSpecification)
                .auth().oauth2(issueJwt("reader@example.com", "CLIENT"))
                .queryParam("page", -1)
                .queryParam("size", 5)
                .when()
                .get(USERS)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .header("X-Page-Number", equalTo("0"))
                .header("X-Page-Size", equalTo("5"))
                .body("number", equalTo(0))
                .body("size", equalTo(5));
    }

    @Test
    void findById_ShouldReturnUser_WhenIdExists() {
        Long id = createUserAndReturnId(uniqueEmail("findById"));

        String token = AuthTokenProvider.getAccessToken("reader@example.com", "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
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
        String token = AuthTokenProvider.getAccessToken("reader@example.com", "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
                .pathParam("id", 999_999L)
                .when()
                .get(USERS + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void findById_ShouldReturnBadRequest_WhenIdNotNumeric() {
        String token = AuthTokenProvider.getAccessToken("reader@example.com", "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
                .when()
                .get(USERS + "/{id}", "abc")
                .then()
                .statusCode(400);
    }

    @Test
    void getMe_ShouldReturnOk_WithBearerToken() {
        String me = uniqueEmail("me");
        createUserAndReturnId(me);

        String token = AuthTokenProvider.getAccessToken(me, "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
                .when()
                .get(USERS + "/me")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("email", equalToIgnoringCase(me))
                .body("id", notNullValue());
    }

    @Test
    void getMe_ShouldReturnUnauthorized_WhenNoToken() {
        given().spec(requestSpecification)
                .when()
                .get(USERS + "/me")
                .then()
                .statusCode(401);
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
                .contentType(JSON)
                .body(body)
                .when()
                .post(USERS)
                .then()
                .statusCode(201)
                .contentType(JSON)
                .header("Location", matchesPattern(".*/api/v1/users/\\d+"))
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
                .contentType(JSON)
                .body(body)
                .when()
                .post(USERS)
                .then()
                .statusCode(400);
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

        String token = AuthTokenProvider.getAccessToken(originalEmail, "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
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

        String token = AuthTokenProvider.getAccessToken(attackerEmail, "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
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

    @Test
    void updateUser_ShouldReturnBadRequest_WhenInvalidPayload() {
        String email = uniqueEmail("upd-badreq");
        Long id = createUserAndReturnId(email);

        String invalidBody = """
            {
              "firstName": "A",
              "lastName": "",
              "email": "not-an-email"
            }""";

        String token = AuthTokenProvider.getAccessToken(email, "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
                .contentType(JSON)
                .pathParam("id", id)
                .body(invalidBody)
                .when()
                .put(USERS + "/{id}")
                .then()
                .statusCode(400);
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenIdDoesNotExist() {
        String email = uniqueEmail("upd-404");
        createUserAndReturnId(email);

        String body = """
            { "firstName": "Xy", "lastName": "Zz", "email": "%s" }
            """.formatted(uniqueEmail("upd-404-new"));

        String token = AuthTokenProvider.getAccessToken(email, "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
                .contentType(JSON)
                .pathParam("id", 9_999_999L)
                .body(body)
                .when()
                .put(USERS + "/{id}")
                .then()
                .statusCode(404);
    }

    @Test
    void updateUser_ShouldReturnConflict_WhenEmailAlreadyExists() {
        String owner = uniqueEmail("owner-conf");
        Long ownerId = createUserAndReturnId(owner);
        String other = uniqueEmail("other-conf");
        createUserAndReturnId(other);

        String body = """
            { "firstName": "Owner", "lastName": "Same", "email": "%s" }
            """.formatted(other);

        String token = AuthTokenProvider.getAccessToken(owner, "password");

        given().spec(requestSpecification)
                .auth().oauth2(token)
                .contentType(JSON)
                .pathParam("id", ownerId)
                .body(body)
                .when()
                .put(USERS + "/{id}")
                .then()
                .statusCode(409);
    }

    @Test
    void getAllUsers_ShouldReturnUnauthorized_WhenTokenExpired() {
        String expired = issueCustomJwt(-1, TEST_SECRET, EXPECTED_ISSUER);

        given().spec(requestSpecification)
                .auth().oauth2(expired)
                .when()
                .get(USERS)
                .then()
                .statusCode(401);
    }

    @Test
    void getAllUsers_ShouldReturnUnauthorized_WhenSignatureInvalid() {
        String invalidSigned = issueCustomJwt(1, INVALID_SECRET, EXPECTED_ISSUER);

        given().spec(requestSpecification)
                .auth().oauth2(invalidSigned)
                .when()
                .get(USERS)
                .then()
                .statusCode(401);
    }

    @Test
    void getAllUsers_ShouldReturnUnauthorized_WhenIssuerInvalid() {
        String badIssuerToken = issueCustomJwt(1, TEST_SECRET, "http://malicious/issuer");

        given().spec(requestSpecification)
                .auth().oauth2(badIssuerToken)
                .when()
                .get(USERS)
                .then()
                .statusCode(401);
    }

    private static String issueCustomJwt(long hoursValid, byte[] secret, String issuer) {
        try {
            var iat = now();
            var claims = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .subject("reader@example.com")
                    .issueTime(Date.from(iat))
                    .expirationTime(Date.from(iat.plus(hoursValid, HOURS)))
                    .claim("username", "reader@example.com")
                    .claim("roles", new String[]{"CLIENT"})
                    .build();
            var jwt = new SignedJWT(new JWSHeader(HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to issue test JWT (negative scenario)", e);
        }
    }
}
