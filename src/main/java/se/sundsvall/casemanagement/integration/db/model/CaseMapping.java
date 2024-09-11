package se.sundsvall.casemanagement.integration.db.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import se.sundsvall.casemanagement.api.model.enums.SystemType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@IdClass(CaseMappingId.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Table(name = "CaseMapping", indexes = {
	@Index(name = "case_mapping_municipality_id_idx", columnList = "municipalityId"),
})
public class CaseMapping {

	@Id
	@Column(unique = true, name = "externalCaseId")
	private String externalCaseId;

	@Id
	@Column(name = "caseId")
	private String caseId;

	@Column(name = "municipalityId")
	private String municipalityId;

	@NotNull
	@Column(nullable = false, name = "system", columnDefinition = "varchar(255)")
	@Enumerated(EnumType.STRING)
	private SystemType system;

	@NotNull
	@Column(name = "caseType", columnDefinition = "varchar(255)")
	private String caseType;

	@Column(name = "serviceName")
	private String serviceName;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(name = "timestamp")
	private LocalDateTime timestamp;

	@PrePersist
	@PreUpdate
	protected void onPersistAndUpdate() {
		timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
	}

}
