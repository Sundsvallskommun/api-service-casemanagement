package se.sundsvall.casemanagement.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "byggr_case_type_config")
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ByggrCaseTypeConfigEntity {

	@Id
	@Column(name = "case_type_name")
	private String caseTypeName;

	@Column(name = "arende_slag")
	private String arendeSlag;

	@Column(name = "arende_grupp")
	private String arendeGrupp;

	@Column(name = "arende_typ")
	private String arendeTyp;

	@Column(name = "handelse_typ")
	private String handelseTyp;

	@Column(name = "handelse_rubrik")
	private String handelseRubrik;

	@Column(name = "handelse_slag")
	private String handelseSlag;

	@Column(name = "arende_mening")
	private String arendeMening;

	@Column(name = "update_handler")
	private String updateHandler;

}
