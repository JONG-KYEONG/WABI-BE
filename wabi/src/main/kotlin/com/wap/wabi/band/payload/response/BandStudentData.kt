package com.wap.wabi.band.payload.response

import com.wap.wabi.band.entity.BandStudent

data class BandStudentData(
    val studentId: String,
    val name: String,
    val club: String,
    val position: String,
    val joinDate: String,
    val college: String,
    val major: String,
    val tel: String,
    val academicStatus: String
) {
    companion object {
        fun of(bandStudent: List<BandStudent>): List<BandStudentData> {
            return bandStudent.map { eventStudent ->
                of(eventStudent)
            }
        }

        fun of(bandStudent: BandStudent): BandStudentData {
            return BandStudentData(
                studentId = bandStudent.student.id,
                name = bandStudent.student.name,
                club = bandStudent.club,
                position = bandStudent.position,
                joinDate = bandStudent.joinDate?.toString() ?: "",
                college = bandStudent.college,
                major = bandStudent.college,
                tel = bandStudent.tel,
                academicStatus = bandStudent.academicStatus
            )
        }
    }
}
