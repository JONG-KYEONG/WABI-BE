package com.wap.wabi.band.service

import com.wap.wabi.band.fixture.BandFixture
import com.wap.wabi.band.fixture.BandStudentFixture
import com.wap.wabi.band.payload.BandStudentDto
import com.wap.wabi.band.payload.request.BandCreateRequest
import com.wap.wabi.band.payload.request.BandStudentEnrollRequest
import com.wap.wabi.band.payload.request.BandUpdateRequest
import com.wap.wabi.band.repository.BandRepository
import com.wap.wabi.band.repository.BandStudentRepository
import com.wap.wabi.exception.ErrorCode
import com.wap.wabi.exception.RestApiException
import com.wap.wabi.student.fixture.StudentFixture
import com.wap.wabi.student.repository.StudentRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDate
import java.util.*

@Transactional
@SpringBootTest
@SuppressWarnings("NonAsciiCharacters")
class BandCommandServiceTest {
    @Autowired
    private lateinit var bandCommandService: BandCommandService

    @MockBean
    private lateinit var bandRepository: BandRepository

    @MockBean
    private lateinit var bandStudentRepository: BandStudentRepository

    @MockBean
    private lateinit var studentRepository: StudentRepository

    @Test
    fun 밴드에_학생_정보를_저장한다() {
        // Given
        val bandId = 1L
        val band = BandFixture.createBand("Band 1")

        val studentId = "201913050"
        val name = "김종경"
        val club = "WAP"
        val position = "회원"
        val joinDate = LocalDate.parse("2023-09-03")
        val college = "정보융합대학"
        val major = "컴퓨터공학전공"
        val tel = "010-6406-9778"
        val academicStatus = "재학"
        val bandStudentDto = BandStudentDto(
            studentId = studentId,
            name = name,
            club = club,
            position = position,
            joinDate = joinDate,
            college = college,
            major = major,
            tel = tel,
            academicStatus = academicStatus
        )
        val bandStudentDtos: MutableList<BandStudentDto> = mutableListOf(bandStudentDto)
        val bandStudentEnrollRequest = BandStudentEnrollRequest(
            bandStudentDtos
        )

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(band))
        `when`(studentRepository.findById(studentId)).thenReturn(Optional.of(StudentFixture.createStudent("김종경")))

        //When & Then
        Assertions.assertDoesNotThrow {
            bandCommandService.enrollBandStudent(bandId = bandId, request = bandStudentEnrollRequest)
        }
    }

    @Test
    fun 유효하지_않은_밴드Id_값을_입력하여_학생_정보를_저장하려하면_NOT_FOUND_BAND_예외를_반환한다() {
        val bandId = 1L
        val invalidBandId = 2L
        val band = BandFixture.createBand("Band 1")

        val studentId = "201913050"
        val name = "김종경"
        val club = "WAP"
        val position = "회원"
        val joinDate = LocalDate.parse("2023-09-03")
        val college = "정보융합대학"
        val major = "컴퓨터공학전공"
        val tel = "010-6406-9778"
        val academicStatus = "재학"
        val bandStudentDto = BandStudentDto(
            studentId = studentId,
            name = name,
            club = club,
            position = position,
            joinDate = joinDate,
            college = college,
            major = major,
            tel = tel,
            academicStatus = academicStatus
        )
        val bandStudentDtos: MutableList<BandStudentDto> = mutableListOf()
        bandStudentDtos.add(bandStudentDto)
        val bandStudentEnrollRequest = BandStudentEnrollRequest(
            bandStudentDtos
        )

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(band))
        `when`(studentRepository.findById(studentId)).thenReturn(Optional.of(StudentFixture.createStudent("김종경")))

        //When
        val exception = assertThrows<RestApiException> {
            bandCommandService.enrollBandStudent(bandId = invalidBandId, request = bandStudentEnrollRequest)
        }

        //Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.NOT_FOUND_BAND)
    }

    @Test
    fun 밴드를_생성한다() {
        //Given
        val adminId = 1L
        val bandName = "band 1"
        val bandCreateRequest = BandCreateRequest(
            bandName = bandName,
            bandMemo = "band memo"
        )

        val savedBand = BandFixture.createBand(id = 1, name = bandName)

        `when`(bandRepository.save(ArgumentMatchers.any())).thenReturn(savedBand)

        //When & Then
        Assertions.assertDoesNotThrow {
            bandCommandService.createBand(adminId = adminId, bandCreateRequest = bandCreateRequest)
        }
    }

    @Test
    fun 밴드_생성_시_유효하지_않은_adminId_값을_입력하면_UNAUTHORIZED_REQUEST_예외를_반환한다() {
        // Given
        val invalidAdminId = -1L
        val bandName = "band 1"
        val bandCreateRequest = BandCreateRequest(
            bandName = bandName,
            bandMemo = "band memo"
        )

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.createBand(adminId = invalidAdminId, bandCreateRequest = bandCreateRequest)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_REQUEST)
    }

    @Test
    fun 밴드를_삭제한다() {
        //Given
        val adminId = 1L
        val bandId = 1L
        val bandName = "band 1"

        val savedBand = BandFixture.createBand(id = 1, name = bandName)

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(savedBand))

        //When & Then
        Assertions.assertDoesNotThrow {
            bandCommandService.deleteBand(adminId = adminId, bandId = bandId)
        }
    }

    @Test
    fun 밴드_삭제_시_유효하지_않은_bandId_값을_입력하면_NOT_FOUND_BAND_예외를_반환한다() {
        // Given
        val adminId = 1L
        val invalidBandId = 2L

        `when`(bandRepository.findById(invalidBandId)).thenReturn(Optional.empty())

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.deleteBand(adminId = adminId, bandId = adminId)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.NOT_FOUND_BAND)
    }

    @Test
    fun 밴드_삭제_시_자신이_생성한_밴드가_아니면_UNAUTHORIZED_BAND_예외를_반환한다() {
        // Given
        val adminId = 1L
        val bandId = 1L
        val savedBand = BandFixture.createAnotherUserBand(id = 1, name = "bandName")

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(savedBand))

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.deleteBand(adminId = adminId, bandId = bandId)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_BAND)
    }

    @Test
    fun 밴드_삭제_시_유효하지_않은_adminId_값을_입력하면_UNAUTHORIZED_REQUEST_예외를_반환한다() {
        // Given
        val invalidAdminId = 2L
        val bandId = 1L
        val bandName = "band 1"

        val savedBand = BandFixture.createBand(id = 1, name = bandName)

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(savedBand))

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.deleteBand(adminId = invalidAdminId, bandId = bandId)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_BAND)
    }

    @Test
    fun 밴드를_수정한다() {
        // Given
        val adminId = 1L
        val bandId = 1L
        val savedBand = BandFixture.createBand("Band 1", 1)
        val bandUpdateRequest = BandUpdateRequest(
            bandId = bandId,
            bandName = "new band name",
            bandMemo = "band memo"
        )

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(savedBand))

        //When & Then
        Assertions.assertDoesNotThrow {
            bandCommandService.updateBand(adminId = adminId, bandUpdateRequest = bandUpdateRequest)
        }
    }

    @Test
    fun 밴드_수정_시_유효하지_않은_bandId_값을_입력하면_NOT_FOUND_BAND_예외를_반환한다() {
        // Given
        val adminId = 1L
        val invalidBandId = 2L
        val bandUpdateRequest = BandUpdateRequest(
            bandId = invalidBandId,
            bandName = "new band name",
            bandMemo = "band memo"
        )

        `when`(bandRepository.findById(invalidBandId)).thenReturn(Optional.empty())

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.updateBand(adminId = adminId, bandUpdateRequest = bandUpdateRequest)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.NOT_FOUND_BAND)
    }

    @Test
    fun 밴드_수정_시_유효하지_않은_adminId_값을_입력하면_UNAUTHORIZED_REQUEST_예외를_반환한다() {
        // Given
        val invalidAdminId = 2L
        val bandId = 1L
        val bandUpdateRequest = BandUpdateRequest(
            bandId = bandId,
            bandName = "new band name",
            bandMemo = "band memo"
        )

        val savedBand = BandFixture.createBand("Band 1", 1)
        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(savedBand))

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.updateBand(adminId = invalidAdminId, bandUpdateRequest = bandUpdateRequest)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_BAND)
    }

    @Test
    fun 밴드_수정_시_자신이_생성한_밴드가_아니면_UNAUTHORIZED_BAND_예외를_반환한다() {
        // Given
        val adminId = 1L
        val bandId = 1L
        val bandUpdateRequest = BandUpdateRequest(
            bandId = bandId,
            bandName = "new band name",
            bandMemo = "band memo"
        )
        val savedBand = BandFixture.createAnotherUserBand(id = 1, name = "bandName")

        `when`(bandRepository.findById(bandId)).thenReturn(Optional.of(savedBand))

        // When
        val exception = assertThrows<RestApiException> {
            bandCommandService.updateBand(adminId = adminId, bandUpdateRequest = bandUpdateRequest)
        }

        // Then
        assertThat(exception.errorCode).isEqualTo(ErrorCode.UNAUTHORIZED_BAND)
    }

}
