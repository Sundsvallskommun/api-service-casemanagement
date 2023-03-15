package se.sundsvall.casemanagement.service.util;

public final class Constants {
    
    private Constants() {

    }

    ////////////////// ByggR
    public static final String BYGGR_ARENDEMENING_NYBYGGNAD_FORHANDSBESKED = "Förhandsbesked för nybyggnad av";
    public static final String BYGGR_ARENDEMENING_BYGGLOV_FOR_NYBYGGNAD_AV = "Bygglov för nybyggnad av";
    public static final String BYGGR_ARENDEMENING_BYGGLOV_FOR_TILLBYGGNAD = "Bygglov för tillbyggnad av";
    public static final String BYGGR_ARENDEMENING_BYGGLOV_ANDRING_ANSOKAN_OM_ = "Bygglov för";
    public static final String BYGGR_ARENDEMENING_BYGGLOV_FOR_UPPSSATTANDE = "Bygglov för uppsättande av ";
    
    
    public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_NYBYGGNAD= "Strandskyddsdispens för nybyggnad av";
    public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANLAGGANDE= "Strandskyddsdispens för anläggande av";
    public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANORDNANDE= "Strandskyddsdispens för anordnare av";
    public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANDRAD_ANVANDNING= "Strandskyddsdispens för ändrad användning av";
    
    
    public static final String BYGGR_HANDELSE_ANTECKNING = "Inkomna kompletteringar via e-tjänst.";
    public static final String BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING = "Manuell hantering";
    public static final String BYGGR_HANDELSE_RUBRIK_KOMPLETTERING_TILL_ADMIN = "Komplettering till Admin";
    public static final String BYGGR_HANDELSE_RUBRIK_BYGGLOV = "Bygglov";
    public static final String BYGGR_HANDELSE_RUBRIK_FORHANDSBESKED = "Förhandsbesked";
    public static final String BYGGR_HANDELSE_RUBRIK_STRANDSKYDD ="Strandskyddsdispens";
    public static final String BYGGR_HANDELSESLAG_STRANDSKYDD ="Strand";
    public static final String BYGGR_HANDELSE_RUBRIK_ANMALAN_ATTEFALL = "Anmälan Attefall";
    
    
    public static final String BYGGR_HANDELSE_RUBRIK_ELDSTAD = "Eldstad";
    public static final String BYGGR_HANDELSESLAG_ELDSTAD = "ELD1";
    public static final String BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL="Eldstad/Rökkanal";
    public static final String BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL = "ELD";
    
    public static final String BYGGR_HANDELSE_RIKTNING_IN = "In";
    public static final String BYGGR_HANDELSETYP_STATUS = "STATUS";
    public static final String BYGGR_HANDELSETYP_ANSOKAN = "ANSÖKAN";
    public static final String BYGGR_HANDELSETYP_ANMALAN = "ANM";
    public static final String BYGGR_HANDELSETYP_BESLUT = "BESLUT";
    public static final String BYGGR_HANDELSETYP_HANDLING = "HANDLING";
    public static final String BYGGR_HANDELSETYP_ATOMHANDELSE = "Atom";
    public static final String BYGGR_HANDELSETYP_REMISS = "REMISS";
    public static final String BYGGR_HANDELSETYP_UNDERRATTELSE = "UNDER";
    public static final String BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE = "KOMP";
    public static final String BYGGR_HANDELSETYP_KOMPLETTERINGSFORELAGGANDE_PAMINNELSE = "KOMP1";

    public static final String BYGGR_HANDELSESLAG_BYGGLOV = "Bygglov";
    public static final String BYGGR_HANDELSESLAG_FORHANDSBESKED = "Förhandsbesked";
    public static final String BYGGR_HANDELSESLAG_SLUTBESKED = "SLU";
    public static final String BYGGR_HANDELSESLAG_AVSKRIVNING = "UAB";
    public static final String BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS = "MANHANT";
    public static final String BYGGR_HANDELSESLAG_KOMPLETTERANDE_HANDLINGAR = "KOMPL";
    public static final String BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR = "KOMPBYGG";
    public static final String BYGGR_HANDELSESLAG_KOMPLETTERANDE_TEKNISKA_HANDLINGAR = "KOMPTEK";
    public static final String BYGGR_HANDELSESLAG_REVIDERADE_HANDLINGAR = "KOMPREV";
    public static final String BYGGR_HANDELSESLAG_ATOM_KVITTENS = "Kv";
    public static final String BYGGR_HANDELSESLAG_UTSKICK_AV_REMISS = "UTSKICK";
    public static final String BYGGR_HANDELSESLAG_MED_KRAV_PA_SVAR = "Med";
    public static final String BYGGR_HANDELSESLAG_UTAN_KRAV_PA_SVAR = "Utan";
    public static final String BYGGR_HANDELSESLAG_KOMPLETTERING_TILL_ADMIN = "KOMPADM";
    
    public static final String BYGGR_HANDELSESLAG_ANMALAN_ATTEFALL = "ANMATT";

    public static final String BYGGR_HANDELSEUTFALL_ATOM_KVITTENS_HL_BYTE = "Kv2";

    public static final String BYGGR_STATUS_AVSLUTAT = "Avslutat";

    public static final String BYGGR_HANDLING_STATUS_INKOMMEN = "Inkommen";
    public static final String BYGGR_SYSTEM_HANDLAGGARE_SIGN = "SYSTEM";
    public static final String BYGGR_NAMNDKOD_STADSBYGGNADSNAMNDEN = "SBN";
    public static final String BYGGR_KOMMUNKOD_SUNDSVALL_KOMMUN = "2281";
    public static final String BYGGR_ENHETKOD_STADSBYGGNADSKONTORET = "SBK";
    public static final String BYGGR_ARENDEGRUPP_LOV_ANMALNINGSARENDE = "LOV";
    public static final String BYGGR_ARENDEGRUPP_STRANDSKYDD = "STRA";
    public static final String BYGGR_ARENDETYP_BYGGLOV_FOR = "BL";
    public static final String BYGGR_ARENDETYP_FORHANDSBESKED = "FÖRF";
    public static final String BYGGR_ARENDETYP_ANMALAN_ATTEFALL = "ATTANM";
    public static final String BYGGR_ARENDETYP_STRANDSKYDD="DI";
    
    public static final String BYGGR_HANDELSE_ANTECKNING_INTRESSENT_KUNDE_INTE_REGISTRERAS = "- Det finns flera intressenter med samma personnummer i den inkomna ansökan. Detta går inte att registrera maskinellt.";
    public static final String BYGGR_HANDELSE_ANTECKNING_KONTROLLANSVARIG = "- Det finns uppgifter om kontrollansvarig i den inkomna ansökan. Detta går inte att registrera maskinellt.";
    public static final String BYGGR_HANDELSE_ANTECKNING_FASTIGHETSAGARE = "- Fastighetsägare kunde inte registreras maskinellt.";
    public static final String BYGGR_HANDELSE_ANTECKNING_DU_MASTE_REGISTRERA_DETTA_MANUELLT = "Du måste registrera ovanstående punkter manuellt. Det inkomna ärendet hittar du i handlingen \"Ansökan om bygglov\".";
    public static final String BYGGR_KOMTYP_MOBIL = "MOB";
    public static final String BYGGR_KOMTYP_HEMTELEFON = "HEM";
    public static final String BYGGR_KOMTYP_EPOST = "Epost";

    ////////////////// Ecos
    public static final String ECOS_DIARY_PLAN_LIVSMEDEL = "73B90981-D7AE-49E3-8AB7-3AED778ABDB4";
    public static final String ECOS_DIARY_PLAN_AVLOPP = "91470D60-FCDE-418D-A2B9-601FC1850B63";
    public static final String ECOS_DIARY_PLAN_HALSOSKYDD = "86100879-6451-4310-AAB2-9C1F9F663F69";

    public static final String ECOS_OCCURENCE_TYPE_ID_ANMALAN = "34BA125B-E9EE-4389-AEAE-9F66288C1B63";
    public static final String ECOS_HANDLING_OFFICER_GROUP_ID_EXPEDITIONEN = "58E6A5CE-C6EE-42B4-A96A-BD25D693420E";

    // Registrering av livsmedelsanläggning
    public static final String ECOS_PROCESS_TYPE_ID_REGISTRERING_AV_LIVSMEDEL = "A764A86B-7327-445B-98C5-C26543D6F705";
    // Anmälan av installation av värmepump
    public static final String ECOS_PROCESS_TYPE_ID_ANMALAN_INSTALLATION_VARMEPUMP = "38C76611-DFE0-4358-864A-31C320712F69";
    // Ansökan om tillstånd till värmepump
    public static final String ECOS_PROCESS_TYPE_ID_ANSOKAN_TILLSTAND_VARMEPUMP = "BDFE8FBB-18D5-45FC-A9E7-DE43E42F6218";
    // Anmälan av installation av enskilt avlopp utan vattentoalett
    public static final String ECOS_PROCESS_TYPE_ID_ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC = "294F547E-C1C9-445E-87F9-8829D0FB1ED6";
    // Anmälan om ändring av avloppsanläggning
    public static final String ECOS_PROCESS_TYPE_ID_ANMALAN_ANDRING_AVLOPPSANLAGGNING = "9511B1D1-4BAA-4FC1-92FD-84622AD8A4C8";
    // Anmälan om ändring av avloppsanordning
    public static final String ECOS_PROCESS_TYPE_ID_ANMALAN_ANDRING_AVLOPPSANORDNING = "11428429-E292-44B5-B03A-A4FE6CEBAAD7";
    // Ansökan om tillstånd för enskilt avlopp
    public static final String ECOS_PROCESS_TYPE_ID_ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP = "50B6FA5B-23E2-4ABA-B393-0B3ADEFC6C9F";
    // Anmälan av hälsoskyddsverksamhet
    public static final String ECOS_PROCESS_TYPE_ID_ANMALAN_HALSOSKYDDSVERKSAMHET = "AF6D94EC-94FB-4C0A-AF39-CC5E4C732D4B";
    // Uppdatering av riskklass
    public static final String ECOS_PROCESS_TYPE_ID_UPPDATERING_RISKKLASS = "E8E389D4-FE45-4195-A790-C58AE4DC96BF";

    public static final String ECOS_DOCUMENT_STATUS_INKOMMEN = "F6F4B956-36D7-4CBF-8BCA-713219BFD5F2";
    public static final String ECOS_FACILITY_STATUS_ID_ANMALD_ANSOKT = "88E11CAA-DF35-4C5E-94A8-3C7B0369D8F2";
    public static final String ECOS_ROLE_ID_FAKTURAMOTTAGARE = "480E2731-1F2F-4F35-8A37-FDDE957E9CD0";
    public static final String ECOS_ROLE_ID_VERKSAMHETSUTOVARE = "45A48C9F-9BAC-45DB-8D47-CDA790E17383";
    public static final String ECOS_ROLE_ID_KONTAKTPERSON = "EC77F83B-4C0F-412F-B145-8E4C18F1ACA0";
    public static final String ECOS_ROLE_ID_SOKANDE = "5BC75375-55FB-4676-968B-64C2C7BB74A8";
    public static final String ECOS_ROLE_ID_INSTALLATOR = "C85DD202-0BA8-4CBC-9FF1-8D641BE35F42";
    public static final String ECOS_ADDRESS_TYPE_ID_FAKTURAADRESS = "EEF91381-7025-4FE7-B5FA-92FB2B77976B";
    public static final String ECOS_ADDRESS_TYPE_ID_POSTADRESS = "B1D7655C-D2D9-4D69-96A2-1267960C6102";
    public static final String ECOS_ADDRESS_TYPE_ID_BESOKSADRESS = "00CCE3B3-52C0-4B7E-AF68-A8204F595A48";
    public static final String ECOS_CONTACT_DETAIL_TYPE_ID_OVRIGT = "BF1F20E2-7687-4BE0-86CF-06A7F6B31303";
    public static final String ECOS_CONTACT_DETAIL_TYPE_ID_EPOST = "D34442AC-D8F7-419A-BE2B-2794674DE58E";
    public static final String ECOS_CONTACT_DETAIL_TYPE_ID_MOBIL = "9C26C006-76A9-4331-8B00-67984AC40885";
    public static final String ECOS_CONTACT_DETAIL_TYPE_ID_TELEFON = "2BB38776-54E4-405E-9E84-BD841C6BB2C3";
    public static final String ECOS_CONTACT_DETAIL_TYPE_ID_HUVUDNUMMER = "6DCBE753-81C5-4FA5-B0D4-0216411CB119";
    public static final String ECOS_OCCURRENCE_TYPE_ID_INFO_FRAN_ETJANST = "BF28124A-7C51-452C-8F72-16412364F8C2";
    public static final String ECOS_OCCURRENCE_TYPE_ID_KOMPLETTERING = "AE3F6E26-6B4F-4231-8BCA-C81A98547727";
    public static final String ECOS_OCCURENCE_TEXT_MOBIL_ANLAGGNING = """
            Anläggningen kunde inte registreras automatiskt då anläggningen var av typen "mobil".
            Denna anläggning och tillhörande part/-er måste registreras manuellt.
            Informationen finns i handlingen "Anmälan livsmedelsanläggning".""";


    ////////////////// Error messages
    public static final String ERR_MSG_ONLY_ONE_MAIN_FACILITY = "Number of mainFacilities must be <= 1";
    public static final String ERR_MSG_CASEMAPPINGS_NOT_FOUND = "CaseMappings not found";
    public static final String ERR_MSG_CASES_NOT_FOUND = "Case not found";
    public static final String ERR_MSG_STATUS_NOT_FOUND = "Status not found";
    public static final String ERR_MSG_PERSON_INVOICE_ADDRESS = "Stakeholders of type PERSON should not have an address with the addressCategory INVOICE_ADDRESS";
    public static final String ERR_MSG_WRONG_ROLE_ENV_CASE = "Stakeholder contains a role that may not be used with Ecos-cases. Check the OpenAPI-specification.";
    public static final String ERR_MSG_WRONG_ROLE_PLANNING_CASE = "Stakeholder contains a role that may not be used with Byggr-cases. Check the OpenAPI-specification.";
    public static final String ERR_START_MUST_BE_BEFORE_END = "startDate must be before endDate";
    public static final String ORGNR_PATTERN_MESSAGE = "organizationNumber must consist of 10 or 12 digits. 10 digit orgnr must follow this format: \"XXXXXX-XXXX\". 12 digit orgnr must follow this format: \"(18|19|20)XXXXXX-XXXX\".";
    public static final String ORGNR_PATTERN_REGEX = "^((18|19|20|21)\\d{6}|\\d{6})-(\\d{4})$";
    public static final String REQUEST_BODY_MUST_NOT_BE_NULL = "Request body must not be null";

    public static String ERR_MSG_PERSONAL_NUMBER_NOT_FOUND_WITH_PERSON_ID(String personId) {
        return "No personalNumber was found in CitizenMapping with personId: " + personId;
    }

    public static String ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND(String propertyDesignation) {
        return "The specified propertyDesignation(" + propertyDesignation + ") could not be found";
    }

    ////////////////// Other
    public static final String SERVICE_NAME = "serviceName";
    public static final String LANTMATERIET_REFERENS_STATUS_GALLANDE = "Gällande";
    public static final String FB_JURIDISK_FORM_PRIVATPERSON = "00";
    public static final String SWEDEN = "Sverige";
    public static final String POST_CASES_REQUEST_BODY_EXAMPLE = """
            {
                "externalCaseId": "e19981ad-34b2-4e14-88f5-133f61ca85aa",
                "caseType": "NYBYGGNAD_ANSOKAN_OM_BYGGLOV",
                "facilities": [
                    {
                        "facilityType": "STOREHOUSE",
                        "address": {
                            "addressCategories": [
                                "VISITING_ADDRESS"
                            ],
                            "propertyDesignation": "SUNDSVALL BALDER 2"
                        }
                    }
                ],
                "stakeholders": [
                    {
                        "type": "ORGANIZATION",
                        "roles": [
                            "APPLICANT",
                            "PAYMENT_PERSON"
                        ],
                        "organizationName": "Testorganisationen",
                        "organizationNumber": "123456-1234"
                    },
                    {
                        "type": "PERSON",
                        "roles": [
                            "CONTACT_PERSON"
                        ],
                        "firstName": "Test",
                        "lastName": "Testsson",
                        "personId": "e19981ad-34b2-4e14-88f5-133f61ca85aa"
                    }
                ],
                "attachments": [
                    {
                        "category": "ANS",
                        "name": "Some_name_2022-03-07",
                        "extension": ".pdf",
                        "file": "dGVzdA=="
                    }
                ]
            }""";

}

