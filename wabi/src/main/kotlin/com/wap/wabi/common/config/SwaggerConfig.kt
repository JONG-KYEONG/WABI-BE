package com.wap.wabi.common.config

import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.HandlerMethod


@Configuration
class SwaggerConfig {
    @Bean
    fun UMCstudyAPI(): OpenAPI {
        val info = Info()
            .title("WABI API")
            .description("WABI API 명세서입니다.")
            .version("1.0.0")
        val jwtSchemeName = "JWT TOKEN"
        // API 요청헤더에 인증정보 포함
        val securityRequirement = SecurityRequirement().addList(jwtSchemeName)
        // SecuritySchemes 등록
        val components = Components()
            .addSecuritySchemes(
                jwtSchemeName, SecurityScheme()
                    .name(jwtSchemeName)
                    .type(SecurityScheme.Type.HTTP) // HTTP 방식
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )

        val server = Server()
        server.url = "https://zepelown.site"

        val localServer = Server()
        localServer.url = "http://localhost:8080"

        return OpenAPI()
            .servers(listOf(server, localServer))
            .info(info)
            .addSecurityItem(securityRequirement)
            .components(components)
    }

    @Bean
    fun globalHeader() = OperationCustomizer { operation: Operation, _: HandlerMethod ->
        operation.addParametersItem(
            Parameter()
            .`in`(ParameterIn.HEADER.toString())
            .schema(StringSchema().name("Refresh-Token"))
            .name("Refresh-Token"))
        operation
    }
}
