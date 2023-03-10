package se.sundsvall.casemanagement.integration.rest.fb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Data
public class FbPropertyInfo {
    private Integer fnr;
    private Integer adressplatsId;
}
