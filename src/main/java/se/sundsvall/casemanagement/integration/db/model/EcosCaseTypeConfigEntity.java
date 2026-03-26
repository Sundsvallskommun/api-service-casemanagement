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

@Entity
@Data
@Table(name = "ecos_case_type_config")
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EcosCaseTypeConfigEntity {

	@Id
	@Column(name = "case_type_name")
	private String caseTypeName;

	@Column(name = "diary_plan_id")
	private String diaryPlanId;

	@Column(name = "process_type_id")
	private String processTypeId;

	@Column(name = "facility_handler")
	@Enumerated(EnumType.STRING)
	private EcosFacilityHandler facilityHandler;

}
