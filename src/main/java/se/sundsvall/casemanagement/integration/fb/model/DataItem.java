package se.sundsvall.casemanagement.integration.fb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Data
public class DataItem{
	private Integer fnr;
	private List<GruppItem> grupp;
	private String kommun;
	private String beteckning;
	private String trakt;
	private String beteckningsnummer;
	private String uuid;
	private String gallandeOrganisationsnamn;
	private String identitetsnummer;
	private String gallandeFornamn;
	private String gallandeEfternamn;
	// Kod för organisationens eller personens juridiska form. För privatpersoner är koden alltid 00.
	private String juridiskForm;

	// Typ av adress. LFADR = folkbokföringsadress för person eller adress för organisation. LFSÄR = Särskild adress. LFUTL = utlandsadress
	private String adresstyp;
	// c/o-adress. Finns inte för utländska adresser
	private String coAdress;
	// Del 1 av utdelningsadress. Svenska gårdsadresser finns i detta fält
	private String utdelningsadress1;
	// Del 2 av utdelningsadress. Normala svenska adresser finns i detta fält
	private String utdelningsadress2;
	// Del 3 av utdelningsadress. Förekommer endast för utlandsadresser
	private String utdelningsadress3;
	// Del 4 av utdelningsadress. Förekommer endast för utlandsadresser
	private String utdelningsadress4;
	// Svenskt postnummer
	private String postnummer;
	// Svensk postort
	private String postort;
	// Land för utlandsadress
	private String land;
}