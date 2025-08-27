package se.sundsvall.casemanagement.api.model;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_AV_LIVSMEDELSVERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_BARANDE_KONSTRUKTION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_BRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_PLANLOSNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_VA;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANDRING_VENTILATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANLAGGNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ANDRING_AVLOPPSANORDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_AVHJALPANDEATGARD_FORORENING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_ELDSTAD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_HALSOSKYDDSVERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLATION_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_KOMPOSTERING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANMALAN_RIVNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_RIVNINGSLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.ANSOKAN_TILLSTAND_VARMEPUMP;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.BYGGR_ADDITIONAL_DOCUMENTS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.BYGGR_ADD_CERTIFIED_INSPECTOR;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INSTALLATION_VA;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INSTALLATION_VENTILATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.INSTALLLATION_HISS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.LOST_PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_FYLL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_OVRIGT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_SCHAKTNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MARKLOV_TRADFALLNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_APPLICATION_FOR_ROAD_ALLOWANCE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_BUILDING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_BUY_LAND_FROM_THE_MUNICIPALITY;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_BUY_LAND_FROM_THE_MUNICIPALITY_BUSINESS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_BUY_LAND_FROM_THE_MUNICIPALITY_PRIVATE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_BUY_SMALL_HOUSE_PLOT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_EARLY_DIALOG_PLAN_NOTIFICATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_EASEMENT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_FORWARDED_FROM_CONTACTSUNDSVALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_HUNTING_LEASE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_INVASIVE_SPECIES;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_INVOICE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LAND_INSTRUCTION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LAND_RESERVATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LAND_RIGHT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LAND_SURVEYING_OFFICE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LEASE_REQUEST;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_LITTERING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_OTHER;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_PROTECTIVE_HUNTING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_PUBLIC_SPACE_LEASE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_REFERRAL_CONSULTATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_REQUEST_FOR_PUBLIC_DOCUMENT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_RETURNED_TO_CONTACT_SUNDSVALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_ROAD_ASSOCIATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_SELL_LAND_TO_THE_MUNICIPALITY;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_SMALL_BOAT_HARBOR_DOCK_PORT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_SQUARE_PLACE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_STORMWATER;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_TERMINATION_OF_LEASE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_TRAINING_SEMINAR;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_TREES_FORESTS;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.MEX_UNAUTHORIZED_RESIDENCE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NEIGHBORHOOD_NOTIFICATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NYBYGGNAD_FORHANDSBESKED;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_BUS_CARD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_CHANGE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NATIONAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NATIONAL_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION_BUS_CARD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION_CHANGE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION_NATIONAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_NOTIFICATION_RIAK;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARATRANSIT_RIAK;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.REGISTRERING_AV_LIVSMEDEL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_ANDRAD_ANVANDNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_ANLAGGANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_ANORDNANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_NYBYGGNAD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.STRANDSKYDD_OVRIGT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.TILLBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.UPPDATERING_RISKKLASSNING;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.UPPSATTANDE_SKYLT;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "caseType", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
@JsonSubTypes({
	@Type(value = ByggRCaseDTO.class, names = {
		NYBYGGNAD_ANSOKAN_OM_BYGGLOV,
		NYBYGGNAD_FORHANDSBESKED,
		ANMALAN_ATTEFALL,
		UPPSATTANDE_SKYLT,
		TILLBYGGNAD_ANSOKAN_OM_BYGGLOV,
		ANDRING_ANSOKAN_OM_BYGGLOV,
		STRANDSKYDD_NYBYGGNAD,
		STRANDSKYDD_ANDRAD_ANVANDNING,
		STRANDSKYDD_ANORDNANDE,
		STRANDSKYDD_ANLAGGANDE,
		ANMALAN_ELDSTAD,
		ANDRING_VENTILATION,
		INSTALLATION_VENTILATION,
		ANDRING_VA,
		INSTALLATION_VA,
		ANDRING_PLANLOSNING,
		ANDRING_BARANDE_KONSTRUKTION,
		ANDRING_BRANDSKYDD,
		INSTALLLATION_HISS,
		MARKLOV_SCHAKTNING,
		MARKLOV_FYLL,
		MARKLOV_TRADFALLNING,
		MARKLOV_OVRIGT,
		STRANDSKYDD_OVRIGT,
		NEIGHBORHOOD_NOTIFICATION,
		BYGGR_ADD_CERTIFIED_INSPECTOR,
		BYGGR_ADDITIONAL_DOCUMENTS,
		ANSOKAN_RIVNINGSLOV,
		ANMALAN_RIVNING
	}),
	@Type(value = EcosCaseDTO.class, names = {
		REGISTRERING_AV_LIVSMEDEL,
		ANMALAN_INSTALLATION_VARMEPUMP,
		ANSOKAN_TILLSTAND_VARMEPUMP,
		ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC,
		ANMALAN_ANDRING_AVLOPPSANLAGGNING,
		ANMALAN_ANDRING_AVLOPPSANORDNING,
		ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP,
		UPPDATERING_RISKKLASSNING,
		ANMALAN_HALSOSKYDDSVERKSAMHET,
		ANMALAN_KOMPOSTERING,
		ANMALAN_AVHJALPANDEATGARD_FORORENING,
		ANDRING_AV_LIVSMEDELSVERKSAMHET,
		INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET
	}),
	@Type(value = OtherCaseDTO.class, names = {
		PARKING_PERMIT,
		LOST_PARKING_PERMIT,
		PARKING_PERMIT_RENEWAL,
		PARATRANSIT,
		PARATRANSIT_RENEWAL,
		PARATRANSIT_CHANGE,
		PARATRANSIT_NATIONAL,
		PARATRANSIT_NATIONAL_RENEWAL,
		PARATRANSIT_RIAK,
		PARATRANSIT_BUS_CARD,
		PARATRANSIT_NOTIFICATION,
		PARATRANSIT_NOTIFICATION_CHANGE,
		PARATRANSIT_NOTIFICATION_RENEWAL,
		PARATRANSIT_NOTIFICATION_NATIONAL,
		PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL,
		PARATRANSIT_NOTIFICATION_RIAK,
		PARATRANSIT_NOTIFICATION_BUS_CARD,
		MEX_LEASE_REQUEST,
		MEX_BUY_LAND_FROM_THE_MUNICIPALITY,
		MEX_SELL_LAND_TO_THE_MUNICIPALITY,
		MEX_BUY_SMALL_HOUSE_PLOT,
		MEX_APPLICATION_FOR_ROAD_ALLOWANCE,
		MEX_UNAUTHORIZED_RESIDENCE,
		MEX_LAND_RIGHT,
		MEX_EARLY_DIALOG_PLAN_NOTIFICATION,
		MEX_PROTECTIVE_HUNTING,
		MEX_LAND_INSTRUCTION,
		MEX_OTHER,
		MEX_LAND_SURVEYING_OFFICE,
		MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE,
		MEX_INVOICE,
		MEX_REQUEST_FOR_PUBLIC_DOCUMENT,
		MEX_TERMINATION_OF_LEASE,
		MEX_HUNTING_LEASE,
		MEX_SQUARE_PLACE,
		MEX_FORWARDED_FROM_CONTACTSUNDSVALL,
		MEX_BUILDING_PERMIT,
		MEX_STORMWATER,
		MEX_INVASIVE_SPECIES,
		MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL,
		MEX_LITTERING,
		MEX_REFERRAL_CONSULTATION,
		MEX_PUBLIC_SPACE_LEASE,
		MEX_EASEMENT,
		MEX_TREES_FORESTS,
		MEX_ROAD_ASSOCIATION,
		MEX_RETURNED_TO_CONTACT_SUNDSVALL,
		MEX_SMALL_BOAT_HARBOR_DOCK_PORT,
		MEX_BUY_LAND_FROM_THE_MUNICIPALITY_PRIVATE,
		MEX_BUY_LAND_FROM_THE_MUNICIPALITY_BUSINESS,
		MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE,
		MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS,
		MEX_TRAINING_SEMINAR,
		MEX_LAND_RESERVATION
	})
})
@Data
@SuperBuilder(setterPrefix = "with")
@NoArgsConstructor
@Schema(description = "Base case model")
public abstract class CaseDTO {

	@NotBlank
	@Schema(description = "Case ID from the client.", example = "caa230c6-abb4-4592-ad9a-34e263c2787b")
	private String externalCaseId;

	@NotNull
	@Schema(description = "The case type", example = "NYBYGGNAD_ANSOKAN_OM_BYGGLOV")
	private String caseType;

	@Schema(description = "Some description of the case.", example = "En fritextbeskrivning av case.")
	private String description;

	@Schema(description = "Additions to the case title. Right now only applicable to cases of CaseType: NYBYGGNAD_ANSOKAN_OM_BYGGLOV.", example = "Eldstad/rökkanal, Skylt")
	private String caseTitleAddition;

	@NotEmpty
	@Valid
	@Schema(description = "The stakeholders in the case", oneOf = {
		PersonDTO.class, OrganizationDTO.class
	})
	private List<StakeholderDTO> stakeholders;

	@NotEmpty
	@Valid
	@Schema(description = "The attachments in the case")
	private List<AttachmentDTO> attachments;

	@Schema(description = "Extra parameters for the case.")
	private Map<String, String> extraParameters;

}
