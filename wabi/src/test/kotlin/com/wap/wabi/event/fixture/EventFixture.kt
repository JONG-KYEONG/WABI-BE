package com.wap.wabi.event.fixture

import com.wap.wabi.common.Reflection
import com.wap.wabi.common.TestConstants
import com.wap.wabi.event.entity.Event
import java.time.LocalDateTime

object EventFixture {
    fun createEvent(name: String, id: Long = 1): Event {
        val event = Event.builder()
            .adminId(TestConstants.ADMIN_ID)
            .name(name)
            .startAt(LocalDateTime.now())
            .endAt(LocalDateTime.now().plusDays(1))
            .eventStudentMaxCount(0)
            .build()
        return Reflection.makeIdChangedClone(Event::class.java, event, id)
    }
}
