package se.sundsvall.casemanagement.integration.byggr;

import arendeexport.Arende;
import arendeexport.Arende2;
import arendeexport.ArendeFastighet;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfAbstractArendeObjekt2;
import arendeexport.ArrayOfArende1;
import arendeexport.ArrayOfArendeIntressent2;
import arendeexport.ArrayOfString;
import arendeexport.Fastighet;
import arendeexport.GetArende;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.HandlaggareBas;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeResponse2;
import arendeexport.SaveNewHandelse;
import generated.client.oep_integrator.ConfirmDeliveryRequest;
import generated.client.oep_integrator.InstanceType;
import generated.client.party.PartyType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.ByggrCaseTypeConfigRepository;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.ByggrCaseTypeConfigEntity;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;
import se.sundsvall.casemanagement.service.ByggrSystemConfigProvider;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.EnvironmentUtil;
import se.sundsvall.dept44.problem.Problem;

import static generated.client.party.PartyType.ENTERPRISE;
import static generated.client.party.PartyType.PRIVATE;
import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.containsControlOfficial;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.containsPersonDuplicates;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.containsPropertyOwner;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.isWithinPlan;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.parsePropertyDesignation;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.writeEventNote;
import static se.sundsvall.casemanagement.util.Constants.BYGGR;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_ANTECKNING_FASTIGHETSAGARE;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_ANTECKNING_INTRESSENT_KUNDE_INTE_REGISTRERAS;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_ANTECKNING_KONTROLLANSVARIG;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_NR;
import static se.sundsvall.casemanagement.util.Constants.SUNDSVALLS_KOMMUN_BYGGR_KUNDNR;
import static se.sundsvall.casemanagement.util.Constants.SUNDSVALLS_KOMMUN_ORGNR_10;
import static se.sundsvall.casemanagement.util.Constants.SUNDSVALLS_KOMMUN_ORGNR_12;

@Service
public class ByggrService {

	private static final Logger LOG = LoggerFactory.getLogger(ByggrService.class);

	private final FbService fbService;
	private final PartyIntegration partyIntegration;
	private final CaseMappingService caseMappingService;
	private final EnvironmentUtil environmentUtil;

	private final ArendeExportClient arendeExportClient;
	private final OepIntegratorClient oepIntegratorClient;

	private final ByggrCaseTypeConfigRepository byggrCaseTypeConfigRepository;
	private final CaseTypeRepository caseTypeRepository;
	private final CaseRepository caseRepository;
	private final MessagingIntegration messagingIntegration;
	private final ByggrSystemConfigProvider byggrSystemConfigProvider;
	private final Map<String, ByggrUpdateHandler> updateHandlers;

	public ByggrService(final FbService fbService,
		final PartyIntegration partyIntegration,
		final CaseMappingService caseMappingService,
		final EnvironmentUtil environmentUtil,
		final ArendeExportClient arendeExportClient,
		final OepIntegratorClient oepIntegratorClient,
		final ByggrCaseTypeConfigRepository byggrCaseTypeConfigRepository,
		final CaseTypeRepository caseTypeRepository,
		final CaseRepository caseRepository,
		final MessagingIntegration messagingIntegration,
		final ByggrSystemConfigProvider byggrSystemConfigProvider,
		final Map<String, ByggrUpdateHandler> updateHandlers) {
		this.fbService = fbService;
		this.partyIntegration = partyIntegration;
		this.caseMappingService = caseMappingService;
		this.environmentUtil = environmentUtil;
		this.arendeExportClient = arendeExportClient;
		this.oepIntegratorClient = oepIntegratorClient;
		this.byggrCaseTypeConfigRepository = byggrCaseTypeConfigRepository;
		this.caseTypeRepository = caseTypeRepository;
		this.caseRepository = caseRepository;
		this.messagingIntegration = messagingIntegration;
		this.byggrSystemConfigProvider = byggrSystemConfigProvider;
		this.updateHandlers = updateHandlers;
	}

	public void updateByggRCase(final ByggRCaseDTO byggRCase, final String municipalityId) {
		try {
			byggRCase.setMunicipalityId(municipalityId);
			final var updateHandlerName = byggrCaseTypeConfigRepository.findById(byggRCase.getCaseType())
				.map(ByggrCaseTypeConfigEntity::getUpdateHandler)
				.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "CaseType %s not supported for update".formatted(byggRCase.getCaseType())));

			final var handler = Optional.ofNullable(updateHandlers.get(updateHandlerName))
				.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No handler for: " + updateHandlerName));
			handler.handle(byggRCase);

			LOG.info("Successfully updated case with externalCaseId: {}, and municipalityId: {}, and caseType: {}", byggRCase.getExternalCaseId(), municipalityId, byggRCase.getCaseType());

			final var confirmDeliveryRequest = new ConfirmDeliveryRequest().caseId(byggRCase.getExtraParameters().get(ERRAND_NR)).delivered(true).system(BYGGR);
			oepIntegratorClient.confirmDelivery(municipalityId, InstanceType.EXTERNAL, byggRCase.getExternalCaseId(), confirmDeliveryRequest);

			caseRepository.findByIdAndMunicipalityId(byggRCase.getExternalCaseId(), municipalityId).ifPresent(caseRepository::delete);
		} catch (final Exception e) {
			LOG.info("Failed to update case with externalCaseId: {}, and municipalityId: {}, and caseType: {}", byggRCase.getExternalCaseId(), municipalityId, byggRCase.getCaseType());
			final var subject = "Incident from CaseManagement[%s]".formatted(environmentUtil.extractEnvironment());
			final var message = "[%s][BYGGR] Could not update case with externalCaseId: %s. Exception: %s ".formatted(municipalityId, byggRCase.getExternalCaseId(), e.getMessage());
			messagingIntegration.sendSlack(message, municipalityId);
			messagingIntegration.sendMail(subject, message, municipalityId);
		}
	}

	public String extractStakeholderId(final List<StakeholderDTO> stakeholders, final String municipalityId) {
		return ByggrUtil.extractStakeholderId(stakeholders, municipalityId, partyIntegration);
	}

	public SaveNewArendeResponse2 saveNewCase(final ByggRCaseDTO byggRCase, final String municipalityId) {
		byggRCase.setMunicipalityId(municipalityId);
		final Map<String, ByggrCaseTypeConfigEntity> caseTypeMap = new HashMap<>();
		byggrCaseTypeConfigRepository.findAll().forEach(config -> caseTypeMap.put(config.getCaseTypeName(), config));

		final var saveNewArende = ByggrMapper.toSaveNewArende(byggRCase, caseTypeMap.get(byggRCase.getCaseType()));
		saveNewArende.getMessage().setArende(toArende2(byggRCase, caseTypeMap.get(byggRCase.getCaseType())));

		final var response = arendeExportClient.saveNewArende(saveNewArende).getSaveNewArendeResult();

		createOccurrence(byggRCase, saveNewArende, response);
		caseMappingService.postCaseMapping(byggRCase, response.getDnr(), SystemType.BYGGR, municipalityId);
		return response;
	}

	private void createOccurrence(final ByggRCaseDTO caseInput, final SaveNewArende saveNewArende, final SaveNewArendeResponse2 response) {
		final var byggrAdminMessageSb = new StringBuilder();
		// If it's something that we should inform the administrator about, we create a new occurrence in the case.
		if (containsControlOfficial(caseInput.getStakeholders())) {
			writeEventNote(BYGGR_HANDELSE_ANTECKNING_KONTROLLANSVARIG, byggrAdminMessageSb);
		}
		if (containsPersonDuplicates(caseInput.getStakeholders())) {
			writeEventNote(BYGGR_HANDELSE_ANTECKNING_INTRESSENT_KUNDE_INTE_REGISTRERAS, byggrAdminMessageSb);
		}
		if (!containsPropertyOwner(saveNewArende.getMessage().getArende().getIntressentLista().getIntressent())) {
			writeEventNote(BYGGR_HANDELSE_ANTECKNING_FASTIGHETSAGARE, byggrAdminMessageSb);
		}
		if (!byggrAdminMessageSb.isEmpty()) {
			writeEventNote(BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT, byggrAdminMessageSb);
			arendeExportClient.saveNewHandelse(ByggrMapper.toSaveNewManuellHanteringHandelse(response.getDnr(), byggrAdminMessageSb.toString()));
		}
	}

	public void saveNewIncomingAttachmentHandelse(final String dnr, final List<AttachmentDTO> attachmentDTOList) {
		final var saveNewHandelse = new SaveNewHandelse()
			.withMessage(ByggrMapper.toSaveNewHandelseMessage(dnr, attachmentDTOList));
		arendeExportClient.saveNewHandelse(saveNewHandelse);
	}

	public CaseStatusDTO toByggrStatus(final CaseMapping caseMapping) {
		final var getArendeResponse = arendeExportClient.getArende(new GetArende().withDnr(caseMapping.getCaseId()));
		return ByggrMapper.toByggrStatus(getArendeResponse.getGetArendeResult(), caseMapping.getExternalCaseId(), List.of(caseMapping), byggrSystemConfigProvider);
	}

	public CaseStatusDTO toByggrStatus(final Arende arende, final String externalCaseId, final String municipalityId) {
		final var caseMappingList = caseMappingService.getCaseMapping(externalCaseId, arende.getDnr(), municipalityId);
		return ByggrMapper.toByggrStatus(arende, externalCaseId, caseMappingList, byggrSystemConfigProvider);
	}

	@Async
	public CompletableFuture<List<CaseStatusDTO>> getByggrStatusByLegalId(final String legalId, final PartyType partyType, final String municipalityId) {
		final var getRelateradeArendenByPersOrgNrAndRoleInput = new GetRelateradeArendenByPersOrgNrAndRole()
			.withPersOrgNr(legalId)
			.withArendeIntressentRoller(new ArrayOfString().withString(StakeholderRole.APPLICANT.getText()))
			.withHandelseIntressentRoller(new ArrayOfString().withString(StakeholderRole.APPLICANT.getText()));

		var arrayOfByggrArenden = new ArrayOfArende1();

		if (partyType.equals(ENTERPRISE)) {
			arrayOfByggrArenden = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();
		}

		// If no cases are found, try to fetch cases with formatted legal id
		if (arrayOfByggrArenden == null || arrayOfByggrArenden.getArende().isEmpty()) {
			getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(CaseUtil.getFormattedLegalId(partyType, legalId));
			arrayOfByggrArenden = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();
		}

		final var statusList = Optional.ofNullable(arrayOfByggrArenden.getArende()).orElse(emptyList()).stream().map(byggrArende -> {
			final var caseMappingList = caseMappingService.getCaseMapping(null, byggrArende.getDnr(), municipalityId);

			return toByggrStatus(byggrArende, Optional.ofNullable(caseMappingList)
				.filter(list -> !list.isEmpty())
				.map(list -> list.getFirst().getExternalCaseId())
				.orElse(null), municipalityId);
		}).toList();

		return CompletableFuture.completedFuture(statusList);
	}

	public ArrayOfArendeIntressent2 getByggrIntressenter(final ByggRCaseDTO byggRCase) {

		// Add all stakeholders from a case to the list
		final var stakeholders = new ArrayList<>(byggRCase.getStakeholders());
		populateStakeholderListWithPropertyOwners(byggRCase, stakeholders);
		final var personIds = ByggrMapper.filterPersonId(stakeholders);

		return new ArrayOfArendeIntressent2().withIntressent(stakeholders.stream().filter(dto -> !dto.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString()))
			.map(stakeholderDTO -> {

				final var intressent = new ArendeIntressent();

				// Check if this is Sundsvalls kommun as the property owner
				if (isSundsvallsKommunPropertyOwner(stakeholderDTO)) {
					// Use intressentId and intressentVersionId from ByggR instead of persOrgNr
					setSundsvallsKommunIntressentFields(intressent, stakeholderDTO);
				} else {
					ByggrMapper.setStakeholderFields(stakeholderDTO, personIds, intressent);
					if (stakeholderDTO.getAddresses() != null) {
						ByggrMapper.toAdressDTos(stakeholderDTO, intressent);
					}
					intressent.withIntressentKommunikationLista(ByggrMapper.toByggrContactInfo(stakeholderDTO, intressent.getAttention()));
				}

				intressent.withRollLista(ByggrMapper.toArrayOfRoles(stakeholderDTO));
				return intressent;
			})
			.filter(intressent -> StringUtils.isNotBlank(intressent.getPersOrgNr()) || (intressent.getIntressentId() != null && intressent.getIntressentVersionId() != null))
			.toList());
	}

	public void populateStakeholderListWithPropertyOwners(final ByggRCaseDTO byggRCase, final List<StakeholderDTO> stakeholders) {
		// Filter all persons
		final var persons = stakeholders.stream()
			.filter(PersonDTO.class::isInstance)
			.map(obj -> {
				final var personOjb = (PersonDTO) obj;
				personOjb.setPersonalNumber(getPersonalNumber(personOjb, byggRCase.getMunicipalityId()));
				return personOjb;
			})
			.toList();

		// Filter all organizations
		final var organizations = stakeholders.stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(obj -> {
				final var orgObj = (OrganizationDTO) obj;
				orgObj.setOrganizationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(orgObj.getOrganizationNumber()));
				return orgObj;
			})
			.toList();
		// Loop through each facility and get the property owners for each one
		byggRCase.getFacilities().forEach(facility -> {
			final var propertyOwners = fbService.getPropertyOwnerByPropertyDesignation(facility.getAddress().getPropertyDesignation());
			ByggrMapper.populateStakeholderListWithPropertyOwnerPersons(persons, stakeholders, propertyOwners);
			ByggrMapper.populateStakeholderListWithPropertyOwnerOrganizations(organizations, stakeholders, propertyOwners);
		});
	}

	/**
	 * Check if a stakeholder is Sundsvalls kommun and has the property owner role
	 *
	 * @param  stakeholder The stakeholder to check
	 * @return             true if it's Sundsvalls kommun with the property owner role
	 */
	private boolean isSundsvallsKommunPropertyOwner(final StakeholderDTO stakeholder) {
		if (!(stakeholder instanceof final OrganizationDTO orgDto)) {
			return false;
		}

		if (!stakeholder.getRoles().contains(StakeholderRole.PROPERTY_OWNER.toString())) {
			return false;
		}

		final var orgNr = orgDto.getOrganizationNumber();
		return SUNDSVALLS_KOMMUN_ORGNR_10.equals(orgNr) ||
			SUNDSVALLS_KOMMUN_ORGNR_12.equals(orgNr);
	}

	/**
	 * Set intressent fields for Sundsvalls kommun by fetching from ByggR using kundnummer
	 *
	 * @param intressent  The intressent to populate
	 * @param stakeholder The stakeholder data
	 */
	private void setSundsvallsKommunIntressentFields(final ArendeIntressent intressent, final StakeholderDTO stakeholder) {
		try {
			final var intressentData = getSundsvallsKommunIntressentFromByggr();
			if (intressentData != null) {
				intressent.setIntressentId(intressentData.getIntressentId());
				intressent.setIntressentVersionId(intressentData.getIntressentVersionId());
				LOG.debug("Using ByggR intressent for Sundsvalls kommun: intressentId={}, intressentVersionId={}",
					intressentData.getIntressentId(), intressentData.getIntressentVersionId());
			} else {
				LOG.warn("Could not fetch Sundsvalls kommun intressent from ByggR, falling back to organization number");
				// Fallback to using organization number
				fallbackToOrgNr(intressent, stakeholder);
			}
		} catch (final Exception e) {
			LOG.error("Error fetching Sundsvalls kommun intressent from ByggR, falling back to organization number", e);
			// Fallback to using organization number
			fallbackToOrgNr(intressent, stakeholder);
		}
	}

	private void fallbackToOrgNr(final ArendeIntressent intressent, final StakeholderDTO stakeholder) {
		if (stakeholder instanceof final OrganizationDTO orgDto) {
			intressent.setPersOrgNr(orgDto.getOrganizationNumber());
		}
	}

	/**
	 * Fetch Sundsvalls kommun intressent details from ByggR using GetIntressent operation
	 *
	 * @return The intressent from ByggR, or null if not found
	 */
	private arendeexport.Intressent getSundsvallsKommunIntressentFromByggr() {
		final var getIntressent = new arendeexport.GetIntressent()
			.withMessage(new arendeexport.GetIntressentMessage()
				.withHandlaggarSign(BYGGR_SYSTEM_HANDLAGGARE_SIGN)
				.withKundNr(SUNDSVALLS_KOMMUN_BYGGR_KUNDNR)
				.withStatusFilter(arendeexport.StatusFilter.AKTIV));

		final var response = arendeExportClient.getIntressent(getIntressent);

		if (response != null &&
			response.getGetIntressentResult() != null &&
			response.getGetIntressentResult().getIntressent() != null &&
			!response.getGetIntressentResult().getIntressent().getIntressent().isEmpty()) {
			return response.getGetIntressentResult().getIntressent().getIntressent().getFirst();
		}

		return null;
	}

	private String getPersonalNumber(final PersonDTO personDTOStakeholder, final String municipalityId) {
		String pnr = partyIntegration.getLegalIdByPartyId(municipalityId, personDTOStakeholder.getPersonId()).get(PRIVATE);
		if ((pnr != null) && (pnr.length() == 12)) {
			pnr = pnr.substring(0, 8) + "-" + pnr.substring(8);
		}
		return pnr;
	}

	public ArrayOfAbstractArendeObjekt2 getByggrArendeObjektLista(final ByggRCaseDTO byggRCase) {

		final var arendeObjektLista = new ArrayOfAbstractArendeObjekt2();

		final var usedPropertyDesignations = new ArrayList<String>();
		byggRCase.getFacilities().forEach(facilityDTO -> {
			if (usedPropertyDesignations.contains(facilityDTO.getAddress().getPropertyDesignation())) {
				// If we already have created an "arendeFastighet" with the same propertyDesignation,
				// we should not create a duplicate. Skip this iteration.
				return;
			}

			final var arendeFastighet = new ArendeFastighet()
				.withArHuvudObjekt(facilityDTO.isMainFacility())
				.withFastighet(new Fastighet()
					.withFnr(fbService.getPropertyInfoByPropertyDesignation(facilityDTO.getAddress().getPropertyDesignation()).getFnr()));

			arendeObjektLista.getAbstractArendeObjekt().add(arendeFastighet);
			usedPropertyDesignations.add(facilityDTO.getAddress().getPropertyDesignation());
		});

		return arendeObjektLista;
	}

	Arende2 toArende2(final ByggRCaseDTO byggRCase, final ByggrCaseTypeConfigEntity caseTypeData) {
		final var arende = new Arende2();

		if ((byggRCase.getFacilities() == null) || (byggRCase.getFacilities().getFirst() == null)) {
			arende.withArendeslag(caseTypeData.getArendeSlag());

		} else if (caseTypeData.getArendeSlag() != null) {
			arende.withArendeslag(caseTypeData.getArendeSlag());
			if (!caseTypeRepository.findById(byggRCase.getCaseType()).map(ct -> ct.isNullableFacilityType()).orElse(false)) {
				arende.withArendeklass(ByggrMapper.getArendeKlass(byggRCase.getFacilities()));
			}
		} else {
			arende.withArendeslag(ByggrMapper.getMainOrOnlyArendeslag(byggRCase.getFacilities()));
		}

		return arende
			.withArendegrupp(caseTypeData.getArendeGrupp())
			.withArendetyp(caseTypeData.getArendeTyp())
			.withNamndkod(BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN)
			.withEnhetkod(BYGGR_ENHETKOD_STADSBYGGNADSKONTORET)
			.withKommun(BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN)
			.withHandlaggare(new HandlaggareBas().withSignatur(BYGGR_SYSTEM_HANDLAGGARE_SIGN))
			.withArInomplan(isWithinPlan(byggRCase.getFacilities()))
			.withBeskrivning(ByggrMapper.getArendeBeskrivning(byggRCase, caseTypeData.getArendeMening()))
			.withIntressentLista(getByggrIntressenter(byggRCase))
			.withObjektLista(getByggrArendeObjektLista(byggRCase))
			.withAnkomstDatum(LocalDate.now())
			// ProjektNummer/FakturaId in ByggR.
			.withProjektnr(Optional.ofNullable(ByggrMapper.getInvoiceMarking(byggRCase))
				.orElse(parsePropertyDesignation(byggRCase.getFacilities())));
	}

}
