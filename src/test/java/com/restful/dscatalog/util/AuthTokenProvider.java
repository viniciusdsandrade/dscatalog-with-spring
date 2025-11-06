package com.restful.dscatalog.util;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lombok.NoArgsConstructor;

import static io.restassured.RestAssured.given;
import static java.security.Security.getProperty;

@NoArgsConstructor
public final class AuthTokenProvider {

    public static String getAccessToken(String username, String password) {
        String useReal = getProperty("use.real.oauth2");
        if ("true".equalsIgnoreCase(useReal)) {
            return obtainAccessTokenViaOauth(username, password);
        }
        return JwtTestHelper.issueJwt(username, "CLIENT");
    }

    private static String obtainAccessTokenViaOauth(String username, String password) {
        Response response =
                given()
                        .auth()
                        .preemptive()
                        .basic("myclientid", "myclientsecret")
                        .contentType("application/x-www-form-urlencoded")
                        .formParam("grant_type", "password")
                        .formParam("username", username)
                        .formParam("password", password)
                        .when()
                        .post("/oauth2/token");

        response.then().statusCode(200);
        JsonPath json = response.jsonPath();
        return json.getString("access_token");
    }
}
