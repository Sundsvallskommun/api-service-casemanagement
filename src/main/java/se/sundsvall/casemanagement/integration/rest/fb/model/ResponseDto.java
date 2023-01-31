package se.sundsvall.casemanagement.integration.rest.fb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Data
public class ResponseDto {
	private String statusMeddelande;
	private int statusKod;
	private List<DataItem> data;
	private List<String> fel;
}