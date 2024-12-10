package se.sundsvall.casemanagement.integration.fb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Data
public class ResponseDto {
	private String statusMeddelande;
	private int statusKod;
	private List<DataItem> data;
	private List<String> fel;
}
