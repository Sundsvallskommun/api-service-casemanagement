package se.sundsvall.casemanagement.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

@Entity
@Table(name = "execution_information")
public class ExecutionInformationEntity {

	@Id
	@Column(name = "municipality_id", length = 4, nullable = false)
	private String municipalityId;

	@Column(name = "last_successful_execution")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime lastSuccessfulExecution;

	public static ExecutionInformationEntity create() {
		return new ExecutionInformationEntity();
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public ExecutionInformationEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public OffsetDateTime getLastSuccessfulExecution() {
		return lastSuccessfulExecution;
	}

	public void setLastSuccessfulExecution(final OffsetDateTime lastSuccessfulExecution) {
		this.lastSuccessfulExecution = lastSuccessfulExecution;
	}

	public ExecutionInformationEntity withLastSuccessfulExecution(final OffsetDateTime lastSuccessfulExecution) {
		this.lastSuccessfulExecution = lastSuccessfulExecution;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ExecutionInformationEntity that = (ExecutionInformationEntity) o;
		return Objects.equals(municipalityId, that.municipalityId) && Objects.equals(lastSuccessfulExecution, that.lastSuccessfulExecution);
	}

	@Override
	public int hashCode() {
		return Objects.hash(municipalityId, lastSuccessfulExecution);
	}

	@Override
	public String toString() {
		return "ExecutionInformationEntity{municipalityId='%s', lastSuccessfulExecution=%s}".formatted(municipalityId, lastSuccessfulExecution);
	}

}
