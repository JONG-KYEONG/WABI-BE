package com.wap.wabi.band.controller

import com.wap.wabi.auth.admin.service.AdminService
import com.wap.wabi.auth.jwt.JwtTokenProvider
import com.wap.wabi.band.payload.response.BandStudentsData
import com.wap.wabi.band.service.BandQueryService
import com.wap.wabi.common.payload.response.Response
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bands/")
class BandQueryController(
    private val bandQueryService: BandQueryService,
    private val jwtTokenProvider: JwtTokenProvider,
    private val adminService: AdminService
) {
    @GetMapping("{bandId}/students")
    @Operation(
        summary = "밴드에 속한 학생 명단 조회"
    )
    fun getBandStudents(@PathVariable bandId: Long): ResponseEntity<Response> {
        val response = Response.ok(data = BandStudentsData(bandQueryService.getBandStudents(bandId = bandId)))

        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("list")
    @Operation(
        summary = "해당 계정의 밴드 목록을 불러옵니다."
    )
    fun getBands(): ResponseEntity<Response> {
        val adminName = jwtTokenProvider.getAdminName()
        val adminId = adminService.getAdminId(adminName = adminName)
        val response = Response.ok(data = bandQueryService.getBands(adminId = adminId))
        return ResponseEntity(response, HttpStatus.OK)
    }

    @GetMapping("{bandId}/detail")
    @Operation(
        summary = "밴드 상세 정보 조회"
    )
    fun getBandDetail(@PathVariable bandId: Long): ResponseEntity<Response> {

        val response = Response.ok(data = bandQueryService.getBandDetail(bandId = bandId))

        return ResponseEntity(response, HttpStatus.OK)
    }


}
