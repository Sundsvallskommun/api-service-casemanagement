package se.sundsvall.casemanagement.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "<h3>General roles:</h3>" +
        "<li>APPLICANT(Sökande)</li>"+
        "<li>CONTACT_PERSON(Kontaktperson)</li>" +

        "<br><br><h3>ByggR-roles:</h3>" +
        "<li>CONTROL_OFFICIAL(Kontrollansvarig)</li>" +
        "<li>PROPERTY_OWNER(Fastighetsägare)</li>" +
        "<li>PAYMENT_PERSON(Betalningsansvarig)</li>" +

        "<br><br><h3>Ecos2-roles:</h3>" +
        "<li>INVOICE_RECIPENT(Fakturamottagare)</li>" +
        "<li>OPERATOR(Verksamhetsutövare)</li>" +
        "<li>INSTALLER(Installatör)</li>")
@Getter
public enum StakeholderRole {

    ////////// ByggR //////////
    // Kontrollansvarig
    CONTROL_OFFICIAL("KOA"),
    // Fastighetsägare
    PROPERTY_OWNER("FAG"),
    // Betalningsansvarig
    PAYMENT_PERSON("BETA"),

    ////////// Ecos2 //////////
    INVOICE_RECIPENT("Fakturamottagare"),
    OPERATOR("Verksamhetsutövare"),
    INSTALLER("Installatör"),

    ////////// General //////////
    // Sökande
    APPLICANT("SOK"),
    // Kontaktperson
    CONTACT_PERSON("KPER"),

    ///////////////////////////////////
    // Parking permit
    ///////////////////////////////////
    ADMINISTRATOR("Handläggare"),
    FELLOW_APPLICANT("Medsökande"),
    DRIVER("Förare"),
    PASSENGER("Passagerare"),
    DOCTOR("Läkare"),

    GRANTOR("Upplåtare"),
    SELLER("Säljare"),
    BUYER("Köpare"),
    LEASEHOLDER("Arrendator"),
    COMPANY_SIGNATORY("Firmatecknare"),
    ASSOCIATION_REPRESENTATIVE("Föreningsrepresentant"),
    CASHIER("Kassör"),
    CHAIRMAN("Ordförande");

    private final String text;

    StakeholderRole(String text) {
        this.text = text;
    }

}
