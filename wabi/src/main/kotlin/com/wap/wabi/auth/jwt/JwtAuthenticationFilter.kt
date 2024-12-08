package com.wap.wabi.auth.jwt

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Order(0)
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
        if (path.startsWith("/swagger-ui/") || path.startsWith("/v3") || path.startsWith("/auth")) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            parseBearerToken(request, HttpHeaders.AUTHORIZATION)?.let { accessToken ->
                jwtTokenProvider.validateAndParseToken(accessToken)

                val user = parseUserSpecification(accessToken)
                val authentication = UsernamePasswordAuthenticationToken.authenticated(user, accessToken, user.authorities)
                authentication.details = WebAuthenticationDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: ExpiredJwtException) {
            // Access Token이 만료된 경우 리프레시 토큰으로 새 토큰 발급
            if (reissueAccessToken(request, response)) {
                return // 새 토큰 발급 후 요청 종료
            }
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Invalid Token")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun parseBearerToken(request: HttpServletRequest, headerName: String): String? {
        return request.getHeader(headerName)
            .takeIf { it.startsWith("Bearer ", ignoreCase = true) }
            ?.substring(7)
    }

    private fun parseUserSpecification(token: String): User {
        val subject =
            jwtTokenProvider.validateTokenAndGetSubject(token) ?: throw IllegalArgumentException("Invalid token")
        val (username, role) = subject.split(":")
        return User(username, "", listOf(SimpleGrantedAuthority(role)))
    }

    private fun reissueAccessToken(request: HttpServletRequest, response: HttpServletResponse): Boolean {
        return try {
            val refreshToken = parseBearerToken(request, "Refresh-Token")
                ?: throw IllegalArgumentException("Refresh token not provided")
            val oldAccessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION)
                ?: throw IllegalArgumentException("Access token not provided")

            // 리프레시 토큰 유효성 검사 및 새로운 액세스 토큰 발급
            jwtTokenProvider.validateRefreshToken(refreshToken, oldAccessToken)
            val newAccessToken = jwtTokenProvider.recreateAccessToken(oldAccessToken)

            // 새 액세스 토큰을 응답 헤더에 추가
            response.setHeader("New-Access-Token", newAccessToken)

            // SecurityContext에 새 인증 정보 업데이트
            val user = parseUserSpecification(newAccessToken)
            val authentication =
                UsernamePasswordAuthenticationToken.authenticated(user, newAccessToken, user.authorities)
            authentication.details = WebAuthenticationDetails(request)
            SecurityContextHolder.getContext().authentication = authentication

            true
        } catch (e: Exception) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.writer.write("Refresh token invalid or expired: ${e.message}")
            false
        }
    }
}
