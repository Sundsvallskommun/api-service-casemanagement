package se.sundsvall.casemanagement.integration.fb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GruppItem {
	private Integer adressplatsId;
	private String uuid;
	// Person-/organisationsnummer
	private String identitetsnummer;
}
