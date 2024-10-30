package se.sundsvall.casemanagement.integration.byggr;

import static se.sundsvall.casemanagement.integration.byggr.ByggrMapper.filterPersonId;

import java.util.List;
import java.util.Optional;

import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.util.Constants;

import arendeexport.Arende;
import arendeexport.ArendeIntressent;

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
			byggrAdminMessageSb.append(byggrAdminMessageSb.toString().isEmpty() ? "" : "\n\n").append(note);
		}
	}

	static boolean isCaseClosed(final Arende arende) {
		return arende.getStatus() != null && Constants.BYGGR_STATUS_AVSLUTAT.equals(arende.getStatus());
	}

	static boolean hasHandelseList(final Arende arende) {
		return arende.getHandelseLista() != null && arende.getHandelseLista().getHandelse() != null;
	}

}
