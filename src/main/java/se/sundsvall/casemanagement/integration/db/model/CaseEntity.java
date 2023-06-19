package se.sundsvall.casemanagement.integration.db.model;

import java.sql.Clob;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Lob;

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
    private Clob dto;
    @With
    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;
}
