package com.tdoodle.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Data
@Entity
@Table(name = "meeting")
public class Meeting {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "slot_id")
    TimeSlot timeSlot;
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "participants", columnDefinition = "jsonb")
    List<String> participants;
    @Id
    @Column(name = "meeting_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId;
    private String title;
    private String description;
}