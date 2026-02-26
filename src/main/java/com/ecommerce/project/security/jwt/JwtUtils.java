package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImplementation;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.security.Key;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;

    @Value("${spring.ecom.app.jwtCookieName}")
    private String jwtCookie;

    // Getting JWT from the request header
//    public String getJwtFromHeader(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        logger.debug("Authorization header: {}", bearerToken);
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);    // removes bearer and space
//        }
//        return null;
//    }

    public String getJwtFromCookies(HttpServletRequest request){
        Cookie cookie = WebUtils.getCookie(request,jwtCookie);
        if(cookie != null){
            return cookie.getValue();
        }
        else{
            logger.debug("JWT cookie not found");
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(UserDetailsImplementation userPrincipal){
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, jwt)
                .path("/api")
                .maxAge(24 * 60 * 60) // 1 day
                .httpOnly(false)
                .build();
        logger.debug("Generated JWT cookie: {}", cookie);
        return cookie;
    }

    public ResponseCookie generateCleanJwtCookie(){
        ResponseCookie cookie = ResponseCookie.from(jwtCookie, null)
                .path("/api")
                .build();
        logger.debug("Invalidated JWT cookie: {}", cookie);
        return cookie;
    }

    // Generating token from username
    public String generateTokenFromUsername(String username){
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith((SecretKey) key())
                .compact();
    }

    // Getting username from the token
    public String getUserNameFromToken(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key())
                .build().parseSignedClaims(token)
                .getPayload().getSubject();
    }

    // Generating signing key
    public Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    // Validating the token
    public boolean validateJwtToken(String authToken){
        try{
            System.out.println("Validating token: " + authToken);
            Jwts.parser()
                    .verifyWith((SecretKey) key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        }
        catch (MalformedJwtException e){
            logger.error("Invalid JWT token: {}" , e.getMessage());
        }
        catch(ExpiredJwtException e){
            logger.error("Token expired: {}" , e.getMessage());
        }
        catch (UnsupportedJwtException e){
            logger.error("Unsupported JWT token: {}" , e.getMessage());
        }
        catch (IllegalArgumentException e){
            logger.error("JWT claims string is empty: {}" , e.getMessage());
        }
        catch (Exception e){
            logger.error("Error validating JWT token: {}" , e.getMessage());
        }
        return false;
    }
}
