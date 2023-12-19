package se.sundsvall.casemanagement.integration.db.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

@Entity(name = "CaseMapping")
@IdClass(CaseMappingId.class)
@Data
@NoArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class CaseMapping {

	@Id
	@Column(unique = true, name = "externalCaseId")
	private String externalCaseId;

	@Id
	@Column(name = "caseId")
	private String caseId;

	@NotNull
	@Column(nullable = false, name = "system", columnDefinition = "varchar(255)")
	@Enumerated(EnumType.STRING)
	private SystemType system;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "caseType", columnDefinition = "varchar(255)")
	private CaseType caseType;

	@Column(name = "serviceName")
	private String serviceName;

	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(name = "timestamp")
	private LocalDateTime timestamp;

	public CaseMapping(String externalCaseId, String caseId, SystemType system, CaseType caseType, String serviceName) {
		this.externalCaseId = externalCaseId;
		this.caseId = caseId;
		this.system = system;
		this.caseType = caseType;
		this.serviceName = serviceName;
	}

	@PrePersist
	@PreUpdate
	protected void onPersistAndUpdate() {
		timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final CaseMapping that)) {
			return false;
		}
		return Objects.equals(externalCaseId, that.externalCaseId) && Objects.equals(caseId, that.caseId) && (system == that.system) && (caseType == that.caseType) && Objects.equals(serviceName, that.serviceName) && Objects.equals(timestamp,
			that.timestamp);
	}

	@Override
	public int hashCode() {
		return Objects.hash(externalCaseId, caseId, system, caseType, serviceName, timestamp);
	}
}
