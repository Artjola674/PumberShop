package com.ikubinfo.plumbershop.security;

import com.ikubinfo.plumbershop.exception.BadRequestException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.crypto.MacProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

import static com.ikubinfo.plumbershop.common.constants.BadRequest.*;
import static com.ikubinfo.plumbershop.common.constants.Constants.PLUMBER_SHOP;

@Component
public class JwtTokenProvider {

    @Value("${app-jwt-expiration}")
    private long jwtExpirationDate;

    private Key secretKeySpec;

    public String getTokenFromRequest(HttpServletRequest request) {

        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public String generateToken(String username) {

        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        Key key = MacProvider.generateKey(SignatureAlgorithm.HS512);
        byte[] keyData = key.getEncoded();
        this.secretKeySpec = new SecretKeySpec(keyData, SignatureAlgorithm.HS512.getJcaName());

        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setIssuer(PLUMBER_SHOP)
                .setAudience(PLUMBER_SHOP)
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(secretKeySpec, SignatureAlgorithm.HS512)
                .compact();

    }

    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKeySpec)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKeySpec)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (MalformedJwtException ex) {
            throw new BadRequestException(INVALID_TOKEN);
        } catch (ExpiredJwtException ex) {
            throw new BadRequestException(EXPIRED_TOKEN);
        } catch (UnsupportedJwtException ex) {
            throw new BadRequestException(UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(EMPTY_CLAIMS);
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
