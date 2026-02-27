package se.sundsvall.casemanagement.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

@Entity
@Table(name = "execution_information", uniqueConstraints = {
	@UniqueConstraint(name = "uq_execution_information_municipality_job", columnNames = {
		"municipality_id", "job_name"
	})
})
public class ExecutionInformationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "municipality_id", length = 4, nullable = false)
	private String municipalityId;

	@Column(name = "job_name", length = 50, nullable = false)
	private String jobName;

	@Column(name = "last_successful_execution")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime lastSuccessfulExecution;

	public static ExecutionInformationEntity create() {
		return new ExecutionInformationEntity();
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public ExecutionInformationEntity withId(final Long id) {
		this.id = id;
		return this;
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

	public String getJobName() {
		return jobName;
	}

	public void setJobName(final String jobName) {
		this.jobName = jobName;
	}

	public ExecutionInformationEntity withJobName(final String jobName) {
		this.jobName = jobName;
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
		return Objects.equals(id, that.id)
			&& Objects.equals(municipalityId, that.municipalityId)
			&& Objects.equals(jobName, that.jobName)
			&& Objects.equals(lastSuccessfulExecution, that.lastSuccessfulExecution);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, municipalityId, jobName, lastSuccessfulExecution);
	}

	@Override
	public String toString() {
		return "ExecutionInformationEntity{id=%d, municipalityId='%s', jobName='%s', lastSuccessfulExecution=%s}".formatted(id, municipalityId, jobName, lastSuccessfulExecution);
	}

}
