package com.restful.dscatalog.util;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.NoArgsConstructor;

import java.util.Date;

import static com.nimbusds.jose.JWSAlgorithm.HS256;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.HOURS;

@NoArgsConstructor
public final class JwtTestHelper {

    private static final String ISSUER = "http://localhost/test";
    private static final byte[] TEST_SECRET = "test-256-bit-secret-0123456789ABCDEF0123456789AB"
            .getBytes(UTF_8);

    public static String issueJwt(String subjectEmail, String... roles) {
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
}
