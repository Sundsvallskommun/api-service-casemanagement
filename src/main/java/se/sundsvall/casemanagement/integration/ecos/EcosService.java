package se.sundsvall.casemanagement.integration.ecos;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_AV_LIVSMEDELSVERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANLAGGNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANORDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_AVHJALPANDEATGARD_FORORENING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_HALSOSKYDDSVERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLATION_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_KOMPOSTERING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_TILLSTAND_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.REGISTRERING_AV_LIVSMEDEL;

import generated.client.party.PartyType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddDocumentsToCaseSvcDto;
import minutmiljo.AddPartyToFacility;
import minutmiljo.AddPartyToFacilitySvcDto;
import minutmiljo.ArrayOfFilterSvcDto;
import minutmiljo.ArrayOfOccurrenceListItemSvcDto;
import minutmiljo.ArrayOfPartySvcDto;
import minutmiljo.ArrayOfSearchCaseResultSvcDto;
import minutmiljo.ArrayOfguid;
import minutmiljo.GetCase;
import minutmiljo.OccurrenceListItemSvcDto;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseSvcDto;
import minutmiljo.SinglePartyRoleFilterSvcDto;
import minutmiljoV2.RegisterDocumentCaseResultSvcDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.EcosCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class EcosService {

	private static final Logger LOG = LoggerFactory.getLogger(EcosService.class);

	private final CaseMappingService caseMappingService;
	private final PartyService partyService;
	private final FbService fbService;
	private final RiskClassService riskClassService;

	private final EcosIntegration ecosIntegration;
	private final EcosObjectFactory ecosObjectFactory;

	public EcosService(
		final CaseMappingService caseMappingService,
		final PartyService partyService,
		final FbService fbService,
		final RiskClassService riskClassService,
		final EcosIntegration ecosIntegration,
		final EcosObjectFactory ecosObjectFactory) {

		this.caseMappingService = caseMappingService;
		this.partyService = partyService;
		this.fbService = fbService;
		this.riskClassService = riskClassService;
		this.ecosIntegration = ecosIntegration;
		this.ecosObjectFactory = ecosObjectFactory;
	}

	public RegisterDocumentCaseResultSvcDto postCase(final EcosCaseDTO caseInput, final String municipalityId) {

		final var eFacility = caseInput.getFacilities().getFirst();
		FbPropertyInfo propertyInfo = null;
		if ((eFacility.getAddress() != null) && (eFacility.getAddress().getPropertyDesignation() != null)) {
			// Collects this early to avoid creating something before we discover potential errors
			propertyInfo = fbService.getPropertyInfoByPropertyDesignation(eFacility.getAddress().getPropertyDesignation());
		}

		// -----> RegisterDocument
		final var registerDocumentResult = ecosObjectFactory.createDocument(caseInput);
		// -----> Search party, Create party if not found and add to case
		List<Map<String, ArrayOfguid>> mapped = List.of();
		if (registerDocumentResult.getCaseId() != null) {
			mapped = partyService.findAndAddPartyToCase(caseInput, registerDocumentResult.getCaseId());
		}
		if (propertyInfo != null) {
			final String facilityGuid = switch (caseInput.getCaseType()) {
				case REGISTRERING_AV_LIVSMEDEL -> ecosObjectFactory.createFoodFacility(caseInput, propertyInfo, registerDocumentResult);
				case ANMALAN_INSTALLATION_VARMEPUMP, ANSOKAN_TILLSTAND_VARMEPUMP -> ecosObjectFactory.createHeatPumpFacility(eFacility.getExtraParameters(), propertyInfo, registerDocumentResult);
				case ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP, ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC,
				     ANMALAN_ANDRING_AVLOPPSANLAGGNING, ANMALAN_ANDRING_AVLOPPSANORDNING -> ecosObjectFactory.createIndividualSewage(eFacility, propertyInfo, registerDocumentResult);
				case ANMALAN_HALSOSKYDDSVERKSAMHET -> ecosObjectFactory.createHealthProtectionFacility(eFacility, propertyInfo, registerDocumentResult);
				case ANMALAN_KOMPOSTERING, ANMALAN_AVHJALPANDEATGARD_FORORENING -> "";
				case ANDRING_AV_LIVSMEDELSVERKSAMHET, INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET -> ""; // Sök facility likt riskklassningen.
				default -> throw Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "CaseType: " + caseInput.getCaseType() + " is not valid. There is a problem in the API validation.");
			};

			// -----> AddPartyToFacility
			if ((facilityGuid != null) && !CaseType.WITH_NULLABLE_FACILITY_TYPE.contains(caseInput.getCaseType())) {
				mapped.forEach(o -> addPartyToFacility(facilityGuid, o));
			}

		} else {
			if (CaseType.UPPDATERING_RISKKLASSNING.toString().equals(caseInput.getCaseType())) {
				try {
					riskClassService.updateRiskClass(caseInput, registerDocumentResult.getCaseId());
				} catch (final Exception e) {
					LOG.warn("Error when updating risk class for case with OpenE-ID: {}", caseInput.getExternalCaseId(), e);
				}
			} else {
				ecosObjectFactory.createOccurrenceOnCase(registerDocumentResult.getCaseId());
			}
		}

		// Persist the connection between OeP-case and Ecos-case
		caseMappingService.postCaseMapping(caseInput, registerDocumentResult.getCaseId(), SystemType.ECOS, municipalityId);
		return registerDocumentResult;
	}

	private void addPartyToFacility(final String foodFacilityGuid, final Map<String, ArrayOfguid> partyRoles) {

		partyRoles.forEach((partyId, roles) -> {
			final AddPartyToFacility addPartyToFacility = new AddPartyToFacility()
				.withModel(new AddPartyToFacilitySvcDto()
					.withFacilityId(foodFacilityGuid)
					.withPartyId(partyId)
					.withRoles(roles));
			ecosIntegration.addPartyToFacility(addPartyToFacility);
		});
	}

	/**
	 * @return CaseStatus from Ecos.
	 * @throws ThrowableProblem NOT_FOUND if no status was found.
	 */
	public CaseStatusDTO getStatus(final String caseId, final String externalCaseId, final String municipalityId) {
		var ecosCase = ecosIntegration.getCase(new GetCase().withCaseId(caseId));

		Optional.ofNullable(ecosCase.getOccurrences())
			.map(ArrayOfOccurrenceListItemSvcDto::getOccurrenceListItemSvcDto)
			.filter(list -> !list.isEmpty())
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND));

		final var latestOccurrence = ecosCase.getOccurrences()
			.getOccurrenceListItemSvcDto()
			.stream()
			.max(Comparator.comparing(OccurrenceListItemSvcDto::getOccurrenceDate))
			.orElse(new OccurrenceListItemSvcDto());

		if (latestOccurrence.getOccurrenceDescription() == null) {
			return null;
		}

		final var caseMapping = Optional.ofNullable(caseMappingService.getCaseMapping(externalCaseId, caseId, municipalityId)
				.getFirst())
			.orElse(new CaseMapping());

		return CaseStatusDTO.builder()
			.withSystem(SystemType.ECOS)
			.withExternalCaseId(externalCaseId)
			.withCaseId(ecosCase.getCaseNumber())
			.withCaseType(caseMapping.getCaseType())
			.withServiceName(caseMapping.getServiceName())
			.withStatus(latestOccurrence.getOccurrenceDescription())
			.withTimestamp(latestOccurrence.getOccurrenceDate()).build();
	}

	public List<CaseStatusDTO> getEcosStatusByLegalId(final String legalId, final PartyType partyType, final String municipalityId) {
		final List<CaseStatusDTO> caseStatuses = new ArrayList<>();

		// Find party both with and without prefix "16"
		final ArrayOfPartySvcDto allParties = partyService.searchPartyByLegalId(legalId, partyType);

		// Search Ecos Case
		if ((allParties.getPartySvcDto() != null) && !allParties.getPartySvcDto().isEmpty()) {

			final ArrayOfSearchCaseResultSvcDto caseResult = new ArrayOfSearchCaseResultSvcDto();

			allParties.getPartySvcDto().forEach(party -> caseResult.getSearchCaseResultSvcDto().addAll(searchCase(party.getId()).getSearchCaseResultSvcDto()));

			// Remove eventual duplicates
			final var caseResultWithoutDuplicates = caseResult.getSearchCaseResultSvcDto().stream().distinct().toList();

			caseResultWithoutDuplicates.forEach(ecosCase -> {
				final List<CaseMapping> caseMappingList = caseMappingService.getCaseMapping(null, ecosCase.getCaseId(), municipalityId);
				final String externalCaseId = caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getExternalCaseId();
				final CaseStatusDTO caseStatusDTO = getStatus(ecosCase.getCaseId(), externalCaseId, municipalityId);

				if (caseStatusDTO != null) {
					caseStatuses.add(caseStatusDTO);
				}
			});
		}

		return caseStatuses;
	}

	public void addDocumentsToCase(final String caseId, final List<AttachmentDTO> attachments) {
		final AddDocumentsToCase addDocumentsToCase = new AddDocumentsToCase();
		final AddDocumentsToCaseSvcDto message = new AddDocumentsToCaseSvcDto();
		message.setCaseId(caseId);
		message.setDocuments(ecosObjectFactory.createArrayOfDocumentSvcDto(attachments));
		message.setOccurrenceTypeId(Constants.ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING);
		message.setDocumentStatusId(Constants.ECOS_DOCUMENT_STATUS_INKOMMEN);
		addDocumentsToCase.setAddDocumentToCaseSvcDto(message);

		ecosIntegration.addDocumentsToCase(addDocumentsToCase);
	}

	private ArrayOfSearchCaseResultSvcDto searchCase(final String partyId) {
		final SearchCase searchCase = new SearchCase();
		final SearchCaseSvcDto searchCaseSvcDto = new SearchCaseSvcDto();
		final ArrayOfFilterSvcDto arrayOfFilterSvcDto = new ArrayOfFilterSvcDto();
		final SinglePartyRoleFilterSvcDto filter = new SinglePartyRoleFilterSvcDto();

		filter.setPartyId(partyId);
		filter.setRoleId(Constants.ECOS_ROLE_ID_VERKSAMHETSUTOVARE);
		arrayOfFilterSvcDto.getFilterSvcDto().add(filter);
		searchCaseSvcDto.setFilters(arrayOfFilterSvcDto);

		searchCase.setModel(searchCaseSvcDto);
		return ecosIntegration.searchCase(searchCase).getSearchCaseResult();
	}

}
