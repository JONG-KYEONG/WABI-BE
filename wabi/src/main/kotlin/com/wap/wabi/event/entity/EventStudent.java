package com.wap.wabi.event.entity;

import com.wap.wabi.event.entity.Enum.EventStudentStatus;
import com.wap.wabi.student.entity.Student;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class EventStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;
    @ManyToOne(fetch = FetchType.LAZY)
    private Student student;
    private String club;
    @Enumerated(EnumType.STRING)
    private EventStudentStatus status;
    private LocalDateTime updatedAt;
    private LocalDateTime checkedInAt;

    private EventStudent(builder builder) {
        this.id = builder.id;
        this.event = builder.event;
        this.student = builder.student;
        this.club = builder.club;
        this.status = builder.status;
        this.updatedAt = builder.updatedAt;
        this.checkedInAt = builder.checkedInAt;
    }

    public static class builder {
        private Long id;
        private Event event;
        private Student student;
        private String club;
        private EventStudentStatus status;
        private LocalDateTime updatedAt;
        private LocalDateTime checkedInAt;

        public builder setId(Long id) {
            this.id = id;
            return this;
        }

        public builder setEvent(Event event) {
            this.event = event;
            return this;
        }

        public builder setStudent(Student student) {
            this.student = student;
            return this;
        }

        public builder setClub(String club) {
            this.club = club;
            return this;
        }

        public builder setStatus(EventStudentStatus status) {
            this.status = status;
            return this;
        }

        public builder setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public builder setCheckedInAt(LocalDateTime checkedInAt) {
            this.checkedInAt = checkedInAt;
            return this;
        }

        public EventStudent build() {
            return new EventStudent(this);
        }
    }

    public EventStudent() {
    }

    public void checkIn() {
        this.status = EventStudentStatus.CHECK_IN;
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public Student getStudent() {
        return student;
    }

    public EventStudentStatus getStatus() {
        return status;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

}
