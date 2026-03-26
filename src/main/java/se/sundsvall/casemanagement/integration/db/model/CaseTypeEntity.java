package se.sundsvall.casemanagement.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casemanagement.api.model.enums.SystemType;

@Entity
@Data
@Table(name = "case_type")
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaseTypeEntity {

	@Id
	@Column(name = "name")
	private String name;

	@Column(name = "system_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private SystemType systemType;

	@Column(name = "nullable_facility_type")
	private boolean nullableFacilityType;

	@Column(name = "nullable_facility")
	private boolean nullableFacility;

	@Column(name = "facility_type_rule")
	private String facilityTypeRule;

}
