package com.wap.wabi.event.service

import com.wap.wabi.band.fixture.BandFixture
import com.wap.wabi.band.repository.BandRepository
import com.wap.wabi.band.repository.BandStudentRepository
import com.wap.wabi.common.TestConstants
import com.wap.wabi.event.fixture.EventBandFixture
import com.wap.wabi.event.fixture.EventFixture
import com.wap.wabi.event.payload.response.CheckInStatusCount
import com.wap.wabi.event.payload.response.EventData
import com.wap.wabi.event.repository.EventBandRepository
import com.wap.wabi.event.repository.EventRepository
import com.wap.wabi.event.repository.EventStudentRepository
import com.wap.wabi.student.repository.StudentRepository
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.*

@Transactional
@SpringBootTest
@SuppressWarnings("NonAsciiCharacters")
class EventQueryServiceTest {
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
    private lateinit var eventQueryService: EventQueryService

    @Test
    fun 이벤트를_단일조회_한다() {
        //Given
        val eventId = 1L
        val event = EventFixture.createEvent(id = eventId, name = "Event1")
        val band1 = BandFixture.createBand(id = 1, name = "Band 1")
        val band2 = BandFixture.createBand(id = 2, name = "Band 2")
        val band3 = BandFixture.createBand(id = 3, name = "Band 3")

        val eventBand1 = EventBandFixture.createEventBnd(event, band1)
        val eventBand2 = EventBandFixture.createEventBnd(event, band2)
        val eventBand3 = EventBandFixture.createEventBnd(event, band3)
        val eventBands = listOf(eventBand1, eventBand2, eventBand3)

        val checkInStatusCount = CheckInStatusCount(checkIn = 20, notCheckIn = 20)

        Mockito.`when`(eventRepository.findById(ArgumentMatchers.any())).thenReturn(Optional.of(event))
        Mockito.`when`(eventBandRepository.findAllByEvent(ArgumentMatchers.any())).thenReturn(eventBands)
        Mockito.`when`(
            eventStudentRepository.getEventStudentStatusCount(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(20)

        val expected = EventData.of(event, eventBands, checkInStatusCount)

        //When
        val result = eventQueryService.getEvent(adminId = TestConstants.ADMIN_ID, eventId = eventId)

        //Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun 이벤트를_목록으로_조회_한다() {
        //Given
        val event1 = EventFixture.createEvent(id = 1, name = "Event 1")
        val event2 = EventFixture.createEvent(id = 2, name = "Event 2")
        val band1 = BandFixture.createBand(id = 1, name = "Band 1")
        val band2 = BandFixture.createBand(id = 2, name = "Band 2")
        val band3 = BandFixture.createBand(id = 3, name = "Band 3")

        val eventBand1 = EventBandFixture.createEventBnd(event1, band1)
        val eventBand2 = EventBandFixture.createEventBnd(event1, band2)
        val eventBand3 = EventBandFixture.createEventBnd(event2, band2)
        val eventBand4 = EventBandFixture.createEventBnd(event2, band3)

        val checkInStatusCount = CheckInStatusCount(checkIn = 20, notCheckIn = 20)

        val eventData1 = EventData.of(event1, listOf(eventBand1, eventBand2), checkInStatusCount)
        val eventData2 = EventData.of(event2, listOf(eventBand3, eventBand4), checkInStatusCount)

        Mockito.`when`(eventRepository.findAllByAdminId(ArgumentMatchers.any())).thenReturn(listOf(event1, event2))
        Mockito.`when`(eventBandRepository.findAllByEvent(event1)).thenReturn(listOf(eventBand1, eventBand2))
        Mockito.`when`(eventBandRepository.findAllByEvent(event2)).thenReturn(listOf(eventBand3, eventBand4))
        Mockito.`when`(
            eventStudentRepository.getEventStudentStatusCount(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(20)
        Mockito.`when`(eventRepository.findById(1L)).thenReturn(Optional.of(event1))
        Mockito.`when`(eventRepository.findById(2L)).thenReturn(Optional.of(event2))
        Mockito.`when`(
            eventStudentRepository.getEventStudentStatusCount(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(20)

        //When
        val result = eventQueryService.getEvents(TestConstants.ADMIN_ID)

        //Then
        assertThat(result).isEqualTo(listOf(eventData1, eventData2))
    }


    @Test
    fun 이벤트의_주최자인지_판단한다() {
        //Given
        val event1 = EventFixture.createEvent(id = 1, name = "Event1")

        //When
        val result = eventQueryService.validateEventOwner(adminId = TestConstants.ADMIN_ID, event = event1)

        //Then
        Assertions.assertThat(result).isTrue()
    }
}