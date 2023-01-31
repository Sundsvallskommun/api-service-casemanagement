package se.sundsvall.casemanagement.integration.db.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CaseMappingId implements Serializable {

    private String externalCaseId;
    private String caseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseMappingId caseMappingId = (CaseMappingId) o;
        return Objects.equals(externalCaseId, caseMappingId.externalCaseId) && Objects.equals(caseId, caseMappingId.caseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(externalCaseId, caseId);
    }
}
