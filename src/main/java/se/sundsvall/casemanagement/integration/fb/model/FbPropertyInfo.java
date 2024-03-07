package se.sundsvall.casemanagement.integration.fb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Data
public class FbPropertyInfo {

	private Integer fnr;

	private Integer adressplatsId;

	public FbPropertyInfo withFnr(final Integer fnr) {
		this.fnr = fnr;
		return this;
	}

	public FbPropertyInfo withAdressplatsId(final Integer adressplatsId) {
		this.adressplatsId = adressplatsId;
		return this;
	}

}
