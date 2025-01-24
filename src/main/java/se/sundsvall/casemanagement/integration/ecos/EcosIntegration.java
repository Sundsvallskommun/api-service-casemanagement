package se.sundsvall.casemanagement.integration.ecos;

import static org.zalando.problem.Status.BAD_GATEWAY;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;

import java.util.List;
import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddFacilityToCase;
import minutmiljo.AddPartyToCase;
import minutmiljo.AddPartyToFacility;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.CaseSvcDto;
import minutmiljo.CreateFoodFacility;
import minutmiljo.CreateHealthProtectionFacility;
import minutmiljo.CreateHeatPumpFacility;
import minutmiljo.CreateIndividualSewageFacility;
import minutmiljo.CreateOccurrenceOnCase;
import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreateOrganizationPartyResponse;
import minutmiljo.CreatePersonParty;
import minutmiljo.CreatePersonPartyResponse;
import minutmiljo.GetCase;
import minutmiljo.SaveFoodFacility2024RiskClassData;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchFacility;
import minutmiljo.SearchFacilityResultSvcDto;
import minutmiljo.SearchParty;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import minutmiljoV2.RegisterDocumentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;

@Component
public class EcosIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(EcosIntegration.class);

	private final MinutMiljoClient minutMiljoClient;
	private final MinutMiljoClientV2 minutMiljoClientV2;

	public EcosIntegration(final MinutMiljoClient minutMiljoClient, final MinutMiljoClientV2 minutMiljoClientV2) {
		this.minutMiljoClient = minutMiljoClient;
		this.minutMiljoClientV2 = minutMiljoClientV2;
	}

	public RegisterDocumentCaseResultSvcDto registerDocumentV2(final RegisterDocument registerDocument) {
		try {
			LOG.debug("Registering document in Ecos");
			RegisterDocumentResponse response = minutMiljoClientV2.registerDocumentV2(registerDocument);
			RegisterDocumentCaseResultSvcDto document = response.getRegisterDocumentResult();
			if (document == null) {
				throw Problem.valueOf(BAD_GATEWAY, "Case could not be created.");
			}
			LOG.debug("Case created with Ecos case number: {}", document.getCaseNumber());
			return document;
		} catch (Exception e) {
			LOG.error("Could not register document in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not register document in Ecos");
		}
	}

	/**
	 * Search party in Ecos.
	 *
	 * @param searchParty The request to search party - SearchParty
	 * @return The list of parties - ArrayOfPartySvcDto
	 */
	public ArrayOfPartySvcDto searchParty(final SearchParty searchParty) {
		try {
			var model = searchParty.getModel();

			String identificationNumber;
			String type;

			if (model.getOrganizationIdentificationNumber() != null) {
				identificationNumber = model.getOrganizationIdentificationNumber();
				type = "organization";
			} else if (model.getPersonalIdentificationNumber() != null) {
				identificationNumber = model.getPersonalIdentificationNumber();
				type = "person";
			} else {
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Could not search party in Ecos, invalid request");
			}

			LOG.debug("Fetching party with {} number {} and name {}", type, identificationNumber, model.getName());
			ArrayOfPartySvcDto arrayOfPartySvcDto = minutMiljoClient.searchParty(searchParty).getSearchPartyResult();
			if (arrayOfPartySvcDto == null) {
				LOG.debug("Could not find {} party in Ecos", type);
				return new ArrayOfPartySvcDto();
			}
			LOG.debug("Successfully fetched {} party from Ecos", type);

			return arrayOfPartySvcDto;
		} catch (Exception e) {
			LOG.error("Could not search party in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not search party in Ecos");
		}
	}

	public CreateOrganizationPartyResponse createOrganizationParty(final CreateOrganizationParty createOrganizationParty) {
		try {
			LOG.debug("Creating organization party in Ecos");
			var response = minutMiljoClient.createOrganizationParty(createOrganizationParty);
			LOG.debug("Organization party created in Ecos");
			return response;
		} catch (Exception e) {
			LOG.error("Could not create organization party in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create organization party in Ecos");
		}
	}

	public CreatePersonPartyResponse createPersonParty(final CreatePersonParty createPersonParty) {
		try {
			LOG.debug("Creating person party in Ecos");
			var response = minutMiljoClient.createPersonParty(createPersonParty);
			LOG.debug("Person party created in Ecos");
			return response;
		} catch (Exception e) {
			LOG.error("Could not create person party in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create person party in Ecos");
		}
	}

	public void addPartyToFacility(final AddPartyToFacility addPartyToFacility) {
		try {
			LOG.debug("Adding party to facility in Ecos");
			minutMiljoClient.addPartyToFacility(addPartyToFacility);
			LOG.debug("Party added to facility in Ecos");
		} catch (Exception e) {
			LOG.error("Could not add party to facility in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not add party to facility in Ecos");
		}
	}

	public void addPartyToCase(final AddPartyToCase addPartyToCase) {
		try {
			LOG.debug("Adding party to case in Ecos");
			minutMiljoClient.addPartyToCase(addPartyToCase);
			LOG.debug("Party added to case in Ecos");
		} catch (Exception e) {
			LOG.error("Could not add party to case in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not add party to case in Ecos");
		}
	}

	public void createOccurrenceOnCase(final CreateOccurrenceOnCase createOccurrenceOnCase) {
		try {
			LOG.debug("Creating occurrence on case in Ecos");
			minutMiljoClient.createOccurrenceOnCase(createOccurrenceOnCase);
			LOG.debug("Occurrence created on case in Ecos");
		} catch (Exception e) {
			LOG.error("Could not create occurrence on case in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create occurrence on case in Ecos");
		}
	}

	public SearchCaseResponse searchCase(final SearchCase searchCase) {
		try {
			LOG.debug("Searching case in Ecos");
			var response = minutMiljoClient.searchCase(searchCase);
			LOG.debug("{} cases fetched from Ecos", response.getSearchCaseResult().getSearchCaseResultSvcDto().size());
			return response;
		} catch (Exception e) {
			LOG.error("Could not fetch case from Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not fetch case from Ecos");
		}
	}

	public void addDocumentsToCase(final AddDocumentsToCase addDocumentsToCase) {
		try {
			LOG.debug("Adding documents to case in Ecos");
			minutMiljoClient.addDocumentsToCase(addDocumentsToCase);
			LOG.debug("Documents added to case in Ecos");
		} catch (Exception e) {
			LOG.error("Could not add documents to case in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not add documents to case in Ecos");
		}
	}

	/**
	 * Get case from Ecos.
	 *
	 * @param getCase The request to get case - GetCase
	 * @return The case - CaseSvcDto
	 */
	public CaseSvcDto getCase(final GetCase getCase) {
		try {
			LOG.debug("Fetching case {} from Ecos", getCase.getCaseId());
			var caseSvcDto = minutMiljoClient.getCase(getCase).getGetCaseResult();
			if (caseSvcDto == null) {
				throw Problem.valueOf(NOT_FOUND, "Could not fetch case %s from Ecos".formatted(getCase.getCaseId()));
			}
			LOG.debug("Case {} fetched from Ecos", caseSvcDto.getCaseId());
			return caseSvcDto;
		} catch (Exception e) {
			LOG.error("Could not fetch case {} from Ecos", getCase.getCaseId(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not fetch case %s from Ecos".formatted(getCase.getCaseId()));
		}
	}

	/**
	 * Create food facility in Ecos.
	 *
	 * @param createFoodFacility The request to create food facility - CreateFoodFacility
	 * @return The facility identifier - String
	 */
	public String createFoodFacility(final CreateFoodFacility createFoodFacility) {
		try {
			LOG.debug("Creating food facility in Ecos");
			String identifier = minutMiljoClient.createFoodFacility(createFoodFacility).getCreateFoodFacilityResult();
			if (identifier == null) {
				throw Problem.valueOf(BAD_GATEWAY, "Could not create food facility in Ecos");
			}
			LOG.debug("Food facility {} created in Ecos", identifier);
			return identifier;
		} catch (Exception e) {
			var name = createFoodFacility.getCreateFoodFacilitySvcDto().getFacilityCollectionName();
			LOG.error("Could not create food facility {} in Ecos", name, e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create food facility %s in Ecos".formatted(name));
		}
	}

	/**
	 * Create heat pump facility in Ecos.
	 *
	 * @param createHeatPumpFacility The request to create heat pump facility - CreateHeatPumpFacility
	 * @return The facility identifier - String
	 */
	public String createHeatPumpFacility(final CreateHeatPumpFacility createHeatPumpFacility) {
		try {
			LOG.debug("Creating heat pump facility in Ecos");
			String identifier = minutMiljoClient.createHeatPumpFacility(createHeatPumpFacility).getCreateHeatPumpFacilityResult();
			if (identifier == null) {
				throw Problem.valueOf(BAD_GATEWAY, "Could not create heat pump facility in Ecos");
			}
			LOG.debug("Heat pump facility {} created in Ecos", identifier);
			return identifier;
		} catch (Exception e) {
			LOG.error("Could not create heat pump facility in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create heat pump facility in Ecos");
		}
	}

	/**
	 * Create individual sewage facility in Ecos and returns the facility identifier.
	 *
	 * @param createIndividualSewageFacility The request to create individual sewage facility - CreateIndividualSewageFacility
	 * @return The facility identifier - String
	 */
	public String createIndividualSewageFacility(final CreateIndividualSewageFacility createIndividualSewageFacility) {
		try {
			LOG.debug("Creating individual sewage facility in Ecos");
			String identifier = minutMiljoClient.createIndividualSewageFacility(createIndividualSewageFacility).getCreateIndividualSewageFacilityResult();
			if (identifier == null) {
				throw Problem.valueOf(BAD_GATEWAY, "Could not create individual sewage facility in Ecos");
			}
			LOG.debug("Individual sewage facility {} created in Ecos", identifier);
			return identifier;
		} catch (Exception e) {
			LOG.error("Could not create individual sewage facility in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create individual sewage facility in Ecos");
		}
	}

	/**
	 * Create health protection facility in Ecos and returns the facility identifier.
	 *
	 * @param createHealthProtectionFacility The request to create health protection facility - CreateHealthProtectionFacility
	 * @return The facility identifier - String
	 */
	public String createHealthProtectionFacility(final CreateHealthProtectionFacility createHealthProtectionFacility) {
		try {
			LOG.debug("Creating health protection facility in Ecos");
			String identifier = minutMiljoClient.createHealthProtectionFacility(createHealthProtectionFacility).getCreateHealthProtectionFacilityResult();
			if (identifier == null) {
				throw Problem.valueOf(BAD_GATEWAY, "Could not create health protection facility in Ecos");
			}
			LOG.debug("Health protection facility {} created in Ecos", identifier);
			return identifier;
		} catch (Exception e) {
			LOG.error("Could not create health protection facility in Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not create health protection facility in Ecos");
		}
	}

	/**
	 * Add facility to case in Ecos.
	 *
	 * @param addFacilityToCase The request to add facility to case - AddFacilityToCase
	 */
	public void addFacilityToCase(final AddFacilityToCase addFacilityToCase) {
		try {
			LOG.debug("Adding facility {} to case {} in Ecos", addFacilityToCase.getFacilityId(), addFacilityToCase.getCaseId());
			minutMiljoClient.addFacilityToCase(addFacilityToCase);
			LOG.debug("Facility {} added to case {} in Ecos", addFacilityToCase.getFacilityId(), addFacilityToCase.getCaseId());
		} catch (Exception e) {
			LOG.error("Could not add facility {} to case {} in Ecos", addFacilityToCase.getFacilityId(), addFacilityToCase.getCaseId(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not add facility %s to case %s in Ecos".formatted(addFacilityToCase.getFacilityId(), addFacilityToCase.getCaseId()));
		}
	}

	/**
	 * Search for facilities in Ecos.
	 *
	 * @param searchFacility The request to search for facilities - SearchFacility
	 * @return The list of facilities - List<SearchFacilityResultSvcDto>
	 */
	public List<SearchFacilityResultSvcDto> searchFacility(final SearchFacility searchFacility) {
		try {
			LOG.debug("Searching facility in Ecos");
			var facilities = minutMiljoClient.searchFacility(searchFacility)
				.getSearchFacilityResult()
				.getSearchFacilityResultSvcDto();
			if (facilities == null) {
				throw Problem.valueOf(NOT_FOUND, "Could not find facilities in Ecos");
			}
			LOG.debug("{} facilities fetched from Ecos", facilities.size());
			return facilities;
		} catch (Exception e) {
			LOG.error("Could not fetch facility from Ecos", e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not fetch facility from Ecos");
		}
	}

	/**
	 * Update risk class for a food facility in Ecos.
	 *
	 * @param data The request to update risk class for a food facility - SaveFoodFacility2024RiskClassData
	 */
	public void updateRiskClass(final SaveFoodFacility2024RiskClassData data) {
		try {
			LOG.debug("Updating risk class for case {} and facility {} in Ecos", data.getModel().getCaseId(), data.getModel().getFacilityId());
			minutMiljoClient.updateRiskClass(data);
			LOG.debug("Risk class updated for case {} and facility {} in Ecos", data.getModel().getCaseId(), data.getModel().getFacilityId());
		} catch (Exception e) {
			LOG.error("Could not update risk class for case {} and facility {} in Ecos", data.getModel().getCaseId(), data.getModel().getFacilityId(), e);
			throw Problem.valueOf(BAD_GATEWAY, "Could not update risk class in Ecos");
		}
	}

}
