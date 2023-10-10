package se.sundsvall.casemanagement.integration.db.model;

import java.sql.Clob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

import org.hibernate.Length;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Entity
@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
public class CaseEntity {

    @Id
    private String id;

    @Lob
    @Column(length = Length.LONG32)
    private Clob dto;

    @With
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
}
