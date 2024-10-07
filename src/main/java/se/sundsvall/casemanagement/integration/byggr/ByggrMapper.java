package se.sundsvall.casemanagement.integration.byggr;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.hasHandelseList;
import static se.sundsvall.casemanagement.integration.byggr.ByggrUtil.isCaseClosed;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDLING_STATUS_INKOMMEN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMTYP_EPOST;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMTYP_HEMTELEFON;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMTYP_MOBIL;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANMALAN;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANSOKAN;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.CaseStatusDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;
import se.sundsvall.casemanagement.util.Constants;

import arendeexport.Aktorbehorighet;
import arendeexport.Arende;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfAktorbehorighet;
import arendeexport.ArrayOfHandelseIntressent2;
import arendeexport.ArrayOfHandling;
import arendeexport.ArrayOfIntressentKommunikation;
import arendeexport.ArrayOfString2;
import arendeexport.Dokument;
import arendeexport.DokumentFil;
import arendeexport.Fakturaadress;
import arendeexport.Handelse;
import arendeexport.HandelseHandling;
import arendeexport.HandelseIntressent;
import arendeexport.IntressentAttention;
import arendeexport.IntressentKommunikation;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeMessage;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseMessage;

public final class ByggrMapper {

	private static final Logger log = LoggerFactory.getLogger(ByggrMapper.class);

	private static final String REGEX_LAST_COMMA = ",(?=[^,]*$)";

	private ByggrMapper() {}


	static void setStakeholderFields(final StakeholderDTO stakeholderDTO, final List<String> personIds, final ArendeIntressent intressent) {
		switch (stakeholderDTO) {
			case final PersonDTO personDTO -> {
				// If the request contains two person with the same personId, it must be handled manually
				if (personIds.stream().filter(personId -> personId.equals(personDTO.getPersonId())).count() > 1) {
					return;
				}
				setPersonFields(intressent, personDTO);
			}
			case final OrganizationDTO organizationDTO ->
				setOrganizationFields(intressent, organizationDTO);
			default -> throw Problem.valueOf(Status.BAD_REQUEST, "Invalid stakeholder type");
		}
	}

	static SaveNewHandelse toSaveNewManuellHanteringHandelse(final String dnr, final String note) {

		return new SaveNewHandelse()
			.withMessage(new SaveNewHandelseMessage()
				.withDnr(dnr)
				.withHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN)
				.withHandelse(new Handelse()
					.withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING)
					.withRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN)
					.withHandelsetyp(Constants.BYGGR_HANDELSETYP_STATUS)
					.withHandelseslag(Constants.BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS)
					.withStartDatum(LocalDateTime.now())
					.withAnteckning(note)));
	}

	static Handelse toHandelse(final ByggRCaseDTO dto, final CaseTypeData caseType) {
		final var handelse = new Handelse()
			.withStartDatum(LocalDateTime.now())
			.withRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN)
			.withRubrik(caseType.getHandelseRubrik())
			.withHandelsetyp(caseType.getHandelseTyp())
			.withHandelseslag(caseType.getHandelseSlag());

		Optional.ofNullable(dto.getFacilities().getFirst().getFacilityType())
			.map(FacilityType::valueOf)
			.ifPresent(facilityType -> {
				switch (facilityType) {
					case FIREPLACE -> handelse
						.withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD)
						.withHandelseslag(Constants.BYGGR_HANDELSESLAG_ELDSTAD);
					case FIREPLACE_SMOKECHANNEL -> handelse
						.withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL)
						.withHandelseslag(Constants.BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL);
					default -> {
						// Do nothing
					}
				}
			});
		return handelse;
	}

	static ArrayOfHandling toArrayOfHandling(final List<AttachmentDTO> attachments) {
		return new ArrayOfHandling()
			.withHandling(attachments.stream()
				.map(ByggrMapper::toHandelseHandling)
				.toList());
	}

	static HandelseHandling toHandelseHandling(final AttachmentDTO attachment) {

		return new HandelseHandling()
			.withAnteckning(attachment.getName()) // Not a typo. They want it like this
			.withDokument(new Dokument()
				.withFil(new DokumentFil()
					.withFilBuffer(Base64.getDecoder().decode(attachment.getFile().getBytes()))
					.withFilAndelse(attachment.getExtension().toLowerCase()))
				.withNamn(attachment.getName())
				.withBeskrivning(attachment.getNote()))
			.withStatus(Constants.BYGGR_HANDLING_STATUS_INKOMMEN)
			.withTyp(attachment.getCategory());
	}

	static SaveNewArende toSaveNewArende(final ByggRCaseDTO byggRCase, final CaseTypeData caseType) {
		return new SaveNewArende()
			.withMessage(new SaveNewArendeMessage()
				.withAnkomststamplaHandlingar(true)
				.withHandlingar(toArrayOfHandling(byggRCase.getAttachments()))
				.withHandelse(toHandelse(byggRCase, caseType))
				.withHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN));
	}

	static SaveNewHandelseMessage toSaveNewHandelseMessage(final String dnr, final List<AttachmentDTO> attachmentDTOList) {
		return new SaveNewHandelseMessage()
			.withDnr(dnr)
			.withHandlaggarSign(Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN)
			.withHandlingar(toArrayOfHandling(attachmentDTOList))
			.withAnkomststamplaHandlingar(true)
			.withHandelse(new Handelse()
				.withRiktning(Constants.BYGGR_HANDELSE_RIKTNING_IN)
				.withRubrik(Constants.BYGGR_HANDELSE_RUBRIK_KOMPLETTERING_TILL_ADMIN)
				.withHandelsetyp(Constants.BYGGR_HANDELSETYP_HANDLING)
				.withHandelseslag(Constants.BYGGR_HANDELSESLAG_KOMPLETTERING_TILL_ADMIN)
				.withStartDatum(LocalDateTime.now(ZoneId.systemDefault()))
				.withAnteckning(Constants.BYGGR_HANDELSE_ANTECKNING));
	}

	static String getInvoiceMarking(final ByggRCaseDTO pCase) {
		return pCase.getStakeholders().stream()
			.filter(Objects::nonNull)
			.filter(stakeholder -> stakeholder.getAddresses() != null)
			.flatMap(stakeholder -> stakeholder.getAddresses().stream())
			.filter(address -> address.getAddressCategories().contains(AddressCategory.INVOICE_ADDRESS))
			.map(AddressDTO::getInvoiceMarking)
			.filter(StringUtils::isNotBlank)
			.findFirst()
			.orElse(null);
	}

	static String getArendeKlass(final List<FacilityDTO> facilityList) {
		return facilityList.stream()
			.findFirst()
			.map(FacilityDTO::getFacilityType)
			.map((String t) -> FacilityType.valueOf(t).getValue())
			.orElse(FacilityType.OTHER.getValue());
	}

	static String getMainOrOnlyArendeslag(final List<FacilityDTO> facilityList) {
		return FacilityType.valueOf(facilityList.stream()
				.filter(facility -> FacilityType.USAGE_CHANGE.equals(FacilityType.valueOf(facility.getFacilityType())))
				.findFirst()
				.orElse(facilityList.getFirst())
				.getFacilityType())
			.getValue();
	}

	static void setPostalAddressFields(final ArendeIntressent intressent, final AddressDTO addressDTO) {
		intressent.setAdress(Optional.ofNullable(addressDTO.getHouseNumber())
			.map(houseNumber -> "%s %s".formatted(addressDTO.getStreet(), houseNumber))
			.orElse(addressDTO.getStreet()));
		intressent.setPostNr(addressDTO.getPostalCode());
		intressent.setOrt(addressDTO.getCity());
		intressent.setLand(addressDTO.getCountry());
		intressent.setCoAdress(addressDTO.getCareOf());
	}

	static void setOrganizationFields(final ArendeIntressent intressent, final OrganizationDTO organizationDTO) {
		intressent.setArForetag(true);
		intressent.setNamn(organizationDTO.getOrganizationName());
		intressent.setPersOrgNr(organizationDTO.getOrganizationNumber());
	}

	static void setPersonFields(final ArendeIntressent intressent, final PersonDTO personDTO) {
		intressent.setArForetag(false);
		intressent.setFornamn(personDTO.getFirstName());
		intressent.setEfternamn(personDTO.getLastName());
		intressent.setPersOrgNr(personDTO.getPersonalNumber());

	}

	static ArrayOfString2 toArrayOfRoles(final StakeholderDTO stakeholderDTO) {
		return new ArrayOfString2().withRoll(
			stakeholderDTO.getRoles().stream()
				.distinct()
				.map(role -> StakeholderRole.valueOf(role).getText())
				.toList());
	}

	static Fakturaadress toFakturaadress(final AddressDTO addressDTO) {
		return new Fakturaadress()
			.withAdress(Optional.ofNullable(addressDTO.getHouseNumber())
				.map(houseNumber -> addressDTO.getStreet() + " " + houseNumber)
				.orElse(addressDTO.getStreet()))
			.withAttention(addressDTO.getAttention())
			.withLand(addressDTO.getCountry())
			.withOrt(addressDTO.getCity())
			.withPostNr(addressDTO.getPostalCode());
	}

	static ArrayOfIntressentKommunikation toByggrContactInfo(final StakeholderDTO stakeholderDTO, final IntressentAttention intressentAttention) {
		final var arrayOfIntressentKommunikation = new ArrayOfIntressentKommunikation();

		if (isNotBlank(stakeholderDTO.getCellphoneNumber())) {
			final var intressentKommunikation = new IntressentKommunikation()
				.withArAktiv(true)
				.withBeskrivning(stakeholderDTO.getCellphoneNumber())
				.withKomtyp(BYGGR_KOMTYP_MOBIL)
				.withAttention(intressentAttention);
			arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
		}
		if (isNotBlank(stakeholderDTO.getPhoneNumber())) {
			final var intressentKommunikation = new IntressentKommunikation()
				.withArAktiv(true)
				.withBeskrivning(stakeholderDTO.getPhoneNumber())
				.withKomtyp(BYGGR_KOMTYP_HEMTELEFON)
				.withAttention(intressentAttention);
			arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
		}
		if (isNotBlank(stakeholderDTO.getEmailAddress())) {
			final var intressentKommunikation = new IntressentKommunikation()
				.withArAktiv(true)
				.withBeskrivning(stakeholderDTO.getEmailAddress())
				.withKomtyp(BYGGR_KOMTYP_EPOST)
				.withAttention(intressentAttention);
			arrayOfIntressentKommunikation.getIntressentKommunikation().add(intressentKommunikation);
		}
		return arrayOfIntressentKommunikation;
	}


	static void populateStakeholderListWithPropertyOwnerPersons(final List<PersonDTO> persons, final List<StakeholderDTO> stakeholders, final List<StakeholderDTO> propertyOwners) {
		final List<PersonDTO> personDTOPropertyOwnerList = propertyOwners.stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast).toList();

		// All incoming personStakeholders that is also propertyOwners
		final List<PersonDTO> personDTOStakeholderPropertyOwnerList = persons.stream()
			.filter(personStakeholder -> personDTOPropertyOwnerList.stream()
				.map(PersonDTO::getPersonalNumber).toList()
				.contains(personStakeholder.getPersonalNumber()))
			.toList();

		log.debug("All incoming personStakeholders that is also propertyOwners: {}", personDTOStakeholderPropertyOwnerList);

		personDTOStakeholderPropertyOwnerList.forEach(person -> person.setRoles(Stream.of(person.getRoles(), List.of(StakeholderRole.PROPERTY_OWNER.toString()))
			.flatMap(Collection::stream)
			.toList()));

		// All personPropertyOwners that does not exist in the incoming request
		final List<PersonDTO> notExistingPersonPropertyOwnerListDTO = personDTOPropertyOwnerList.stream()
			.filter(not(personPropertyOwner -> persons.stream()
				.map(PersonDTO::getPersonalNumber).toList()
				.contains(personPropertyOwner.getPersonalNumber())))
			.toList();

		log.debug("All personPropertyOwners that does not exist in the incoming request: {}", notExistingPersonPropertyOwnerListDTO);

		stakeholders.addAll(notExistingPersonPropertyOwnerListDTO);
	}


	static void populateStakeholderListWithPropertyOwnerOrganizations(final List<OrganizationDTO> organizationDTOStakeholders, final List<StakeholderDTO> stakeholderDTOList, final List<StakeholderDTO> propertyOwnerList) {
		final var organizationDTOPropertyOwnerList = propertyOwnerList.stream()
			.filter(OrganizationDTO.class::isInstance)
			.map(OrganizationDTO.class::cast)
			.toList();

		// All incoming organizationStakeholders that is also propertyOwners
		final var organizationDTOStakeholderPropertyOwnerList = organizationDTOStakeholders.stream()
			.filter(organizationStakeholder -> organizationDTOPropertyOwnerList.stream()
				.map(OrganizationDTO::getOrganizationNumber)
				.toList()
				.contains(organizationStakeholder.getOrganizationNumber()))
			.toList();

		organizationDTOStakeholderPropertyOwnerList.forEach(orgStakeholder -> orgStakeholder.setRoles(Stream.of(orgStakeholder.getRoles(), List.of(StakeholderRole.PROPERTY_OWNER.toString()))
			.flatMap(Collection::stream)
			.toList()));

		// All organizationPropertyOwners that does not exist in the incoming request
		final var notExistingOrgPropertyOwnerList = organizationDTOPropertyOwnerList.stream()
			.filter(not(organizationPropertyOwner -> organizationDTOStakeholders.stream()
				.map(OrganizationDTO::getOrganizationNumber)
				.toList()
				.contains(organizationPropertyOwner.getOrganizationNumber())))
			.toList();

		stakeholderDTOList.addAll(notExistingOrgPropertyOwnerList);
	}

	/**
	 * "Ärendemening" - Is automatically set in ByggR based on "typ", "slag" and "klass",
	 * but when its multiple facilities, it must be set to contain all facilities.
	 *
	 * @param pCase PlanningPermissionCase
	 * @return ärendemening or null
	 */
	static String getArendeBeskrivning(final ByggRCaseDTO pCase, final String caseDescription) {

		if ((caseDescription == null) || pCase.getFacilities().isEmpty()) {
			return null;
		}

		final var descriptions = pCase.getFacilities().stream()
			.filter(facility -> facility.getFacilityType() != null)
			.sorted(Comparator.comparing(FacilityDTO::isMainFacility, Comparator.reverseOrder()))
			.map(facility -> FacilityType.valueOf(facility.getFacilityType()).getDescription().trim().toLowerCase())
			.collect(Collectors.joining(", "))
			.replaceAll(REGEX_LAST_COMMA, " &");

		final var caseDescriptionAddition = Optional.ofNullable(pCase.getCaseTitleAddition())
			.filter(string -> !string.isBlank())
			.map(string -> " samt " + string.trim().toLowerCase())
			.orElse("");

		return MessageFormat.format("{0} {1}{2}", caseDescription, descriptions, caseDescriptionAddition);
	}

	static List<String> filterPersonId(final List<StakeholderDTO> stakeholderDTOList) {
		return stakeholderDTOList.stream()
			.filter(PersonDTO.class::isInstance)
			.map(PersonDTO.class::cast)
			.map(PersonDTO::getPersonId)
			.filter(Objects::nonNull)
			.toList();
	}


	static void toAdressCategory(final StakeholderDTO stakeholderDTO, final AddressDTO addressDTO, final AddressCategory addressCategory, final ArendeIntressent intressent) {
		if (AddressCategory.POSTAL_ADDRESS.equals(addressCategory)) {

			setPostalAddressFields(intressent, addressDTO);

			if (stakeholderDTO instanceof OrganizationDTO) {
				final IntressentAttention intressentAttention = new IntressentAttention();
				intressentAttention.setAttention(addressDTO.getAttention());
				intressent.setAttention(intressentAttention);
			}

		}
		if (AddressCategory.INVOICE_ADDRESS.equals(addressCategory)) {
			if (stakeholderDTO instanceof PersonDTO) {
				throw Problem.valueOf(Status.BAD_REQUEST, Constants.ERR_MSG_PERSON_INVOICE_ADDRESS);
			}

			intressent.setFakturaAdress(toFakturaadress(addressDTO));
		}
	}

	static void toAdressCategories(final StakeholderDTO stakeholderDTO, final AddressDTO addressDTO, final ArendeIntressent intressent) {
		addressDTO.getAddressCategories()
			.forEach(addressCategory -> toAdressCategory(stakeholderDTO, addressDTO, addressCategory, intressent));
	}

	static void toAdressDTos(final StakeholderDTO stakeholderDTO, final ArendeIntressent intressent) {
		stakeholderDTO.getAddresses()
			.forEach(addressDTO -> toAdressCategories(stakeholderDTO, addressDTO, intressent));
	}

	static CaseStatusDTO toByggrStatus(final Arende arende, final String externalCaseId, final List<CaseMapping> caseMappingList) {
		final var caseStatusDTO = buildCaseStatusDTO(arende, externalCaseId, caseMappingList);

		if (isCaseClosed(arende)) {
			caseStatusDTO.setStatus(arende.getStatus());
			return caseStatusDTO;
		}
		if (hasHandelseList(arende)) {
			final var handelseLista = arende.getHandelseLista().getHandelse();
			handelseLista.sort(Comparator.comparing(Handelse::getStartDatum).reversed());

			for (final var handelse : handelseLista) {
				caseStatusDTO.setStatus(getHandelseStatus(handelse.getHandelsetyp(), handelse.getHandelseslag(), handelse.getHandelseutfall()));

				if (caseStatusDTO.getStatus() != null) {
					caseStatusDTO.setTimestamp(handelse.getStartDatum());
					return caseStatusDTO;
				}
			}
		}
		throw Problem.valueOf(Status.NOT_FOUND, Constants.ERR_MSG_STATUS_NOT_FOUND);
	}


	static CaseStatusDTO buildCaseStatusDTO(final Arende arende, final String externalCaseId, final List<CaseMapping> caseMappingList) {
		return CaseStatusDTO.builder()
			.withSystem(SystemType.BYGGR)
			.withExternalCaseId(externalCaseId)
			.withCaseId(arende.getDnr())
			.withCaseType(caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getCaseType())
			.withServiceName(caseMappingList.isEmpty() ? null : caseMappingList.getFirst().getServiceName())
			.build();
	}

	static String getHandelseStatus(final String handelsetyp, final String handelseslag, final String handelseutfall) {

		// OEP-status = Inskickat
		// UNDER
		// OEP-status = Väntar på komplettering
		// KOMP, KOMP1
		if (HANDELSETYP_ANMALAN.equals(handelsetyp)
			|| HANDELSETYP_ANSOKAN.equals(handelsetyp)
			|| Constants.BYGGR_HANDELSETYP_UNDERRATTELSE.equals(handelsetyp) && (Constants.BYGGR_HANDELSESLAG_MED_KRAV_PA_SVAR.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_UTAN_KRAV_PA_SVAR.equals(handelseslag))
			|| Constants.BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE.equals(handelsetyp)
			|| Constants.BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE_PAMINNELSE.equals(handelsetyp)) {
			// ANM, ANSÖKAN
			return handelsetyp;
		}
		// OEP-status = Under behandling
		if (Constants.BYGGR_HANDELSETYP_BESLUT.equals(handelsetyp) && (Constants.BYGGR_HANDELSESLAG_SLUTBESKED.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_AVSKRIVNING.equals(handelseslag))
			|| ((Constants.BYGGR_HANDELSETYP_HANDLING.equals(handelsetyp) && Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR.equals(handelseslag))
			|| Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_TEKNISKA_HANDLINGAR.equals(handelseslag)
			|| Constants.BYGGR_HANDELSESLAG_REVIDERADE_HANDLINGAR.equals(handelseslag))) {
			// SLU, UAB
			return handelseslag;
		} else if (Constants.BYGGR_HANDELSETYP_REMISS.equals(handelsetyp)
			&& Constants.BYGGR_HANDELSESLAG_UTSKICK_AV_REMISS.equals(handelseslag)) {
			// UTSKICK
			return handelseslag;
		}
		// OEP-status = Kompletterad
		else if (Constants.BYGGR_HANDELSETYP_ATOMHANDELSE.equals(handelsetyp)
			&& Constants.BYGGR_HANDELSESLAG_ATOM_KVITTENS.equals(handelseslag)
			&& Constants.BYGGR_HANDELSEUTFALL_ATOM_KVITTENS_HL_BYTE.equals(handelseutfall)) {
			// Kv2
			return handelseutfall;
		}

		return null;
	}

	static ArrayOfIntressentKommunikation createArrayOfIntressentKommunikation(StakeholderDTO stakeholder) {
		List<IntressentKommunikation> intressentKommunikationList = new ArrayList<>();

		if (stakeholder.getPhoneNumber() != null) {
			intressentKommunikationList.add(new IntressentKommunikation()
				.withArAktiv(true)
				.withKomtyp(BYGGR_KOMTYP_HEMTELEFON)
				.withAttention(new IntressentAttention().withAttention(stakeholder.getAddresses().getFirst().getAttention()))
				.withBeskrivning(stakeholder.getPhoneNumber()));
		}
		if (stakeholder.getCellphoneNumber() != null) {
			intressentKommunikationList.add(new IntressentKommunikation()
				.withArAktiv(true)
				.withKomtyp(BYGGR_KOMTYP_MOBIL)
				.withAttention(new IntressentAttention().withAttention(stakeholder.getAddresses().getFirst().getAttention()))
				.withBeskrivning(stakeholder.getCellphoneNumber()));
		}
		if (stakeholder.getEmailAddress() != null) {
			intressentKommunikationList.add(new IntressentKommunikation()
				.withArAktiv(true)
				.withKomtyp(BYGGR_KOMTYP_EPOST)
				.withAttention(new IntressentAttention().withAttention(stakeholder.getAddresses().getFirst().getAttention()))
				.withBeskrivning(stakeholder.getEmailAddress()));
		}
		return new ArrayOfIntressentKommunikation()
			.withIntressentKommunikation(intressentKommunikationList);
	}

	static ArrayOfHandling createArrayOfHandling(final ByggRCaseDTO byggRCase) {
		List<HandelseHandling> handelseHandlingar = new ArrayList<>();
		for (var attachment : byggRCase.getAttachments()) {
			var handelseHandling = new HandelseHandling()
				.withAnteckning(attachment.getName())
				.withStatus(BYGGR_HANDLING_STATUS_INKOMMEN)
				.withTyp(attachment.getCategory())
				.withDokument(new Dokument()
					.withNamn(attachment.getName())
					.withBeskrivning(attachment.getNote())
					.withFil(new DokumentFil()
						.withFilBuffer(Base64.getDecoder().decode(attachment.getFile().getBytes()))
						.withFilAndelse(attachment.getExtension().toLowerCase())));

			handelseHandlingar.add(handelseHandling);
		}
		return new ArrayOfHandling().withHandling(handelseHandlingar);
	}

	/**
	 * Repackages the attachments from the incoming request to a format that ByggR can understand.
	 *
	 * @param byggRCase The incoming request from OpenE
	 * @return ArrayOfHandelseHandling, a list of attachments that the stakeholder sends with the response
	 */
	static ArrayOfHandling createNeighborhoodNotificationArrayOfHandling(final ByggRCaseDTO byggRCase) {
		var handlingar = byggRCase.getAttachments().stream()
			.filter(attachmentDTO -> attachmentDTO.getCategory().equalsIgnoreCase("BIL"))
			.map(attachment -> new HandelseHandling()
				.withAnteckning(attachment.getName())
				.withStatus(Constants.BYGGR_HANDLING_STATUS_INKOMMEN)
				.withTyp("UNDERE")
				.withDokument(new Dokument()
					.withNamn(attachment.getName())
					.withBeskrivning(attachment.getNote())
					.withFil(new DokumentFil()
						.withFilBuffer(Base64.getDecoder().decode(attachment.getFile().getBytes()))
						.withFilAndelse(attachment.getExtension().toLowerCase()))))
			.toList();
		return new ArrayOfHandling().withHandling(handlingar);
	}

	static HandelseIntressent createAddAdditionalDocumentsHandelseIntressent(final StakeholderDTO stakeholder, final String stakeholderId) {
		var handelseIntressent = new HandelseIntressent()
			.withPersOrgNr(stakeholderId)
			.withAdress(Optional.ofNullable(stakeholder.getAddresses()).map(addresses -> addresses.getFirst().getStreet()).orElse(null))
			.withPostNr(Optional.ofNullable(stakeholder.getAddresses()).map(addresses -> addresses.getFirst().getPostalCode()).orElse(null))
			.withOrt(stakeholder.getAddresses().getFirst().getCity())
			.withIntressentKommunikationLista(createArrayOfIntressentKommunikation(stakeholder));

		if (stakeholder instanceof OrganizationDTO organization) {
			handelseIntressent
				.withArForetag(true)
				.withNamn(organization.getOrganizationName());
		}
		if (stakeholder instanceof PersonDTO person) {
			handelseIntressent
				.withArForetag(false)
				.withFornamn(person.getFirstName())
				.withEfternamn(person.getLastName());
		}

		return handelseIntressent;
	}

	static HandelseIntressent createAddCertifiedInspectorHandelseIntressent(final StakeholderDTO stakeholder, final String stakeholderId, final Map<String, String> extraParameters) {
		var handelseIntressent = new HandelseIntressent()
			.withPersOrgNr(stakeholderId)
			.withAdress(stakeholder.getAddresses().getFirst().getStreet())
			.withPostNr(stakeholder.getAddresses().getFirst().getPostalCode())
			.withOrt(stakeholder.getAddresses().getFirst().getCity())
			.withIntressentKommunikationLista(createArrayOfIntressentKommunikation(stakeholder))
			.withAktorbehorighetLista(createAddCertifiedInspectorArrayOfAktorbehorighet(extraParameters))
			.withRollLista(new ArrayOfString2().withRoll("KOA"));

		if (stakeholder instanceof OrganizationDTO organization) {
			handelseIntressent
				.withArForetag(true)
				.withNamn(organization.getOrganizationName());
		}
		if (stakeholder instanceof PersonDTO person) {
			handelseIntressent
				.withArForetag(false)
				.withFornamn(person.getFirstName())
				.withEfternamn(person.getLastName());
		}
		return handelseIntressent;
	}

	static Handelse createAddAdditionalDocumentsHandelse(final String errandInformation, final HandelseIntressent handelseIntressent, final String handelseslag) {
		return new Handelse()
			.withRiktning("In")
			.withRubrik("Kompletterande handlingar")
			.withStartDatum(LocalDateTime.now())
			.withAnteckning(errandInformation)
			.withHandelsetyp("HANDLING")
			.withHandelseslag(handelseslag)
			.withSekretess(false)
			.withMakulerad(false)
			.withIntressentLista(new ArrayOfHandelseIntressent2().withIntressent(handelseIntressent));

	}

	static Handelse createAddCertifiedInspectorHandelse(final String errandInformation, final HandelseIntressent handelseIntressent) {
		return new Handelse()
			.withRiktning("In")
			.withRubrik("Anmälan KA")
			.withStartDatum(LocalDateTime.now())
			.withAnteckning(errandInformation)
			.withHandelseslag("KOMPL")
			.withHandelsetyp("HANDLING")
			.withSekretess(false)
			.withMakulerad(false)
			.withIntressentLista(new ArrayOfHandelseIntressent2().withIntressent(handelseIntressent));
	}

	static ArrayOfAktorbehorighet createAddCertifiedInspectorArrayOfAktorbehorighet(final Map<String, String> extraParameters) {
		return new ArrayOfAktorbehorighet()
			.withAktorbehorighet(new Aktorbehorighet()
				.withBehorighetRoll("KOA")
				.withNiva(extraParameters.get("certificateAuthType"))
				.withNr(extraParameters.get("certificateNumber"))
				.withCertifieradAv(extraParameters.get("certificateIssuer"))
				.withCertifieradTillDatum(LocalDate.parse(extraParameters.get("certificateValidDate"))));
	}

	static SaveNewHandelse createAlertCaseManagerEvent(final String dnr) {
		var alertCaseManagerEvent = new Handelse()
			.withRiktning("In")
			.withRubrik("Manuell hantering krävs")
			.withStartDatum(LocalDateTime.now())
			.withHandelseslag("MANHANT")
			.withHandelsetyp("STATUS")
			.withSekretess(false)
			.withMakulerad(false)
			.withArbetsmaterial(false);

		return new SaveNewHandelse()
			.withMessage(new SaveNewHandelseMessage()
				.withDnr(dnr)
				.withHandlaggarSign("SYSTEM")
				.withHandelse(alertCaseManagerEvent)
				.withAnkomststamplaHandlingar(false)
				.withAutoGenereraBeslutNr(false));
	}

	/**
	 * Extracts a stakeholder from a specific byggR event based on the stakeholder id.
	 *
	 * @param handelse the event
	 * @param stakeholderId the stakeholder id
	 * @return HandelseIntressent, the stakeholder of a specific event
	 */
	static HandelseIntressent extractIntressentFromEvent(final Handelse handelse, final String stakeholderId) {
		final var intressent = handelse.getIntressentLista().getIntressent().stream()
			.filter(intressent1 -> (intressent1.getPersOrgNr().equals(stakeholderId) || intressent1.getPersOrgNr().equals(stakeholderId.substring(2))))
			.findFirst().orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "Stakeholder with id %s not found in ByggRCase".formatted(stakeholderId)));

		var intressentKommunkationLista = new ArrayOfIntressentKommunikation()
			.withIntressentKommunikation(intressent.getIntressentKommunikationLista().getIntressentKommunikation().stream()
				.map(intressentKommunikation -> new IntressentKommunikation()
					.withBeskrivning(intressentKommunikation.getBeskrivning())
					.withAttention(new IntressentAttention().withAttention(Optional.ofNullable(intressentKommunikation.getAttention()).map(IntressentAttention::getAttention).orElse(null)))
					.withKomtyp(intressentKommunikation.getKomtyp()))
				.toList());

		return new HandelseIntressent()
			.withIntressentId(intressent.getIntressentId())
			.withIntressentVersionId(intressent.getIntressentVersionId())
			.withIntressentKommunikationLista(intressentKommunkationLista)
			.withRollLista(intressent.getRollLista());
	}

	static Handelse extractEvent(final Arende arende, final Integer handelseId) {
		return arende.getHandelseLista().getHandelse().stream()
			.filter(handelse -> handelse.getHandelseId() == handelseId)
			.findFirst().orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No handelse with id %s found in ByggRCase".formatted(handelseId)));
	}

	/**
	 * Creates a SaveNewHandelse object with the given parameters, used for NEIGHBORHOOD_NOTIFICATION cases.
	 *
	 * @param dnr The case number
	 * @param handelse The ByggR event
	 * @param arrayOfHandling The attachments that the stakeholder sends with the response
	 * @return SaveNewHandelse, a request model that is sent to ByggR
	 */
	static SaveNewHandelse createSaveNewHandelse(final String dnr, final Handelse handelse, final ArrayOfHandling arrayOfHandling, final Integer besvaradHandelseId) {
		return new SaveNewHandelse()

			.withMessage(new SaveNewHandelseMessage()
				.withDnr(dnr)
				.withBesvaradHandelseId(besvaradHandelseId)
				.withHandlaggarSign("SYSTEM")
				.withHandelse(handelse)
				.withHandlingar(arrayOfHandling));
	}

	/**
	 * Creates a new Handelse object with the given parameters.
	 *
	 * @param comment String that determines if the stakeholder has any issues with the building permit
	 * @param errandInformation String that contains the stakeholders comment. (Bad name given by OpenE)
	 * @param intressent HandelseIntressent
	 * @param stakeholderName The stakeholder name
	 * @param propertyDesignation The property designation
	 * @return Handelse, a new event in a ByggR Case
	 */
	static Handelse createNewEvent(final String comment, final String errandInformation, final HandelseIntressent intressent, final String stakeholderName, final String propertyDesignation) {
		var isOpposed = comment.equals("Jag har synpunkter");
		var opinion = isOpposed ? "Grannehörande Svar med erinran" : "Grannehörande Svar utan erinran";

		var title = opinion + ", " + propertyDesignation + ", " + stakeholderName;

		return new Handelse()
			.withRiktning("In")
			.withHandelseslag("GRASVA")
			.withHandelsetyp("GRANHO")
			.withRubrik(title)
			.withAnteckning(errandInformation)
			.withSekretess(false)
			.withMakulerad(false)
			.withIntressentLista(new ArrayOfHandelseIntressent2().withIntressent(intressent));
	}

}
