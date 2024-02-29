package se.sundsvall.casemanagement.api.model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class OtherCaseDTO extends CaseDTO implements Serializable {

}
