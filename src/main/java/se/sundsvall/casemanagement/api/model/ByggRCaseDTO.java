package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;

import se.sundsvall.casemanagement.api.validation.ByggRCaseFacility;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@ByggRCaseFacility
@Schema(description = "ByggR-cases")
public class ByggRCaseDTO extends CaseDTO implements Serializable {

	private String diaryNumber;

}
