package com.wap.wabi.auth.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.wap.wabi.auth.admin.repository.AdminRefreshTokenRepository
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret-key}")
    private val secretKey: String,
    @Value("\${jwt.expiration-minutes}")
    private val expirationMinutes: Long,
    @Value("\${jwt.refresh-expiration-hours}")
    private val refreshExpirationHours: Long,
    @Value("\${jwt.issuer}")
    private val issuer: String,
    private val adminRefreshTokenRepository: AdminRefreshTokenRepository
) {
    private val reissueLimit = refreshExpirationHours * 60 / expirationMinutes
    private val objectMapper = ObjectMapper()

    fun createAccessToken(userSpecification: String) = Jwts.builder()
        .signWith(
            SecretKeySpec(
                secretKey.toByteArray(),
                SignatureAlgorithm.HS512.jcaName
            )
        ) // HS512 알고리즘을 사용하여 secretKey를 이용해 서명
        .setSubject(userSpecification)   // JWT 토큰 제목
        .setIssuer(issuer)    // JWT 토큰 발급자
        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))    // JWT 토큰 발급 시간
        .setExpiration(Date.from(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES)))    // JWT 토큰의 만료시간 설정
        .compact()!!    // JWT 토큰 생성

    fun validateTokenAndGetSubject(token: String): String? = Jwts.parserBuilder()
        .setSigningKey(secretKey.toByteArray())
        .build()
        .parseClaimsJws(token)
        .body
        .subject

    fun getAdminName(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return authentication.name
    }

    fun createRefreshToken() = Jwts.builder()
        .signWith(SecretKeySpec(secretKey.toByteArray(), SignatureAlgorithm.HS512.jcaName))
        .setIssuer(issuer)
        .setIssuedAt(Timestamp.valueOf(LocalDateTime.now()))
        .setExpiration(Date.from(Instant.now().plus(refreshExpirationHours, ChronoUnit.HOURS)))
        .compact()!!

    @Transactional
    fun recreateAccessToken(oldAccessToken: String): String {
        val subject = decodeJwtPayloadSubject(oldAccessToken)
        adminRefreshTokenRepository.findAdminRefreshTokenByAdminNameAndReissueCountLessThan(
            (subject.split(':')[0]),
            reissueLimit
        ).ifPresentOrElse(
            { it.increaseReissueCount() },
            { throw ExpiredJwtException(null, null, "레프레시 토큰이 만료되었습니다.") }
        )
        return createAccessToken(subject)
    }

    @Transactional(readOnly = true)
    fun validateRefreshToken(refreshToken: String, oldAccessToken: String) {
        validateAndParseToken(refreshToken)
        val adminName = decodeJwtPayloadSubject(oldAccessToken).split(':')[0]
        adminRefreshTokenRepository.findAdminRefreshTokenByAdminNameAndReissueCountLessThan(adminName, reissueLimit)
            .ifPresentOrElse(
                { it.validateRefreshToken(refreshToken) },
                { throw ExpiredJwtException(null, null, "레프레시 토큰이 만료되었습니다.") }
            )
    }

    fun validateAndParseToken(token: String?) = Jwts.parserBuilder()    // validateTokenAndGetSubject()에서 따로 분리
        .setSigningKey(secretKey.toByteArray())
        .build()
        .parseClaimsJws(token)!!

    private fun decodeJwtPayloadSubject(oldAccessToken: String) =
        objectMapper.readValue(
            Base64.getUrlDecoder().decode(oldAccessToken.split('.')[1]).decodeToString(),
            Map::class.java
        )["sub"].toString()
}
