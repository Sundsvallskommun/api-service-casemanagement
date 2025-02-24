package se.sundsvall.casemanagement.integration.byggr;

import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.WITH_NULLABLE_FACILITY_TYPE;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createAddAdditionalDocumentsHandelse;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createAddAdditionalDocumentsHandelseIntressent;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createAddCertifiedInspectorHandelse;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createAddCertifiedInspectorHandelseIntressent;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createAlertCaseManagerEvent;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createArrayOfHandling;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.createNeighborhoodNotificationArrayOfHandling;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.extractEvent;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.extractIntressentFromEvent;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.filterPersonId;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.getArendeBeskrivning;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.getArendeKlass;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.getInvoiceMarking;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.getMainOrOnlyArendeslag;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.populateStakeholderListWithPropertyOwnerOrganizations;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.populateStakeholderListWithPropertyOwnerPersons;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.setStakeholderFields;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.toAdressDTos;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.toArrayOfRoles;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.toByggrContactInfo;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.toSaveNewArende;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.toSaveNewHandelseMessage;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.toSaveNewManuellHanteringHandelse;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.containsControlOfficial;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.containsPersonDuplicates;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.containsPropertyOwner;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.isWithinPlan;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.parsePropertyDesignation;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.writeEventNote;
import static se.sundsvall.casemanagement.util.Constants.BYGGR;
import static se.sundsvall.casemanagement.util.Constants.COMMENT;
import static se.sundsvall.casemanagement.util.Constants.DONE;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_INFORMATION;
import static se.sundsvall.casemanagement.util.Constants.ERRAND_NR;
import static se.sundsvall.casemanagement.util.Constants.EVENT_CATEGORY;
import static se.sundsvall.casemanagement.util.Constants.OTHER_INFORMATION;
import static se.sundsvall.casemanagement.util.Constants.SYSTEM;

import arendeexport.Arende;
import arendeexport.Arende2;
import arendeexport.ArendeFastighet;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfAbstractArendeObjekt2;
import arendeexport.ArrayOfArendeIntressent2;
import arendeexport.ArrayOfString;
import arendeexport.Fastighet;
import arendeexport.GetArende;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.GetRemisserByPersOrgNr;
import arendeexport.HandelseIntressent;
import arendeexport.HandlaggareBas;
import arendeexport.Remiss;
import arendeexport.RemissStatusFilter;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeResponse2;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseMessage;
import arendeexport.SaveNewRemissvar;
import arendeexport.SaveNewRemissvarMessage;
import generated.client.party.PartyType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.CaseTypeDataRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;
import se.sundsvall.casemanagement.integration.messaging.MessagingIntegration;
import se.sundsvall.casemanagement.integration.opene.OpenEIntegration;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CitizenService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.service.util.LegalIdUtility;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;
import se.sundsvall.casemanagement.util.EnvironmentUtil;

@Service
public class ByggrService {

	private static final Logger LOG = LoggerFactory.getLogger(ByggrService.class);

	private final FbService fbService;
	private final CitizenService citizenService;
	private final CaseMappingService caseMappingService;
	private final EnvironmentUtil environmentUtil;

	private final ArendeExportClient arendeExportClient;
	private final OpenEIntegration openEIntegration;

	private final CaseTypeDataRepository caseTypeDataRepository;
	private final CaseRepository caseRepository;
	private final MessagingIntegration messagingIntegration;

	public ByggrService(final FbService fbService,
		final CitizenService citizenService,
		final CaseMappingService caseMappingService,
		final EnvironmentUtil environmentUtil,
		final ArendeExportClient arendeExportClient,
		final OpenEIntegration openEIntegration,
		final CaseTypeDataRepository caseTypeDataRepository,
		final CaseRepository caseRepository,
		final MessagingIntegration messagingIntegration) {
		this.fbService = fbService;
		this.citizenService = citizenService;
		this.caseMappingService = caseMappingService;
		this.environmentUtil = environmentUtil;
		this.arendeExportClient = arendeExportClient;
		this.openEIntegration = openEIntegration;
		this.caseTypeDataRepository = caseTypeDataRepository;
		this.caseRepository = caseRepository;
		this.messagingIntegration = messagingIntegration;
	}

	public void updateByggRCase(final ByggRCaseDTO byggRCase, final String municipalityId) {
		try {
			switch (byggRCase.getCaseType()) {
				case "NEIGHBORHOOD_NOTIFICATION" -> respondToNeighborhoodNotification(byggRCase);
				case "BYGGR_ADD_CERTIFIED_INSPECTOR" -> addCertifiedInspector(byggRCase);
				case "BYGGR_ADDITIONAL_DOCUMENTS" -> addAdditionalDocuments(byggRCase);
				default -> throw Problem.valueOf(BAD_REQUEST, "CaseType %s not supported".formatted(byggRCase.getCaseType()));
			}
			LOG.info("Successfully updated case with externalCaseId: {}, and municipalityId: {}, and caseType: {}", byggRCase.getExternalCaseId(), municipalityId, byggRCase.getCaseType());
			openEIntegration.confirmDelivery(byggRCase.getExternalCaseId(), BYGGR, byggRCase.getExtraParameters().get(ERRAND_NR));
			caseRepository.findByIdAndMunicipalityId(byggRCase.getExternalCaseId(), municipalityId).ifPresent(caseRepository::delete);
		} catch (Exception e) {
			LOG.info("Failed to update case with externalCaseId: {}, and municipalityId: {}, and caseType: {}", byggRCase.getExternalCaseId(), municipalityId, byggRCase.getCaseType());
			final var subject = "Incident from CaseManagement[%s]".formatted(environmentUtil.extractEnvironment());
			final var message = "[%s][BYGGR] Could not update case with externalCaseId: %s. Exception: %s ".formatted(municipalityId, byggRCase.getExternalCaseId(), e.getMessage());
			messagingIntegration.sendSlack(subject, message);
			messagingIntegration.sendMail(subject, message, municipalityId);
		}
	}

	public void addAdditionalDocuments(final ByggRCaseDTO byggRCase) {
		var stakeholder = byggRCase.getStakeholders().stream()
			.max(Comparator.comparing(StakeholderDTO::getType))
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No stakeholder found in the incoming request."));
		var stakeholderId = extractStakeholderId(byggRCase.getStakeholders());
		var otherInformation = byggRCase.getExtraParameters().get(OTHER_INFORMATION);
		var errandNr = byggRCase.getExtraParameters().get(ERRAND_NR);
		var handelseslag = byggRCase.getExtraParameters().get(EVENT_CATEGORY);

		var handelseIntressent = createAddAdditionalDocumentsHandelseIntressent(stakeholder, stakeholderId);
		var newHandelse = createAddAdditionalDocumentsHandelse(otherInformation, handelseIntressent, handelseslag);
		var arrayOfHandling = createArrayOfHandling(byggRCase);

		var saveNewHandelse = new SaveNewHandelse()
			.withMessage(new SaveNewHandelseMessage()
				.withDnr(errandNr)
				.withHandlaggarSign(SYSTEM)
				.withHandelse(newHandelse)
				.withHandlingar(arrayOfHandling)
				.withAnkomststamplaHandlingar(false)
				.withAutoGenereraBeslutNr(false)
				.withAnkomststamplaHandlingar(true));

		arendeExportClient.saveNewHandelse(saveNewHandelse);
		openEIntegration.setStatus(byggRCase.getExternalCaseId(), BYGGR, errandNr, DONE);
	}

	public void addCertifiedInspector(final ByggRCaseDTO byggRCase) {
		var stakeholder = byggRCase.getStakeholders().getFirst();
		var stakeholderId = extractStakeholderId(byggRCase.getStakeholders());
		var errandNr = byggRCase.getExtraParameters().get(ERRAND_NR);
		var otherInformation = byggRCase.getExtraParameters().get(OTHER_INFORMATION);
		var arrayOfHandling = createArrayOfHandling(byggRCase);

		var handelseIntressent = createAddCertifiedInspectorHandelseIntressent(stakeholder, stakeholderId, byggRCase.getExtraParameters());
		var newHandelse = createAddCertifiedInspectorHandelse(otherInformation, handelseIntressent);

		var saveNewHandelse = new SaveNewHandelse()
			.withMessage(new SaveNewHandelseMessage()
				.withDnr(errandNr)
				.withHandlaggarSign(SYSTEM)
				.withHandelse(newHandelse)
				.withHandlingar(arrayOfHandling)
				.withAnkomststamplaHandlingar(false)
				.withAutoGenereraBeslutNr(false)
				.withAnkomststamplaHandlingar(true));

		arendeExportClient.saveNewHandelse(saveNewHandelse);
		arendeExportClient.saveNewHandelse(createAlertCaseManagerEvent(errandNr));
		openEIntegration.setStatus(byggRCase.getExternalCaseId(), BYGGR, errandNr, DONE);
	}

	public void respondToNeighborhoodNotification(final ByggRCaseDTO byggRCase) {
		var stakeholderId = extractStakeholderId(byggRCase.getStakeholders());
		var comment = byggRCase.getExtraParameters().get(COMMENT);
		var errandInformation = byggRCase.getExtraParameters().get(ERRAND_INFORMATION);
		var errandNr = byggRCase.getExtraParameters().get(ERRAND_NR);
		var handelseId = Integer.parseInt(errandNr.replaceAll("^[^\\[]*\\[([^]]+)].*", "$1"));
		var dnr = errandNr.split("\\[")[0].trim();
		var handelseHandling = createNeighborhoodNotificationArrayOfHandling(byggRCase);
		var handelse = extractEvent(getByggRCase(dnr), handelseId);
		var intressent = extractIntressentFromEvent(handelse, stakeholderId);
		var remisser = getRemisserByPersOrgNrAndFilterByDnr(stakeholderId, dnr);
		var remiss = filterRemisserByStakeholderIdAndIntressentRoll(remisser, stakeholderId, intressent);

		var saveNewRemissvar = new SaveNewRemissvar()
			.withMessage(new SaveNewRemissvarMessage()
				.withHandlaggarSign(SYSTEM)
				.withErinran(comment.equals("Jag har synpunkter"))
				.withMeddelande(errandInformation)
				.withRemissId(remiss.getRemissId())
				.withHandlingar(handelseHandling));

		arendeExportClient.saveNewRemissvar(saveNewRemissvar);
		openEIntegration.setStatus(byggRCase.getExternalCaseId(), BYGGR, errandNr, DONE);
	}

	private Remiss filterRemisserByStakeholderIdAndIntressentRoll(final List<Remiss> remisser, final String stakeholderId, final HandelseIntressent intressent) {
		return remisser.stream()
			.filter(remiss -> stakeholderId.equals(remiss.getMottagare().getPersOrgNr()))
			.filter(remiss -> // Kontrollerar att alla roller i intressenten finns i remissen och vice versa
			new HashSet<>(intressent.getRollLista().getRoll()).containsAll(remiss.getMottagare().getRollLista().getRoll()) &&
				new HashSet<>(remiss.getMottagare().getRollLista().getRoll()).containsAll(intressent.getRollLista().getRoll()))
			.findFirst().orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No remiss with equal rollLista found for stakeholderId %s".formatted(stakeholderId)));
	}

	private List<Remiss> getRemisserByPersOrgNrAndFilterByDnr(final String persOrgNr, final String dnr) {
		return arendeExportClient.getRemisserByPersOrgNr(new GetRemisserByPersOrgNr()
			.withPersOrgNr(persOrgNr)
			.withStatusFilter(RemissStatusFilter.NONE)).getGetRemisserByPersOrgNrResult().getRemiss().stream()
			.filter(remiss -> remiss.getDnr().equals(dnr))
			.toList();
	}

	/**
	 * The incoming request might have one or two stakeholders. If any stakeholder is of type Organization, we should use
	 * the organization number as stakeholderId. If no organization is found, we should use the personId to fetch a personal
	 * number from
	 * citizenService and use this personal number as the stakeholder id.
	 *
	 * @param  stakeholders List of stakeholders
	 * @return              String, organization number or personal number of the stakeholder.
	 */
	public String extractStakeholderId(final List<StakeholderDTO> stakeholders) {
		final var organizationId = stakeholders.stream()
			.filter(OrganizationDTO.class::isInstance)
			.findFirst()
			.map(stakeholder -> ((OrganizationDTO) stakeholder).getOrganizationNumber())
			.map(LegalIdUtility::prefixOrgnbr)
			.map(LegalIdUtility::addHyphen)
			.orElse(null);

		if (organizationId != null) {
			return organizationId;
		}

		return stakeholders.stream()
			.filter(PersonDTO.class::isInstance)
			.findFirst()
			.map(stakeholder -> ((PersonDTO) stakeholder).getPersonId())
			.map(citizenService::getPersonalNumber)
			.map(LegalIdUtility::addHyphen)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No stakeholder found in the incoming request."));
	}

	/**
	 * Fetches a ByggR case based on the case number.
	 *
	 * @param  dnr the case number
	 * @return     Arende, a ByggR case
	 */
	public Arende getByggRCase(final String dnr) {
		return arendeExportClient.getArende(new GetArende().withDnr(dnr)).getGetArendeResult();
	}

	public SaveNewArendeResponse2 saveNewCase(final ByggRCaseDTO byggRCase, final String municipalityId) {
		Map<String, CaseTypeData> caseTypeMap = new HashMap<>();
		caseTypeDataRepository.findAll().forEach(caseTypeData -> caseTypeMap.put(caseTypeData.getValue(), caseTypeData));

		final var saveNewArende = toSaveNewArende(byggRCase, caseTypeMap.get(byggRCase.getCaseType()));
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
			writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_KONTROLLANSVARIG, byggrAdminMessageSb);
		}
		if (containsPersonDuplicates(caseInput.getStakeholders())) {
			writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_INTRESSENT_KUNDE_INTE_REGISTRERAS, byggrAdminMessageSb);
		}
		if (!containsPropertyOwner(saveNewArende.getMessage().getArende().getIntressentLista().getIntressent())) {
			writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_FASTIGHETSAGARE, byggrAdminMessageSb);
		}
		if (!byggrAdminMessageSb.toString().isEmpty()) {
			writeEventNote(Constants.BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT, byggrAdminMessageSb);
			arendeExportClient.saveNewHandelse(toSaveNewManuellHanteringHandelse(response.getDnr(), byggrAdminMessageSb.toString()));
		}
	}

	public void saveNewIncomingAttachmentHandelse(final String dnr, final List<AttachmentDTO> attachmentDTOList) {
		final var saveNewHandelse = new SaveNewHandelse()
			.withMessage(toSaveNewHandelseMessage(dnr, attachmentDTOList));
		arendeExportClient.saveNewHandelse(saveNewHandelse);
	}

	public CaseStatusDTO toByggrStatus(final CaseMapping caseMapping) {
		var getArendeResponse = arendeExportClient.getArende(new GetArende().withDnr(caseMapping.getCaseId()));
		return ByggrMapper.toByggrStatus(getArendeResponse.getGetArendeResult(), caseMapping.getExternalCaseId(), List.of(caseMapping));
	}

	public CaseStatusDTO toByggrStatus(final Arende arende, final String externalCaseId, final String municipalityId) {
		final var caseMappingList = caseMappingService.getCaseMapping(externalCaseId, arende.getDnr(), municipalityId);
		return ByggrMapper.toByggrStatus(arende, externalCaseId, caseMappingList);
	}

	public List<CaseStatusDTO> getByggrStatusByLegalId(final String legalId, final PartyType partyType, final String municipalityId) {
		final var getRelateradeArendenByPersOrgNrAndRoleInput = new GetRelateradeArendenByPersOrgNrAndRole()
			.withPersOrgNr(legalId)
			.withArendeIntressentRoller(new ArrayOfString().withString(StakeholderRole.APPLICANT.getText()))
			.withHandelseIntressentRoller(new ArrayOfString().withString(StakeholderRole.APPLICANT.getText()));

		var arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();

		if (arrayOfByggrArende == null) {
			return emptyList();
		}

		// If no cases are found, try to fetch cases with formatted legal id
		if (arrayOfByggrArende.getArende().isEmpty()) {
			getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(CaseUtil.getFormattedLegalId(partyType, legalId));
			arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();
		}

		return arrayOfByggrArende.getArende().stream().map(byggrArende -> {
			final var caseMappingList = caseMappingService.getCaseMapping(null, byggrArende.getDnr(), municipalityId);

			return toByggrStatus(byggrArende, Optional.ofNullable(caseMappingList)
				.filter(list -> !list.isEmpty())
				.map(list -> list.getFirst().getExternalCaseId())
				.orElse(null), municipalityId);
		})
			.toList();
	}

	public ArrayOfArendeIntressent2 getByggrIntressenter(final ByggRCaseDTO byggRCase) {

		// Add all stakeholders from case to the list
		final var stakeholders = new ArrayList<>(byggRCase.getStakeholders());
		populateStakeholderListWithPropertyOwners(byggRCase, stakeholders);
		final var personIds = filterPersonId(stakeholders);

		return new ArrayOfArendeIntressent2().withIntressent(stakeholders.stream().filter(dto -> !dto.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString()))
			.map(stakeholderDTO -> {

				final var intressent = new ArendeIntressent();
				setStakeholderFields(stakeholderDTO, personIds, intressent);
				if (stakeholderDTO.getAddresses() != null) {
					toAdressDTos(stakeholderDTO, intressent);
				}
				intressent.withIntressentKommunikationLista(toByggrContactInfo(stakeholderDTO, intressent.getAttention()))
					.withRollLista(toArrayOfRoles(stakeholderDTO));
				return intressent;
			})
			.filter(intressent -> StringUtils.isNotBlank(intressent.getPersOrgNr()))
			.toList());
	}

	public void populateStakeholderListWithPropertyOwners(final ByggRCaseDTO byggRCase, final List<StakeholderDTO> stakeholders) {
		// Filter all persons
		final var persons = stakeholders.stream()
			.filter(PersonDTO.class::isInstance)
			.map(obj -> {
				final var personOjb = (PersonDTO) obj;
				personOjb.setPersonalNumber(getPersonalNumber(personOjb));
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
			populateStakeholderListWithPropertyOwnerPersons(persons, stakeholders, propertyOwners);
			populateStakeholderListWithPropertyOwnerOrganizations(organizations, stakeholders, propertyOwners);
		});
	}

	private String getPersonalNumber(final PersonDTO personDTOStakeholder) {
		String pnr = citizenService.getPersonalNumber(personDTOStakeholder.getPersonId());
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
				// If we already have created a "arendeFastighet" with the same propertyDesignation,
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

	Arende2 toArende2(final ByggRCaseDTO byggRCase, final CaseTypeData caseTypeData) {
		final var arende = new Arende2();

		if ((byggRCase.getFacilities() == null) || (byggRCase.getFacilities().getFirst() == null)) {
			arende.withArendeslag(caseTypeData.getArendeSlag());

		} else if (caseTypeData.getArendeSlag() != null) {
			arende.withArendeslag(caseTypeData.getArendeSlag());
			if (!WITH_NULLABLE_FACILITY_TYPE.contains(byggRCase.getCaseType())) {
				arende.withArendeklass(getArendeKlass(byggRCase.getFacilities()));
			}
		} else {
			arende.withArendeslag(getMainOrOnlyArendeslag(byggRCase.getFacilities()));
		}

		return arende
			.withArendegrupp(caseTypeData.getArendeGrupp())
			.withArendetyp(caseTypeData.getArendeTyp())
			.withNamndkod(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN)
			.withEnhetkod(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET)
			.withKommun(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN)
			.withHandlaggare(new HandlaggareBas().withSignatur(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN))
			.withArInomplan(isWithinPlan(byggRCase.getFacilities()))
			.withBeskrivning(getArendeBeskrivning(byggRCase, caseTypeData.getArendeMening()))
			.withIntressentLista(getByggrIntressenter(byggRCase))
			.withObjektLista(getByggrArendeObjektLista(byggRCase))
			.withAnkomstDatum(LocalDate.now())
			// ProjektNummer/FakturaId in ByggR.
			.withProjektnr(Optional.ofNullable(getInvoiceMarking(byggRCase))
				.orElse(parsePropertyDesignation(byggRCase.getFacilities())));
	}

}
