package se.sundsvall.casemanagement.service;

import java.util.Objects;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casemanagement.integration.citizenmapping.CitizenMappingClient;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class CitizenMappingService {

    private final CitizenMappingClient citizenMappingClient;

    public CitizenMappingService(CitizenMappingClient citizenMappingClient) {
        this.citizenMappingClient = citizenMappingClient;
    }

    /**
     * Returns null if personId is null
     */
    public String getPersonalNumber(String personId) {
        String personalNumber = null;

        if (personId != null && !personId.isBlank()) {
            try {
                personalNumber = citizenMappingClient.getPersonalNumber(personId);
            } catch (ThrowableProblem e) {
                if (Objects.equals(e.getStatus(), Status.NOT_FOUND)) {
                    throw Problem.valueOf(Status.BAD_REQUEST, Constants.ERR_MSG_PERSONAL_NUMBER_NOT_FOUND_WITH_PERSON_ID(personId), null);
                } else {
                    throw e;
                }
            }
        }
        return personalNumber;
    }

}
