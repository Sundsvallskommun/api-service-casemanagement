package se.sundsvall.casemanagement.integration.db.model;

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
@Table(name = "casetypedata")
@Builder(setterPrefix = "with")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaseTypeData {

	@Id
	private String value;
	private String arendeSlag;
	private String arendeGrupp;
	private String arendeTyp;
	private String handelseTyp;
	private String handelseRubrik;
	private String handelseSlag;
	private String arendeMening;
}
