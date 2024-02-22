package se.sundsvall.casemanagement.api.model.enums;

import lombok.Getter;

public enum AttachmentCategory {

	///////////////////////////////////
	// ByggR
	///////////////////////////////////

	// archiveClassification "A"
	ARIT("A-ritningar", "A"),
	FAS("Fasad", "A"),
	FS2("Fasad- och sektionsritning", "A"),
	FAP("Fasad Plan", "A"),
	FAPL("Fasad Plan Sektion", "A"),
	FPSS("Fasad Plan Sektion Situationsplan", "A"),
	FS("Fasad sektion", "A"),
	FASSIT("Fasad Situation", "A"),
	FAS2("Fasadritning", "A"),
	FASSIT2("Fasadritning + situationsplan", "A"),
	FOTOMON("Fotomontage", "A"),
	FÄRG("Färgsättningsförslag", "A"),
	MAST("Mastritning", "A"),
	MUR("Murritning", "A"),
	MÅTT("Måttritning", "A"),
	PERSPEKTIV("Perspektivsritning", "A"),
	PLA("Plan", "A"),
	PLFA("Plan Fasad", "A"),
	PLFASE("Plan Fasad Sektion", "A"),
	PLFASESI("Plan Fasad Sektion Situationsplan", "A"),
	PLFASI("Plan Fasad Situationsplan", "A"),
	PLFA2("Plan- och fasadritning", "A"),
	PFSI2("Plan- och fasadritning + situationsplan", "A"),
	PLSE2("Plan- och sektionsritning", "A"),
	PSS2("Plan- och sektionsritning + situationsp.", "A"),
	PLSE("Plan Sektion", "A"),
	PSS("Plan Sektion Situation", "A"),
	PLASIT("Plan Situation", "A"),
	PFS2("Plan, fasad- och sektionsritning", "A"),
	PFSS2("Plan, fasad, sektion, situation", "A"),
	TEVS("Planbeskrivning", "A"),
	UPLA("Planer", "A"),
	PLAN("Planer", "A"),
	PLANK("Plankritning", "A"),
	PLA2("Planritning", "A"),
	PSI2("Planritning + situationsplan", "A"),
	REL("Relationsritning", "A"),
	REVRIT("Reviderade ritning", "A"),
	RITNING("Ritning", "A"),
	TJ("Ritningar", "A"),
	RIT("Ritningar", "A"),
	SEK("Sektion", "A"),
	SEKSIT("Sektion Situation", "A"),
	SEKT("Sektioner", "A"),
	SEK2("Sektionsritning", "A"),
	SESI2("Sektionsritning + situationsplan", "A"),
	SKYL("Skyltritning", "A"),
	UPPM("Uppmätningsritning", "A"),
	ANV("Utställningshandling", "A"),

	// archiveClassification "D"
	ANM("Anmälan", "D"),
	ANMÄ("Anmälan av kontrollansvarig", "D"),
	ANS("Ansökan om bygglov", "D"),
	ANSFÖ("Ansökan om förhandsbesked", "D"),
	ANSM("Ansökan om marklov", "D"),
	ANSR("Ansökan om rivningslov", "D"),
	ANSS("Ansökan om strandskyddsdispens", "D"),
	BEGLST("Begäran från länsstyrelsen", "D"),
	BERBSA("Beräkning byggsanktionsavgift", "D"),
	BLST("Beslut från Länsstyrelsen", "D"),
	OMPLÄ("Beslut omprövning Länsstyrelsen", "D"),
	BULL("Bullerutredning", "D"),
	DEB("Debiteringsblad", "D"),
	DEL("Delgivning", "D"),
	DELK("Delgivningskvitto", "D"),
	DELSLU("Delslutbesked", "D"),
	DELSTA("Delstartbesked", "D"),
	DOM("Dom", "D"),
	ENER("Energibalansberäkning", "D"),
	ENEDEK("Energideklaration", "D"),
	FAST("Fastställd kontrollplan", "D"),
	FÖLJREVRIT("Följebrev reviderad ritning", "D"),
	FÖRG2("Förhandsgranskningsblad", "D"),
	GODFÄ("Godkännande från fastighetsägare", "D"),
	GRAM("Grannemedgivande", "D"),
	INFOSS("Info inför start- och slutbesked", "D"),
	INTSLUT("Interimistiskt slutbesked", "D"),
	KM("Kontrollmeddelande", "D"),
	MOTBKR("Mottagningsbekräftelse", "D"),
	OVK("OVK-protokoll", "D"),
	PM("PM", "D"),
	PMINN("Påminnelse", "D"),
	PROARB("Protokoll arbetsplatsbesök", "D"),
	PROTAU("Protokoll AU", "D"),
	PROTKS("Protokoll KS", "D"),
	PROTPLU("Protokoll PLU", "D"),
	PROTSBN("Protokoll SBN", "D"),
	PROSS("Protokoll slutsamråd", "D"),
	PROTS("Protokoll tekniskt samråd", "D"),
	REMISS("Remiss", "D"),
	REMS("Remissvar", "D"),
	RÄTT("Rättidsprövning", "D"),
	SKP("Signerad kontrollplan", "D"),
	SBES("Slutbesked", "D"),
	STAB("Startbesked", "D"),
	SVAR("Svar", "D"),
	SVAR2år("Svar 2-årsbrev", "D"),
	TJÄ("Tjänsteskrivelse", "D"),
	UNDER("Underrättelsesvar", "D"),
	ÄRB("Ärendeblad", "D"),
	ÖVER("Överklagandeskrivelse", "D"),
	ADRESS("Adressblad", "D"),
	ANSUPA("Anmälan utan personnummer", "D"),
	ANNO("Annons", "D"),
	ANSF("Ansökan om förhandsbesked", "D"),
	ANSSL("Ansökan om slutbesked", "D"),
	ANSUP("Ansökan utan personnummer", "D"),
	ANKVU("Antikvariskt utlåtande", "D"),
	ARBI("Arbetstagarintyg", "D"),
	BEHA("Atomutskick handläggare tilldelad", "D"),
	AVPLAN("Avvecklingsplan", "D"),
	BANK("Bankgaranti", "D"),
	BEGSTART("Begäran om startbesked", "D"),
	BEK("Bekräftelse", "D"),
	BEKMOTANS("Bekräftelse mottagen ansökan", "D"),
	BEMÖ("Bemötande", "D"),
	BESKA("Besöksrapporter KA", "D"),
	BESLUT("Beslut", "D"),
	BIL("Bilaga", "D"),
	BRS("Brandskiss", "D"),
	BRAB("Brandskyddsbeskrivning", "D"),
	BRAD("Brandskyddsdokumentation", "D"),
	BROS("Broschyr", "D"),
	DPH("Detaljplankarta/detaljplanhandling", "D"),
	DETALJ("Detaljritning", "D"),
	DHBHUR("Du har fått bygglov/ Hur man överklagar", "D"),
	ELD("Elda rätt", "D"),
	EPOS("Epost", "D"),
	EXRIT("Exempelritning", "D"),
	FAKTU("Fakturaunderlag", "D"),
	FAKTUS("Fakturaunderlag sanktionsavgift", "D"),
	FOTO("Foto", "D"),
	FUM("Fullmakt", "D"),
	FSF("Färdigställandeförsäkring", "D"),
	FÖLJ("Följebrev", "D"),
	FÖRB("Förhandsbesked", "D"),
	FÖRK("Förslag till kontrollplan", "D"),
	FÖRR("Förslag till rivningsplan", "D"),
	FÖRGARBO("Försäkringsbrev Gar-Bo", "D"),
	URÖR("Genomförandebeskrivning", "D"),
	GRA("Grannhörande", "D"),
	GRAN("Granskningsblad", "D"),
	GBLAD("Granskningsblad", "D"),
	HISSINT("Hissintyg", "D"),
	HUR("Hur man överklagar", "D"),
	ARK("Illustration/ perspektiv", "D"),
	INTFAK("Internfakturaunderlag", "D"),
	INTY("Intyg", "D"),
	KLA("Klassningsplan", "D"),
	KOMP("Kompletteringsföreläggande", "D"),
	KONT("Kontrollansvarig", "D"),
	KPLAN("Kontrollplan PBL", "D"),
	RAPP("Kontrollrapport", "D"),
	KVAL("Kvalitetsansvarig", "D"),
	LUFT("Luftflödesprotokoll", "D"),
	LUTE("Lufttäthetstest", "D"),
	MAIL("Mail", "D"),
	MAPL("Markplaneringsritning", "D"),
	MATINV("Materialinventering", "D"),
	MEDDEL("Meddelanden", "D"),
	MIRP("Miljöinventering/ rivningsplan", "D"),
	MINN("Minnesanteckningar", "D"),
	POIT("PoIT", "D"),
	PRESENTA("Presentation", "D"),
	PRES("Prestandadeklaration", "D"),
	KPV("Programsamrådshandling", "D"),
	PROT("Protokoll", "D"),
	PÅMINNTB("Påminnelse tidsbegränsat lov", "D"),
	RAP("Rapport", "D"),
	REMUA("Remiss utan adress", "D"),
	RUE("Remissvar utan erinran", "D"),
	HBB("Ritningsförteckning", "D"),
	RIVA("Rivningsanmälan", "D"),
	RIVP("Rivningsplan", "D"),
	SAK("Sakkunnigintyg", "D"),
	SAKUT("Sakkunnigutlåtande brand", "D"),
	KPR("Samrådshandling", "D"),
	KP("Samrådsredogörelse del 1", "D"),
	KR("Samrådsredogörelse del 2", "D"),
	SIN("Signerad kontrollplan", "D"),
	SKR("Skrivelse", "D"),
	KA("Skrivelser", "D"),
	SKY("Skyddsrumsförfrågan", "D"),
	SLUT("Slutbevis", "D"),
	SCB("Statistikblankett SCB", "D"),
	STIM("Stimulansbidrag", "D"),
	SÅF("Svar på åtgärdsföreläggande", "D"),
	TEBY("Teknisk beskrivning", "D"),
	TEKN("Teknisk beskrivning brf", "D"),
	TEKRAP("Teknisk rapport", "D"),
	TILL("Tillsynsbesiktning", "D"),
	TILLVR("Tillverkningsritning", "D"),
	SBN("Tjänsteskrivelse till nämnden", "D"),
	SAKNAS("Typen saknades vid konverteringen", "D"),
	UND("Underlag situationsplan", "D"),
	UKP("Underlag till kontrollplan", "D"),
	UKR("Underlag till rivningsplan", "D"),
	UNDUT("Underrättelse", "D"),
	UBGARBO("Uppdragsbekräftelse", "D"),
	UTBEU("Utbetalningsunderlag", "D"),
	UTSK("Utskick", "D"),
	UTSKP("Påminnelseutskick", "D"),
	UTSKS("Svar utskick", "D"),
	BRAU("Utförandekontroll brandskydd", "D"),
	UKA("Utlåtande KA", "D"),
	ÅTG("Åtgärdsföreläggande", "D"),

	// archiveClassification "GU"
	GEO("Geotekniska handling", "GU"),

	// archiveClassification "K"
	GRUNDP("Grundplan", "K"),
	GRUNDR("Grundritning", "K"),
	KOND("Konstruktionsdokument", "K"),
	UKON("Konstruktionshandling", "K"),
	KONR("Konstruktionsritning", "K"),
	STOMR("Stomritningar", "K"),
	TAPL("Takplan", "K"),
	TSR("Takstolsritning", "K"),

	// archiveClassification "S"
	KART("Karta", "S"),
	NYKA("Nybyggnadskarta", "S"),
	SITU("Situationsplan", "S"),
	TOMTPLBE("Tomtplatsbestämning", "S"),

	// archiveClassification "VVS"
	VAH("VA-handling", "VVS"),
	VENT("Ventilationshandling", "VVS"),
	UVEN("Ventilationsritning", "VVS"),
	VS("VS-handling", "VVS"),
	VVSH("VVS-handling", "VVS"),

	// archiveClassification UNKNOWN
	MASE("Marksektionsritning", null),
	///////////////////////////////////
	// ECOS
	///////////////////////////////////

	ANMALAN_AVHJALPANDEATGARD_FORORENING("FACD0F1D-BAFF-4506-95C9-99014EA8D827", null),

	ANMALAN_LIVSMEDELSANLAGGNING("3AD42CEE-C09E-401B-ABE8-0CD5D03FE6B4", null),

	UNDERLAG_RISKKLASSNING("77DBC762-4EDC-46A8-983C-BE03EDC07E13", null),
	ANMALAN_KOMPOSTERING("AC838DD1-61D9-4AB9-9E8B-FD48379EF510", null),

	ANMALAN_ENSKILT_AVLOPP("E9F85119-9E94-47AD-B531-BB91EF75368A", null),
	ANSOKAN_ENSKILT_AVLOPP("296B51FA-C77B-42E7-AFBE-F0A74CAE4FD2", null),
	ANMALAN_ANDRING_AVLOPPSANLAGGNING("3FBEECCA-099D-4E51-8FFA-D023AF79017D", null),
	ANMALAN_ANDRING_AVLOPPSANORDNING("52E2898B-D780-4EB5-B9CA-24842714E6DF", null),

	ANMALAN_VARMEPUMP("ACCC629C-4D26-4466-9DFD-578DB746D119", null),
	ANSOKAN_TILLSTAND_VARMEPUMP_MINDRE_AN_100KW("3F6DBE03-DB41-47AA-A56A-ECD87C8133B1", null),

	ANMALAN_HALSOSKYDDSVERKSAMHET("EA5D5EBE-DCBE-4EAA-A2B9-8662B128BD96", null),

	SITUATIONSPLAN("9288F033-8E1A-48AE-858F-CB7345F81359", null),

	SKRIVELSE("A06E65AD-E4B1-4B84-BCC6-7843CDE6B0A1", null),

	///////////////////////////////////
	// Parking permit
	///////////////////////////////////
	MEDICAL_CONFIRMATION("Läkarintyg", null),
	POLICE_REPORT("Polisanmälan", null),
	PASSPORT_PHOTO("Passfoto", null),
	SIGNATURE("Namnunderskrift", null),
	POWER_OF_ATTORNEY("Fullmakt", null),

	///////////////////////////////////
	// MEX
	///////////////////////////////////
	LEASE_REQUEST("Förfrågan arrende", null),
	RECEIVED_MAP("Karta inkommen", null),
	RECEIVED_CONTRACT("Avtal inkommit", null),
	LAND_PURCHASE_REQUEST("Förfrågan markköp", null),
	INQUIRY_LAND_SALE("Förfrågan markförsäljning", null),
	APPLICATION_SQUARE_PLACE("Ansökan torgplats", null),
	CORPORATE_TAX_CARD("F-skattsedel", null),
	TERMINATION_OF_HUNTING_RIGHTS("Uppsägning jakträtt", null),
	REQUEST_TO_BUY_SMALL_HOUSE_PLOT("Förfrågan köpa småhustomt", null),
	CONTRACT_DRAFT("Avtalsutkast", null);

	@Getter
	private final String description;
	@Getter
	private final String archiveClassification;

	AttachmentCategory(String description, String archiveClassification) {
		this.description = description;
		this.archiveClassification = archiveClassification;
	}

	public static AttachmentCategory valueOfDescription(String description) {
		for (final AttachmentCategory attachmentCategory : values()) {
			if (attachmentCategory.description.equals(description)) {
				return attachmentCategory;
			}
		}
		throw new IllegalArgumentException("Could not find any AttachmentCategory with the description: " + description);
	}
}
