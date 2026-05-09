package smarttrade.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    /*
     Convert secret string into cryptographic signing key.
    */

 public JwtService(@Value("${jwt.secret}") String secret) {
     this.key = Keys.hmacShaKeyFor(
             secret.getBytes(StandardCharsets.UTF_8)
     );
 }


 /*
     Generate JWT token.

     Subject = user's email.

     Token contains:
     - subject
     - issue time
     - expiration
     - signature
 */
    public String generateToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
        Extract all claims from token.

        Claims contain:
        - subject
        - expiration
        - timestamps
    */
    private Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /*
        Extract email from token.
    */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /*
        Validate token against user.

        Checks:
        - same email
        - token not expired
    */
    public boolean isTokenValid(String token, String email) {

        String extractedEmail = extractEmail(token);

        return extractedEmail.equals(email)
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }
}
