package se.sundsvall.casemanagement.integration.citizenmapping.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
@Data
public class CitizenMappingErrorResponse {

	private String type;
	private String title;
	private long status;
	private String traceId;
	private Errors errors;

	@Data
	public static class Errors {
		private String[] personId;
	}
}
