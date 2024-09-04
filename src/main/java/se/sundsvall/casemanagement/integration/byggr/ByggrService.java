package se.sundsvall.casemanagement.integration.byggr;


import static java.util.Collections.emptyList;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.WITH_NULLABLE_FACILITY_TYPE;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
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
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.casemanagement.service.CitizenService;
import se.sundsvall.casemanagement.service.FbService;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

import arendeexport.Arende;
import arendeexport.Arende2;
import arendeexport.ArendeFastighet;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfAbstractArendeObjekt2;
import arendeexport.ArrayOfArendeIntressent2;
import arendeexport.ArrayOfHandelseHandling;
import arendeexport.ArrayOfHandelseIntressent2;
import arendeexport.ArrayOfHandling;
import arendeexport.ArrayOfString;
import arendeexport.Dokument;
import arendeexport.DokumentFil;
import arendeexport.Fastighet;
import arendeexport.GetArende;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.Handelse;
import arendeexport.HandelseHandling;
import arendeexport.HandelseIntressent;
import arendeexport.HandlaggareBas;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeResponse2;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseMessage;

@Service
public class ByggrService {

	private final FbService fbService;

	private final CitizenService citizenService;

	private final CaseMappingService caseMappingService;

	private final ArendeExportClient arendeExportClient;

	private final Map<String, CaseTypeData> caseTypeMap = new HashMap<>();

	private final CaseTypeRepository caseTypeRepository;

	public ByggrService(final FbService fbService,
		final CitizenService citizenService,
		final CaseMappingService caseMappingService,
		final ArendeExportClient arendeExportClient,
		final CaseTypeRepository caseTypeRepository) {
		this.fbService = fbService;
		this.citizenService = citizenService;
		this.caseMappingService = caseMappingService;
		this.arendeExportClient = arendeExportClient;
		this.caseTypeRepository = caseTypeRepository;
	}

	public void updateByggRCase(final ByggRCaseDTO byggRCaseDTO) {
		var stakeholderId = extractStakeholderId(byggRCaseDTO.getStakeholders());
		var errandNr = byggRCaseDTO.getExtraParameters().get("errandNr");
		var comment = byggRCaseDTO.getExtraParameters().get("comment");
		var errandInformation = byggRCaseDTO.getExtraParameters().get("errandInformation");
		var handelseHandling = createHandelseHandling(byggRCaseDTO);

		var byggRCase = getByggRCase(errandNr);
		var handelse = extractEvent(byggRCase, "GRANHO", "GRAUTS");

		var intressent = extractEventStakeholder(handelse, stakeholderId);

		var arendeFastighet = byggRCase.getObjektLista().getAbstractArendeObjekt().stream()
			.map(abstractArendeObjekt -> (ArendeFastighet) abstractArendeObjekt)
			.findFirst().orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No ArendeFastighet found in ByggRCase"));

		var fastighet = arendeFastighet.getFastighet();
		var newHandelse = createNewEvent(comment, errandInformation, intressent, fastighet, handelseHandling);
		var saveNewHandelse = createSaveNewHandelse(errandNr, newHandelse, handelseHandling);

		arendeExportClient.saveNewHandelse(saveNewHandelse);
	}

	/**
	 * @param dnr The case number
	 * @param handelse The ByggR event
	 * @param arrayOfHandelseHandling The attachments that the stakeholder sends with the response
	 * @return SaveNewHandelse, a request model that is sent to ByggR
	 */
	public SaveNewHandelse createSaveNewHandelse(final String dnr, final Handelse handelse, final ArrayOfHandelseHandling arrayOfHandelseHandling) {
		var handlingar = arrayOfHandelseHandling.getHandling();

		var arrayOfHandling = new ArrayOfHandling().withHandling(handlingar);

		var saveNewHandelseMessage = new SaveNewHandelseMessage()
			.withDnr(dnr)
			.withHandelse(handelse)
			.withHandlingar(arrayOfHandling);


		return new SaveNewHandelse().withMessage(saveNewHandelseMessage);
	}

	/**
	 * Repackages the attachments from the incoming request to a format that ByggR can understand.
	 *
	 * @param byggRCaseDTO The incoming request from OpenE
	 * @return ArrayOfHandelseHandling, a list of attachments that the stakeholder sends with the response
	 */
	public ArrayOfHandelseHandling createHandelseHandling(final ByggRCaseDTO byggRCaseDTO) {
		var attachments = byggRCaseDTO.getAttachments();
		var arrayOfHandelseHandling = new ArrayOfHandelseHandling();

		for (var attachment : attachments) {
			var dokumentFil = new DokumentFil().withFilBuffer(attachment.getFile().getBytes()).withFilAndelse(attachment.getExtension());
			var dokument = new Dokument().withFil(dokumentFil);
			var handelseHandling = new HandelseHandling().withDokument(dokument).withTyp("BIL");
			arrayOfHandelseHandling.getHandling().add(handelseHandling);
		}
		return arrayOfHandelseHandling;
	}

	/**
	 * Creates a new Handelse object with the given parameters.
	 *
	 * @param comment String that determines if the stakeholder has any issues with the building permit
	 * @param errandInformation String that contains the stakeholders comment. (Bad name given by OpenE)
	 * @param intressent The stakeholder that responds to the hearing request
	 * @param fastighet The property that the permit is for
	 * @param handelseHandling The attachments that the stakeholder sends with the response
	 * @return Handelse, a new event in a ByggR Case
	 */
	public Handelse createNewEvent(final String comment, final String errandInformation, final HandelseIntressent intressent, final Fastighet fastighet, final ArrayOfHandelseHandling handelseHandling) {
		var opinion = comment.equals("Jag har synpunkter") ? "Grannehörande Svar med erinran" : "Grannehörande Svar utan erinran";
		var trakt = fastighet.getTrakt();
		var fbetNr = fastighet.getFbetNr();

		var title = opinion + ", " + trakt + " " + fbetNr + ", " + intressent.getNamn();

		return new Handelse()
			.withRubrik(title)
			.withAnteckning(errandInformation)
			.withHandelsetyp("GRANHO")
			.withHandelseslag("GRASVA")
			.withHandlingLista(handelseHandling)
			.withIntressentLista(new ArrayOfHandelseIntressent2().withIntressent(intressent));
	}

	/**
	 * Extracts a stakeholder from a specific byggR event based on the stakeholder id.
	 *
	 * @param handelse the event
	 * @param stakeholderId the stakeholder id
	 * @return HandelseIntressent, the stakeholder of a specific event
	 */
	public HandelseIntressent extractEventStakeholder(final Handelse handelse, final String stakeholderId) {
		return handelse.getIntressentLista().getIntressent().stream()
			.filter(intressent1 -> intressent1.getPersOrgNr().equals(stakeholderId))
			.findFirst().orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "Stakeholder with id %s not found in ByggRCase".formatted(stakeholderId)));
	}

	/**
	 * A byggR case might have multiple different events. This method extracts the wanted event based
	 * on the handelsetyp and handelseslag.
	 *
	 * @param arende the byggR case
	 * @param handelsetyp wanted handelsetyp
	 * @param handelseslag wanted handelseslag
	 * @return Handelse, a specific event in a ByggR Case
	 */
	public Handelse extractEvent(final Arende arende, final String handelsetyp, final String handelseslag) {
		return arende.getHandelseLista().getHandelse().stream()
			.filter(handelse -> handelse.getHandelsetyp().equals(handelsetyp) && handelse.getHandelseslag().equals(handelseslag))
			.findFirst().orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No GRANHO/GRAUTS event found in ByggRCase"));
	}

	/**
	 * The incoming request might have one or two stakeholders. If any stakeholder is of type
	 * Organization, we should use the organization number as stakeholderId.
	 * If no organization is found, we should use the personId to fetch a personal number from
	 * citizenService and use this personal number as the stakeholder id.
	 *
	 * @param stakeholders List of stakeholders
	 * @return String, organization number or personal number of the stakeholder.
	 */
	public String extractStakeholderId(final List<StakeholderDTO> stakeholders) {
		var organizationId = stakeholders.stream()
			.filter(stakeholder -> stakeholder instanceof OrganizationDTO)
			.findFirst()
			.map(stakeholder -> ((OrganizationDTO) stakeholder).getOrganizationNumber())
			.orElse(null);

		if (organizationId != null) {
			return organizationId;
		}

		return stakeholders.stream()
			.filter(stakeholder -> stakeholder instanceof PersonDTO)
			.findFirst()
			.map(stakeholder -> ((PersonDTO) stakeholder).getPersonId())
			.map(citizenService::getPersonalNumber)
			.map(personalNumber -> personalNumber.substring(0, 8) + "-" + personalNumber.substring(8))
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No stakeholder found in the incoming request."));
	}

	/**
	 * Fetches a ByggR case based on the case number.
	 *
	 * @param dnr the case number
	 * @return Arende, a ByggR case
	 */
	public Arende getByggRCase(final String dnr) {
		return arendeExportClient.getArende(new GetArende().withDnr(dnr)).getGetArendeResult();
	}

	public SaveNewArendeResponse2 saveNewCase(final ByggRCaseDTO caseInput) {

		caseTypeRepository.findAll().forEach(caseTypeData -> caseTypeMap.put(caseTypeData.getValue(), caseTypeData));

		final var saveNewArende = toSaveNewArende(caseInput, caseTypeMap.get(caseInput.getCaseType()));
		saveNewArende.getMessage().setArende(toArende2(caseInput));

		final var response = arendeExportClient.saveNewArende(saveNewArende).getSaveNewArendeResult();

		createOccurrence(caseInput, saveNewArende, response);
		caseMappingService.postCaseMapping(caseInput, response.getDnr(), SystemType.BYGGR);
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
		return ByggrMapper.toByggrStatus(arendeExportClient.getArende(new GetArende().withDnr(caseMapping.getCaseId())).getGetArendeResult(), caseMapping.getExternalCaseId(), List.of(caseMapping));
	}

	public CaseStatusDTO toByggrStatus(final Arende arende, final String externalCaseId) {
		final var caseMappingList = caseMappingService.getCaseMapping(externalCaseId, arende.getDnr());
		return ByggrMapper.toByggrStatus(arende, externalCaseId, caseMappingList);
	}

	public List<CaseStatusDTO> getByggrStatusByOrgNr(final String organizationNumber) {

		final var getRelateradeArendenByPersOrgNrAndRoleInput = new GetRelateradeArendenByPersOrgNrAndRole()
			.withPersOrgNr(organizationNumber)
			.withArendeIntressentRoller(new ArrayOfString().withString(StakeholderRole.APPLICANT.getText()))
			.withHandelseIntressentRoller(new ArrayOfString().withString(StakeholderRole.APPLICANT.getText()));

		var arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();

		if (arrayOfByggrArende == null) {
			return emptyList();
		}

		if (arrayOfByggrArende.getArende().isEmpty()) {
			getRelateradeArendenByPersOrgNrAndRoleInput.setPersOrgNr(CaseUtil.getSokigoFormattedOrganizationNumber(organizationNumber));
			arrayOfByggrArende = arendeExportClient.getRelateradeArendenByPersOrgNrAndRole(getRelateradeArendenByPersOrgNrAndRoleInput).getGetRelateradeArendenByPersOrgNrAndRoleResult();
		}

		return arrayOfByggrArende.getArende().stream().map(byggrArende -> {
				final var caseMappingList = caseMappingService.getCaseMapping(null, byggrArende.getDnr());
				return toByggrStatus(byggrArende,
					Optional.ofNullable(caseMappingList)
						.filter(list -> !list.isEmpty()).map(list -> list.getFirst().getExternalCaseId())
						.orElse(null));
			})
			.toList();
	}

	public ArrayOfArendeIntressent2 getByggrIntressenter(final ByggRCaseDTO pCase) {

		// Add all stakeholders from case to the list
		final var stakeholderDTOList = new ArrayList<>(pCase.getStakeholders());
		populateStakeholderListWithPropertyOwners(pCase, stakeholderDTOList);

		final var personIdList = filterPersonId(stakeholderDTOList);

		return new ArrayOfArendeIntressent2().withIntressent(stakeholderDTOList.stream().
			filter(dto -> !dto.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString()))
			.map(stakeholderDTO -> {

				final var intressent = new ArendeIntressent();
				setStakeholderFields(stakeholderDTO, personIdList, intressent);
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

	public void populateStakeholderListWithPropertyOwners(final ByggRCaseDTO pCase, final List<StakeholderDTO> stakeholderDTOList) {
		// Filter all persons
		final var personDTOStakeholders = stakeholderDTOList.stream()
			.filter(PersonDTO.class::isInstance)
			.map(obj -> {
				final var personOjb = (PersonDTO) obj;
				personOjb.setPersonalNumber(getPersonalNumber(personOjb));
				return personOjb;
			})
			.toList();

		// Filter all organizations
		final var organizationDTOStakeholders = stakeholderDTOList.stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(obj -> {
				final var orgObj = (OrganizationDTO) obj;
				orgObj.setOrganizationNumber(CaseUtil.getSokigoFormattedOrganizationNumber(orgObj.getOrganizationNumber()));
				return orgObj;
			})
			.toList();
		// Loop through each facility and get the property owners for each one
		pCase.getFacilities().forEach(facility -> {
			final var propertyOwnerList = fbService.getPropertyOwnerByPropertyDesignation(facility.getAddress().getPropertyDesignation());
			populateStakeholderListWithPropertyOwnerPersons(personDTOStakeholders, stakeholderDTOList, propertyOwnerList);
			populateStakeholderListWithPropertyOwnerOrganizations(organizationDTOStakeholders, stakeholderDTOList, propertyOwnerList);
		});
	}

	private String getPersonalNumber(final PersonDTO personDTOStakeholder) {
		String pnr = citizenService.getPersonalNumber(personDTOStakeholder.getPersonId());
		if ((pnr != null) && (pnr.length() == 12)) {
			pnr = pnr.substring(0, 8) + "-" + pnr.substring(8);
		}
		return pnr;
	}

	public ArrayOfAbstractArendeObjekt2 getByggrArendeObjektLista(final ByggRCaseDTO pCase) {

		final var arendeObjektLista = new ArrayOfAbstractArendeObjekt2();

		final var usedPropertyDesignations = new ArrayList<String>();
		pCase.getFacilities().forEach(facilityDTO -> {
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

	Arende2 toArende2(final ByggRCaseDTO pCase) {

		final var caseType = caseTypeMap.get(pCase.getCaseType());
		final var arende = new Arende2();

		if ((pCase.getFacilities() == null) || (pCase.getFacilities().getFirst() == null)) {
			arende.withArendeslag(caseType.getArendeSlag());

		} else if (caseType.getArendeSlag() != null) {
			arende.withArendeslag(caseType.getArendeSlag());
			if (!WITH_NULLABLE_FACILITY_TYPE.contains(pCase.getCaseType())) {
				arende.withArendeklass(getArendeKlass(pCase.getFacilities()));
			}
		} else {
			arende.withArendeslag(getMainOrOnlyArendeslag(pCase.getFacilities()));
		}

		return arende
			.withArendegrupp(caseType.getArendeGrupp())
			.withArendetyp(caseType.getArendeTyp())
			.withNamndkod(Constants.BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN)
			.withEnhetkod(Constants.BYGGR_ENHETKOD_STADSBYGGNADSKONTORET)
			.withKommun(Constants.BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN)
			.withHandlaggare(new HandlaggareBas().withSignatur(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN))
			.withArInomplan(isWithinPlan(pCase.getFacilities()))
			.withBeskrivning(getArendeBeskrivning(pCase, caseType.getArendeMening()))
			.withIntressentLista(getByggrIntressenter(pCase))
			.withObjektLista(getByggrArendeObjektLista(pCase))
			.withAnkomstDatum(LocalDate.now())
			// ProjektNummer/FakturaId in ByggR.
			.withProjektnr(Optional.ofNullable(getInvoiceMarking(pCase))
				.orElse(parsePropertyDesignation(pCase.getFacilities())));
	}

}
