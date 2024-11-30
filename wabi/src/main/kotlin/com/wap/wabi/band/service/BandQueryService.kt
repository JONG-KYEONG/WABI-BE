package com.wap.wabi.band.service

import com.wap.wabi.auth.admin.repository.AdminRepository
import com.wap.wabi.band.entity.Band
import com.wap.wabi.band.payload.response.BandDetailData
import com.wap.wabi.band.payload.response.BandStudentData
import com.wap.wabi.band.payload.response.BandsData
import com.wap.wabi.band.repository.BandRepository
import com.wap.wabi.band.repository.BandStudentRepository
import com.wap.wabi.exception.ErrorCode
import com.wap.wabi.exception.RestApiException
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class BandQueryService(
    private val bandRepository: BandRepository,
    private val bandStudentRepository: BandStudentRepository,
    private val adminRepository: AdminRepository,
) {
    @Transactional
    fun getBandStudents(bandId: Long): List<BandStudentData> {
        val band = bandRepository.findById(bandId).orElseThrow { RestApiException(ErrorCode.NOT_FOUND_BAND) }

        val bandStudents = bandStudentRepository.findAllByBand(band)

        return BandStudentData.of(bandStudents)
    }

    @Transactional
    fun getBands(adminId: Long): List<BandsData> {
        val admin = adminRepository.findById(adminId)

        if (admin.isEmpty) {
            throw RestApiException(ErrorCode.UNAUTHORIZED_REQUEST)
        }

        val bands: List<Band> = bandRepository.findAllByAdminId(admin.get().id)
        val bandsDatas: MutableList<BandsData> = mutableListOf()
        bands.forEach { band ->
            if (band.isAvailable()) {
                bandsDatas.add(BandsData(bandId = band.id, bandName = band.bandName))
            }
        }

        return bandsDatas
    }

    @Transactional
    fun getBandDetail(bandId: Long): BandDetailData {
        val band = bandRepository.findById(bandId)
            .orElseThrow { RestApiException(ErrorCode.NOT_FOUND_BAND) }

        return BandDetailData.of(band = band)
    }
}
