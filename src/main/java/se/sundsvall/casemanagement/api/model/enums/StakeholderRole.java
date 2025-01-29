package se.sundsvall.casemanagement.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "Stakeholder roles")
@Getter
public enum StakeholderRole {

	// ByggR
	// Kontrollansvarig
	CONTROL_OFFICIAL("KOA"),
	// Fastighetsägare
	PROPERTY_OWNER("FAG"),
	// Betalningsansvarig
	PAYMENT_PERSON("BETA"),

	// Ecos2
	INVOICE_RECIPIENT("Fakturamottagare"),
	OPERATOR("Verksamhetsutövare"),
	INSTALLER("Installatör"),

	// General
	// Sökande
	APPLICANT("SOK"),
	// Kontaktperson
	CONTACT_PERSON("KPER"),

	// Parking permit
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
	CHAIRMAN("Ordförande"),
	LAND_RIGHT_OWNER("Tomträttshavare"),
	DELEGATE("Ombud"),
	USUFRUCTUARY("Nyttjanderättshavare");

	private final String text;

	StakeholderRole(final String text) {
		this.text = text;
	}

}
