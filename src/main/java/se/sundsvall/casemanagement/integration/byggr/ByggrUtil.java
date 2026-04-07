package se.sundsvall.casemanagement.integration.byggr;

import arendeexport.Arende;
import arendeexport.ArendeIntressent;
import java.util.List;
import java.util.Optional;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;
import se.sundsvall.casemanagement.service.util.LegalIdUtility;
import se.sundsvall.dept44.problem.Problem;

import static generated.client.party.PartyType.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.filterPersonId;

public final class ByggrUtil {

	private ByggrUtil() {
		// Intentionally empty
	}

	static boolean containsControlOfficial(final List<StakeholderDTO> stakeholders) {

		return stakeholders.stream()
			.anyMatch(dto -> dto.getRoles().contains(StakeholderRole.CONTROL_OFFICIAL.toString()));
	}

	static boolean containsPersonDuplicates(final List<StakeholderDTO> stakeholders) {
		final List<String> personIds = filterPersonId(stakeholders);
		// If the request contains two person with the same personId, it must be handled manually
		return stakeholders.stream()
			.anyMatch(dto -> dto instanceof final PersonDTO personDTO && (personIds.stream()
				.filter(personId -> personId.equals(personDTO.getPersonId())).count() > 1));
	}

	static boolean containsPropertyOwner(final List<ArendeIntressent> stakeholders) {
		return stakeholders.stream()
			.anyMatch(stakeholder -> stakeholder.getRollLista().getRoll().contains(StakeholderRole.PROPERTY_OWNER.getText()));
	}

	static String getPropertyDesignation(final List<FacilityDTO> facilities) {
		return Optional.ofNullable(getMainOrTheOnlyFacility(facilities))
			.map(planningPermissionFacilityDTO -> planningPermissionFacilityDTO.getAddress()
				.getPropertyDesignation().trim().toUpperCase())
			.orElse(null);
	}

	static Boolean isWithinPlan(final List<FacilityDTO> facilities) {
		return facilities.stream()
			.findFirst()
			.map(FacilityDTO::getAddress)
			.map(AddressDTO::getIsZoningPlanArea)
			.orElse(null);
	}

	static String parsePropertyDesignation(final List<FacilityDTO> facilities) {
		var propertyDesignation = getPropertyDesignation(facilities);
		if ((propertyDesignation != null) && propertyDesignation.startsWith("SUNDSVALL ")) {
			propertyDesignation = propertyDesignation.substring(propertyDesignation.indexOf(" ") + 1);
		}
		return propertyDesignation;
	}

	static FacilityDTO getMainOrTheOnlyFacility(final List<FacilityDTO> facilities) {
		if (facilities.size() == 1) {
			// The list only contains one facility, return it.
			return facilities.getFirst();
		}

		// If the list contains more than one facility and mainFacility exists, return it.
		// If the list doesn't contain a mainFacility, return null.
		return facilities.stream().anyMatch(FacilityDTO::isMainFacility) ? facilities.stream().filter(FacilityDTO::isMainFacility).toList().getFirst() : null;
	}

	static void writeEventNote(final String note, final StringBuilder byggrAdminMessageSb) {
		if (!byggrAdminMessageSb.toString().contains(note)) {
			byggrAdminMessageSb.append(byggrAdminMessageSb.isEmpty() ? "" : "\n\n").append(note);
		}
	}

	static boolean isCaseClosed(final Arende arende, final String closedStatus) {
		return arende.getStatus() != null && closedStatus.equals(arende.getStatus());
	}

	static boolean hasHandelseList(final Arende arende) {
		return arende.getHandelseLista() != null && arende.getHandelseLista().getHandelse() != null;
	}

	/**
	 * The incoming request might have one or two stakeholders. If any stakeholder is of type Organization, we should use
	 * the organization number as stakeholderId. If no organization is found, we should use the personId to fetch a personal
	 * number from
	 * partyIntegration and use this personal number as the stakeholder id.
	 *
	 * @param  stakeholders     List of stakeholders
	 * @param  municipalityId   Municipality ID
	 * @param  partyIntegration PartyIntegration for looking up legal IDs
	 * @return                  String, organization number, or personal number of the stakeholder.
	 */
	public static String extractStakeholderId(final List<StakeholderDTO> stakeholders, final String municipalityId, final PartyIntegration partyIntegration) {
		final var organizationId = stakeholders.stream()
			.filter(OrganizationDTO.class::isInstance)
			.findFirst()
			.map(stakeholder -> ((OrganizationDTO) stakeholder).getOrganizationNumber())
			.map(LegalIdUtility::prefixOrgNr)
			.map(LegalIdUtility::addHyphen)
			.orElse(null);

		if (organizationId != null) {
			return organizationId;
		}

		return stakeholders.stream()
			.filter(PersonDTO.class::isInstance)
			.findFirst()
			.map(stakeholder -> ((PersonDTO) stakeholder).getPersonId())
			.map((final String personId) -> partyIntegration.getLegalIdByPartyId(municipalityId, personId).get(PRIVATE))
			.map(LegalIdUtility::addHyphen)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "No stakeholder found in the incoming request."));
	}

}
