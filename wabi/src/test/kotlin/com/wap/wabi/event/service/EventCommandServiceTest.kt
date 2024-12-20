package com.wap.wabi.event.service

import com.wap.wabi.band.fixture.BandFixture
import com.wap.wabi.band.fixture.BandStudentFixture
import com.wap.wabi.band.repository.BandRepository
import com.wap.wabi.band.repository.BandStudentRepository
import com.wap.wabi.common.TestConstants
import com.wap.wabi.event.entity.Enum.EventStudentStatus
import com.wap.wabi.event.fixture.EventFixture
import com.wap.wabi.event.fixture.EventStudentFixture
import com.wap.wabi.event.payload.request.CheckInRequest
import com.wap.wabi.event.payload.request.EventCreateRequest
import com.wap.wabi.event.payload.request.EventUpdateRequest
import com.wap.wabi.event.repository.EventBandRepository
import com.wap.wabi.event.repository.EventRepository
import com.wap.wabi.event.repository.EventStudentRepository
import com.wap.wabi.student.fixture.StudentFixture
import com.wap.wabi.student.repository.StudentRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDateTime
import java.util.*

@Transactional
@SpringBootTest
@SuppressWarnings("NonAsciiCharacters")
class EventCommandServiceTest {
    @MockBean
    private lateinit var studentRepository: StudentRepository

    @MockBean
    private lateinit var eventStudentRepository: EventStudentRepository

    @MockBean
    private lateinit var eventRepository: EventRepository

    @MockBean
    private lateinit var eventBandRepository: EventBandRepository

    @MockBean
    private lateinit var bandRepository: BandRepository

    @MockBean
    private lateinit var bandStudentRepository: BandStudentRepository

    @Autowired
    private lateinit var eventCommandService: EventCommandService


    @Test
    fun 이벤트를_생성한다() {
        //Given
        val eventName = "Event 1"
        val eventCreateRequest = EventCreateRequest(
            eventName = eventName,
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusDays(1),
            eventStudentMaxCount = 80,
            bandIds = listOf(1L, 2L, 3L)
        )

        val savedEvent = EventFixture.createEvent(id = 1, name = eventName)

        val band1 = BandFixture.createBand(id = 1, name = "Band 1")
        val band2 = BandFixture.createBand(id = 2, name = "Band 2")
        val band3 = BandFixture.createBand(id = 3, name = "Band 3")

        `when`(eventRepository.save(any())).thenReturn(savedEvent)
        `when`(bandRepository.findAllById(eventCreateRequest.bandIds)).thenReturn(listOf(band1, band2, band3))

        //When
        val result =
            eventCommandService.createEvent(adminId = TestConstants.ADMIN_ID, eventCreateRequest = eventCreateRequest)

        //Then
        assertThat(result.id).isEqualTo(savedEvent.id)
    }

    @Test
    fun 특정_밴드에_속한_학생들을_이벤트에_참여시킨다() {
        //Given
        val event1 = EventFixture.createEvent(id = 1, name = "Event1")
        val band1 = BandFixture.createBand("Band1")
        val student1 = StudentFixture.createStudent("Student1")
        val student2 = StudentFixture.createStudent("Student2")
        val bandStudent1 = BandStudentFixture.createBandStudent(student = student1, band = band1)
        val bandStudent2 = BandStudentFixture.createBandStudent(student = student2, band = band1)
        val eventStudent1 = EventStudentFixture.createEventStudent(event1, student1)
        val eventStudent2 = EventStudentFixture.createEventStudent(event1, student2)

        `when`(bandStudentRepository.findAllByBand(any())).thenReturn(listOf(bandStudent1, bandStudent2))
        `when`(eventStudentRepository.findByStudentAndEvent(student1, event1)).thenReturn(Optional.of(eventStudent1))
        `when`(eventStudentRepository.findByStudentAndEvent(student2, event1)).thenReturn(Optional.of(eventStudent2))

        val expected = 1L

        //When
        val result = eventCommandService.saveEventStudentsFromBand(event = event1, band = band1)

        //Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun 이벤트를_수정한다() {
        //Given
        val eventId: Long = 1
        val originalEventName = "Event 1"
        val newEventName = "Event 2"
        val eventUpdateRequest = EventUpdateRequest(
            eventId = eventId,
            eventName = newEventName,
            startAt = LocalDateTime.now(),
            endAt = LocalDateTime.now().plusDays(1),
            eventStudentMaxCount = 80,
        )

        val savedEvent = EventFixture.createEvent(id = eventId, name = originalEventName)
        val updatedEvent = EventFixture.createEvent(id = eventId, name = newEventName)

        `when`(eventRepository.findById(any())).thenReturn(Optional.of(savedEvent))

        //When
        val result =
            eventCommandService.updateEvent(adminId = TestConstants.ADMIN_ID, eventUpdateRequest = eventUpdateRequest)

        //Then
        assertThat(result.name).isEqualTo(updatedEvent.name)
    }

    @Test
    fun 이벤트를_삭제한다() {
        //Given
        val eventId = 1L
        val event = EventFixture.createEvent(id = eventId, name = "Event 1")

        Mockito.`when`(eventRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(event))

        //When
        eventCommandService.deleteEvent(adminId = TestConstants.ADMIN_ID, eventId = eventId)

        //Then
        Mockito.verify(eventRepository, Mockito.times(1)).delete(event)
    }

    @Test
    fun 이벤트에_체크인_한다() {
        //Given
        val checkInRequest = CheckInRequest(
            studentId = "201912050", eventId = 1
        )

        val event = EventFixture.createEvent(id = 1, name = "Event 1")
        val band = BandFixture.createBand(id = 1, name = "Band 1")
        val student = StudentFixture.createStudent(id = "201912050", name = "Student1")
        val eventStudent = EventStudentFixture.createEventStudent(id = 1, event = event, student = student)

        Mockito.`when`(studentRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(student))
        Mockito.`when`(eventRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(event))
        Mockito.`when`(eventStudentRepository.findByStudentAndEvent(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(Optional.of(eventStudent))

        //When
        val result = eventCommandService.checkIn(checkInRequest)

        //Then
        assertThat(result).isEqualTo(EventStudentStatus.CHECK_IN)
    }


}
