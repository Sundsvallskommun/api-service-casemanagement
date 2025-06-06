package se.sundsvall.casemanagement.api.model.enums;

import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Getter;

@Getter
@Schema(description = "Case types", example = NYBYGGNAD_ANSOKAN_OM_BYGGLOV)
public enum CaseType {

	// =================== Byggr ===================

	// BYGGR BYGGLOV
	NYBYGGNAD_ANSOKAN_OM_BYGGLOV(Value.NYBYGGNAD_ANSOKAN_OM_BYGGLOV),
	TILLBYGGNAD_ANSOKAN_OM_BYGGLOV(Value.TILLBYGGNAD_ANSOKAN_OM_BYGGLOV),
	UPPSATTANDE_SKYLT(Value.UPPSATTANDE_SKYLT),
	ANDRING_ANSOKAN_OM_BYGGLOV(Value.ANDRING_ANSOKAN_OM_BYGGLOV),

	NYBYGGNAD_FORHANDSBESKED(Value.NYBYGGNAD_FORHANDSBESKED),

	// CASETYPES FOR PUT IN BYGGR
	NEIGHBORHOOD_NOTIFICATION(Value.NEIGHBORHOOD_NOTIFICATION),
	BYGGR_ADD_CERTIFIED_INSPECTOR(Value.BYGGR_ADD_CERTIFIED_INSPECTOR),
	BYGGR_ADDITIONAL_DOCUMENTS(Value.BYGGR_ADDITIONAL_DOCUMENTS),

	// BYGGR STRANDSKYDD
	STRANDSKYDD_NYBYGGNAD(Value.STRANDSKYDD_NYBYGGNAD),
	STRANDSKYDD_ANLAGGANDE(Value.STRANDSKYDD_ANLAGGANDE),
	STRANDSKYDD_ANORDNANDE(Value.STRANDSKYDD_ANORDNANDE),
	STRANDSKYDD_ANDRAD_ANVANDNING(Value.STRANDSKYDD_ANDRAD_ANVANDNING),
	STRANDSKYDD_OVRIGT(Value.STRANDSKYDD_OVRIGT),

	// ANMALAN
	ANDRING_VENTILATION(Value.ANDRING_VENTILATION),
	INSTALLATION_VENTILATION(Value.INSTALLATION_VENTILATION),
	ANDRING_VA(Value.ANDRING_VA),
	INSTALLATION_VA(Value.INSTALLATION_VA),
	ANDRING_PLANLOSNING(Value.ANDRING_PLANLOSNING),
	ANDRING_BARANDE_KONSTRUKTION(Value.ANDRING_BARANDE_KONSTRUKTION),
	ANDRING_BRANDSKYDD(Value.ANDRING_BRANDSKYDD),
	INSTALLLATION_HISS(Value.INSTALLLATION_HISS),
	ANSOKAN_RIVNINGSLOV(Value.ANSOKAN_RIVNINGSLOV),
	ANMALAN_RIVNING(Value.ANMALAN_RIVNING),

	// MARKLOV
	MARKLOV_SCHAKTNING(Value.MARKLOV_SCHAKTNING),
	MARKLOV_FYLL(Value.MARKLOV_FYLL),
	MARKLOV_TRADFALLNING(Value.MARKLOV_TRADFALLNING),
	MARKLOV_OVRIGT(Value.MARKLOV_OVRIGT),

	// BYGGR OTHER
	ANMALAN_ATTEFALL(Value.ANMALAN_ATTEFALL),
	ANMALAN_ELDSTAD(Value.ANMALAN_ELDSTAD),

	// =================== ECOS ===================
	REGISTRERING_AV_LIVSMEDEL(Value.REGISTRERING_AV_LIVSMEDEL),
	ANMALAN_INSTALLATION_VARMEPUMP(Value.ANMALAN_INSTALLATION_VARMEPUMP),
	ANSOKAN_TILLSTAND_VARMEPUMP(Value.ANSOKAN_TILLSTAND_VARMEPUMP),
	ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP(Value.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP),
	ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC(Value.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC),
	ANMALAN_ANDRING_AVLOPPSANLAGGNING(Value.ANMALAN_ANDRING_AVLOPPSANLAGGNING),
	ANMALAN_ANDRING_AVLOPPSANORDNING(Value.ANMALAN_ANDRING_AVLOPPSANORDNING),
	ANMALAN_HALSOSKYDDSVERKSAMHET(Value.ANMALAN_HALSOSKYDDSVERKSAMHET),
	UPPDATERING_RISKKLASSNING(Value.UPPDATERING_RISKKLASSNING),
	ANMALAN_KOMPOSTERING(Value.ANMALAN_KOMPOSTERING),
	ANMALAN_AVHJALPANDEATGARD_FORORENING(Value.ANMALAN_AVHJALPANDEATGARD_FORORENING),
	ANDRING_AV_LIVSMEDELSVERKSAMHET(Value.ANDRING_AV_LIVSMEDELSVERKSAMHET),
	INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET(Value.INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET),

	// =================== CASEDATA ===================
	// =================== PRH ===================

	PARKING_PERMIT(Value.PARKING_PERMIT),
	PARKING_PERMIT_RENEWAL(Value.PARKING_PERMIT_RENEWAL),
	LOST_PARKING_PERMIT(Value.LOST_PARKING_PERMIT),

	// Färdtjänst
	// Ansökan av färdtjänst
	PARATRANSIT(Value.PARATRANSIT),
	// Ansökan om fortsatt färdtjänst
	PARATRANSIT_RENEWAL(Value.PARATRANSIT_RENEWAL),
	// Ansökan om förändring av insatser
	PARATRANSIT_CHANGE(Value.PARATRANSIT_CHANGE),
	// Ansökan om riksfärdtjänst
	PARATRANSIT_NATIONAL(Value.PARATRANSIT_NATIONAL),
	// Ansökan om fortsatt riksfärdtjänst
	PARATRANSIT_NATIONAL_RENEWAL(Value.PARATRANSIT_NATIONAL_RENEWAL),
	// Ansökan om RIAK
	PARATRANSIT_RIAK(Value.PARATRANSIT_RIAK),
	// Ansökan om busskort
	PARATRANSIT_BUS_CARD(Value.PARATRANSIT_BUS_CARD),
	// Anmälan av färdtjänst
	PARATRANSIT_NOTIFICATION(Value.PARATRANSIT_NOTIFICATION),
	// Anmälan om förändring av insatser
	PARATRANSIT_NOTIFICATION_CHANGE(Value.PARATRANSIT_NOTIFICATION_CHANGE),
	// Anmälan om fortsatt färdtjänst
	PARATRANSIT_NOTIFICATION_RENEWAL(Value.PARATRANSIT_NOTIFICATION_RENEWAL),
	// Anmälan om riksfärdtjänst
	PARATRANSIT_NOTIFICATION_NATIONAL(Value.PARATRANSIT_NOTIFICATION_NATIONAL),
	// Anmälan om fortsatt riksfärdtjänst
	PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL(Value.PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL),
	// Anmälan om RIAK
	PARATRANSIT_NOTIFICATION_RIAK(Value.PARATRANSIT_NOTIFICATION_RIAK),
	PARATRANSIT_NOTIFICATION_BUS_CARD(Value.PARATRANSIT_NOTIFICATION_BUS_CARD),
	// =================== MEX ===================

	MEX_LEASE_REQUEST(Value.MEX_LEASE_REQUEST),
	MEX_BUY_LAND_FROM_THE_MUNICIPALITY(Value.MEX_BUY_LAND_FROM_THE_MUNICIPALITY),
	MEX_SELL_LAND_TO_THE_MUNICIPALITY(Value.MEX_SELL_LAND_TO_THE_MUNICIPALITY),
	MEX_BUY_SMALL_HOUSE_PLOT(Value.MEX_BUY_SMALL_HOUSE_PLOT),
	MEX_APPLICATION_FOR_ROAD_ALLOWANCE(Value.MEX_APPLICATION_FOR_ROAD_ALLOWANCE),
	MEX_UNAUTHORIZED_RESIDENCE(Value.MEX_UNAUTHORIZED_RESIDENCE),
	MEX_LAND_RIGHT(Value.MEX_LAND_RIGHT),
	MEX_EARLY_DIALOG_PLAN_NOTIFICATION(Value.MEX_EARLY_DIALOG_PLAN_NOTIFICATION),
	MEX_PROTECTIVE_HUNTING(Value.MEX_PROTECTIVE_HUNTING),
	MEX_LAND_INSTRUCTION(Value.MEX_LAND_INSTRUCTION),
	MEX_OTHER(Value.MEX_OTHER),
	MEX_LAND_SURVEYING_OFFICE(Value.MEX_LAND_SURVEYING_OFFICE),
	MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE(Value.MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE),
	MEX_INVOICE(Value.MEX_INVOICE),
	MEX_REQUEST_FOR_PUBLIC_DOCUMENT(Value.MEX_REQUEST_FOR_PUBLIC_DOCUMENT),
	MEX_TERMINATION_OF_LEASE(Value.MEX_TERMINATION_OF_LEASE),
	MEX_HUNTING_LEASE(Value.MEX_HUNTING_LEASE),

	MEX_SQUARE_PLACE(Value.MEX_SQUARE_PLACE),
	MEX_FORWARDED_FROM_CONTACTSUNDSVALL(Value.MEX_FORWARDED_FROM_CONTACTSUNDSVALL),
	MEX_BUILDING_PERMIT(Value.MEX_BUILDING_PERMIT),
	MEX_STORMWATER(Value.MEX_STORMWATER),
	MEX_INVASIVE_SPECIES(Value.MEX_INVASIVE_SPECIES),
	MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL(Value.MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL),
	MEX_LITTERING(Value.MEX_LITTERING),
	MEX_REFERRAL_CONSULTATION(Value.MEX_REFERRAL_CONSULTATION),
	MEX_PUBLIC_SPACE_LEASE(Value.MEX_PUBLIC_SPACE_LEASE),
	MEX_EASEMENT(Value.MEX_EASEMENT),
	MEX_TREES_FORESTS(Value.MEX_TREES_FORESTS),
	MEX_ROAD_ASSOCIATION(Value.MEX_ROAD_ASSOCIATION),
	MEX_RETURNED_TO_CONTACT_SUNDSVALL(Value.MEX_RETURNED_TO_CONTACT_SUNDSVALL),
	MEX_SMALL_BOAT_HARBOR_DOCK_PORT(Value.MEX_SMALL_BOAT_HARBOR_DOCK_PORT),
	MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE(Value.MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE),
	MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS(Value.MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS),
	MEX_TRAINING_SEMINAR(Value.MEX_TRAINING_SEMINAR),
	MEX_LAND_RESERVATION(Value.MEX_LAND_RESERVATION);

	public static final Set<String> WITH_NULLABLE_FACILITY_TYPE = Set.of(
		MARKLOV_SCHAKTNING.toString(),
		MARKLOV_FYLL.toString(),
		MARKLOV_TRADFALLNING.toString(),
		MARKLOV_OVRIGT.toString(),
		STRANDSKYDD_OVRIGT.toString(),
		ANMALAN_KOMPOSTERING.toString(),
		ANMALAN_AVHJALPANDEATGARD_FORORENING.toString());

	public static final Set<CaseType> MEX_CASE_TYPES = Set.of(
		MEX_LEASE_REQUEST,
		MEX_BUY_LAND_FROM_THE_MUNICIPALITY,
		MEX_SELL_LAND_TO_THE_MUNICIPALITY,
		MEX_SQUARE_PLACE,
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
		MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE,
		MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS,
		MEX_TRAINING_SEMINAR,
		MEX_LAND_RESERVATION);

	public static final Set<CaseType> PRH_CASE_TYPES = Set.of(
		PARKING_PERMIT,
		PARKING_PERMIT_RENEWAL,
		LOST_PARKING_PERMIT,
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
		PARATRANSIT_NOTIFICATION_BUS_CARD);

	private final String value;

	CaseType(final String value) {
		this.value = value;
	}

	public static final class Value {

		public static final String ANSOKAN_RIVNINGSLOV = "ANSOKAN_RIVNINGSLOV";

		public static final String ANMALAN_RIVNING = "ANMALAN_RIVNING";

		public static final String BYGGR_ADDITIONAL_DOCUMENTS = "BYGGR_ADDITIONAL_DOCUMENTS";

		public static final String BYGGR_ADD_CERTIFIED_INSPECTOR = "BYGGR_ADD_CERTIFIED_INSPECTOR";

		public static final String NEIGHBORHOOD_NOTIFICATION = "NEIGHBORHOOD_NOTIFICATION";

		public static final String UPPSATTANDE_SKYLT = "UPPSATTANDE_SKYLT";

		public static final String TILLBYGGNAD_ANSOKAN_OM_BYGGLOV = "TILLBYGGNAD_ANSOKAN_OM_BYGGLOV";

		public static final String ANDRING_ANSOKAN_OM_BYGGLOV = "ANDRING_ANSOKAN_OM_BYGGLOV";

		public static final String STRANDSKYDD_NYBYGGNAD = "STRANDSKYDD_NYBYGGNAD";

		public static final String STRANDSKYDD_ANDRAD_ANVANDNING = "STRANDSKYDD_ANDRAD_ANVANDNING";

		public static final String STRANDSKYDD_ANORDNANDE = "STRANDSKYDD_ANORDNANDE";

		public static final String STRANDSKYDD_ANLAGGANDE = "STRANDSKYDD_ANLAGGANDE";

		public static final String ANMALAN_ELDSTAD = "ANMALAN_ELDSTAD";

		public static final String NYBYGGNAD_FORHANDSBESKED = "NYBYGGNAD_FORHANDSBESKED";

		public static final String NYBYGGNAD_ANSOKAN_OM_BYGGLOV = "NYBYGGNAD_ANSOKAN_OM_BYGGLOV";

		public static final String ANMALAN_ATTEFALL = "ANMALAN_ATTEFALL";

		public static final String REGISTRERING_AV_LIVSMEDEL = "REGISTRERING_AV_LIVSMEDEL";

		public static final String ANMALAN_INSTALLATION_VARMEPUMP = "ANMALAN_INSTALLATION_VARMEPUMP";

		public static final String ANSOKAN_TILLSTAND_VARMEPUMP = "ANSOKAN_TILLSTAND_VARMEPUMP";

		public static final String ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC = "ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC";

		public static final String ANMALAN_ANDRING_AVLOPPSANLAGGNING = "ANMALAN_ANDRING_AVLOPPSANLAGGNING";

		public static final String ANMALAN_ANDRING_AVLOPPSANORDNING = "ANMALAN_ANDRING_AVLOPPSANORDNING";

		public static final String ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP = "ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP";

		public static final String ANMALAN_HALSOSKYDDSVERKSAMHET = "ANMALAN_HALSOSKYDDSVERKSAMHET";

		public static final String PARKING_PERMIT = "PARKING_PERMIT";

		public static final String PARKING_PERMIT_RENEWAL = "PARKING_PERMIT_RENEWAL";

		public static final String LOST_PARKING_PERMIT = "LOST_PARKING_PERMIT";

		public static final String UPPDATERING_RISKKLASSNING = "UPPDATERING_RISKKLASSNING";

		public static final String ANDRING_VENTILATION = "ANDRING_VENTILATION";

		public static final String INSTALLATION_VENTILATION = "INSTALLATION_VENTILATION";

		public static final String ANDRING_VA = "ANDRING_VA";

		public static final String INSTALLATION_VA = "INSTALLATION_VA";

		public static final String ANDRING_PLANLOSNING = "ANDRING_PLANLOSNING";

		public static final String ANDRING_BRANDSKYDD = "ANDRING_BRANDSKYDD";

		public static final String INSTALLLATION_HISS = "INSTALLLATION_HISS";

		public static final String MARKLOV_SCHAKTNING = "MARKLOV_SCHAKTNING";

		public static final String MARKLOV_FYLL = "MARKLOV_FYLL";

		public static final String MARKLOV_TRADFALLNING = "MARKLOV_TRADFALLNING";

		public static final String MARKLOV_OVRIGT = "MARKLOV_OVRIGT";

		public static final String STRANDSKYDD_OVRIGT = "STRANDSKYDD_OVRIGT";

		public static final String ANDRING_BARANDE_KONSTRUKTION = "ANDRING_BARANDE_KONSTRUKTION";

		public static final String ANMALAN_KOMPOSTERING = "ANMALAN_KOMPOSTERING";

		public static final String ANMALAN_AVHJALPANDEATGARD_FORORENING = "ANMALAN_AVHJALPANDEATGARD_FORORENING";

		public static final String ANDRING_AV_LIVSMEDELSVERKSAMHET = "ANDRING_AV_LIVSMEDELSVERKSAMHET";

		public static final String INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET = "INFORMATION_OM_UPPHORANDE_AV_VERKSAMHET";

		public static final String MEX_LEASE_REQUEST = "MEX_LEASE_REQUEST";

		public static final String MEX_BUY_LAND_FROM_THE_MUNICIPALITY = "MEX_BUY_LAND_FROM_THE_MUNICIPALITY";

		public static final String MEX_SELL_LAND_TO_THE_MUNICIPALITY = "MEX_SELL_LAND_TO_THE_MUNICIPALITY";

		public static final String MEX_BUY_SMALL_HOUSE_PLOT = "MEX_BUY_SMALL_HOUSE_PLOT";

		public static final String MEX_APPLICATION_FOR_ROAD_ALLOWANCE = "MEX_APPLICATION_FOR_ROAD_ALLOWANCE";

		public static final String MEX_UNAUTHORIZED_RESIDENCE = "MEX_UNAUTHORIZED_RESIDENCE";

		public static final String MEX_LAND_RIGHT = "MEX_LAND_RIGHT";

		public static final String MEX_EARLY_DIALOG_PLAN_NOTIFICATION = "MEX_EARLY_DIALOG_PLAN_NOTIFICATION";

		public static final String MEX_PROTECTIVE_HUNTING = "MEX_PROTECTIVE_HUNTING";

		public static final String MEX_LAND_INSTRUCTION = "MEX_LAND_INSTRUCTION";

		public static final String MEX_OTHER = "MEX_OTHER";

		public static final String MEX_LAND_SURVEYING_OFFICE = "MEX_LAND_SURVEYING_OFFICE";

		public static final String MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE = "MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE";

		public static final String MEX_INVOICE = "MEX_INVOICE";

		public static final String MEX_REQUEST_FOR_PUBLIC_DOCUMENT = "MEX_REQUEST_FOR_PUBLIC_DOCUMENT";

		public static final String MEX_TERMINATION_OF_LEASE = "MEX_TERMINATION_OF_LEASE";

		public static final String MEX_HUNTING_LEASE = "MEX_HUNTING_LEASE";
		public static final String MEX_SQUARE_PLACE = "MEX_SQUARE_PLACE";
		public static final String MEX_FORWARDED_FROM_CONTACTSUNDSVALL = "MEX_FORWARDED_FROM_CONTACTSUNDSVALL";
		public static final String MEX_BUILDING_PERMIT = "MEX_BUILDING_PERMIT";
		public static final String MEX_SMALL_BOAT_HARBOR_DOCK_PORT = "MEX_SMALL_BOAT_HARBOR_DOCK_PORT";
		public static final String MEX_RETURNED_TO_CONTACT_SUNDSVALL = "MEX_RETURNED_TO_CONTACT_SUNDSVALL";
		public static final String MEX_ROAD_ASSOCIATION = "MEX_ROAD_ASSOCIATION";
		public static final String MEX_TREES_FORESTS = "MEX_TREES_FORESTS";
		public static final String MEX_STORMWATER = "MEX_STORMWATER";
		public static final String MEX_INVASIVE_SPECIES = "MEX_INVASIVE_SPECIES";
		public static final String MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL = "MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL";
		public static final String MEX_LITTERING = "MEX_LITTERING";
		public static final String MEX_REFERRAL_CONSULTATION = "MEX_REFERRAL_CONSULTATION";
		public static final String MEX_PUBLIC_SPACE_LEASE = "MEX_PUBLIC_SPACE_LEASE";
		public static final String MEX_EASEMENT = "MEX_EASEMENT";
		public static final String MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE = "MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE";
		public static final String MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS = "MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS";
		public static final String MEX_TRAINING_SEMINAR = "MEX_TRAINING_SEMINAR";
		public static final String MEX_LAND_RESERVATION = "MEX_LAND_RESERVATION";
		public static final String PARATRANSIT = "PARATRANSIT";
		public static final String PARATRANSIT_RENEWAL = "PARATRANSIT_RENEWAL";
		public static final String PARATRANSIT_CHANGE = "PARATRANSIT_CHANGE";
		public static final String PARATRANSIT_NATIONAL = "PARATRANSIT_NATIONAL";
		public static final String PARATRANSIT_NATIONAL_RENEWAL = "PARATRANSIT_NATIONAL_RENEWAL";
		public static final String PARATRANSIT_RIAK = "PARATRANSIT_RIAK";
		public static final String PARATRANSIT_BUS_CARD = "PARATRANSIT_BUS_CARD";
		public static final String PARATRANSIT_NOTIFICATION = "PARATRANSIT_NOTIFICATION";
		public static final String PARATRANSIT_NOTIFICATION_CHANGE = "PARATRANSIT_NOTIFICATION_CHANGE";
		public static final String PARATRANSIT_NOTIFICATION_RENEWAL = "PARATRANSIT_NOTIFICATION_RENEWAL";
		public static final String PARATRANSIT_NOTIFICATION_NATIONAL = "PARATRANSIT_NOTIFICATION_NATIONAL";
		public static final String PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL = "PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL";
		public static final String PARATRANSIT_NOTIFICATION_RIAK = "PARATRANSIT_NOTIFICATION_RIAK";
		public static final String PARATRANSIT_NOTIFICATION_BUS_CARD = "PARATRANSIT_NOTIFICATION_BUS_CARD";

		private Value() {}

	}
}
