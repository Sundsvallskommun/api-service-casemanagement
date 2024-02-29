package se.sundsvall.casemanagement.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Type of facility."

        + "<br><br>Compatible with CaseType=NYBYGGNAD_ANSOKAN_OM_BYGGLOV:"
        + "<li>ONE_FAMILY_HOUSE(enbostadshus)</li>"
        + "<li>APARTMENT_BLOCK(flerbostadshus)</li>"
        + "<li>WEEKEND_COTTAGE(fritidshus)</li>"
        + "<li>OFFICE_BUILDING(kontorsbyggnad)</li>"
        + "<li>INDUSTRIAL_BUILDING(industribyggnad)</li>"
        + "<li>GARAGE(garage)</li>"
        + "<li>CARPORT(carport)</li>"
        + "<li>STOREHOUSE(förråd)</li>"
        + "<li>GREENHOUSE(växthus)</li>"
        + "<li>GUEST_HOUSE(gäststuga)</li>"
        + "<li>WAREHOUSE(lagerbyggnad)</li>"
        + "<li>WORKSHOP_BUILDING(Verkstadsbyggnad)</li>"
        + "<li>RESTAURANT(Restaurang)</li>"
        + "<li>SCHOOL(Skola)</li>"
        + "<li>PRESCHOOL(Förskola)</li>"
        + "<li>PARKING(Parkering, Cykelparkering)</li>"
        + "<li>DEPOT(Upplag)</li>"
        + "<li>MARINA(Småbåtshamn)</li>"
        + "<li>WALL(Mur)</li>"
        + "<li>PALING(Plank)</li>"
        + "<li>RECYCLING_STATION(Återvinningsstation)</li>"
        + "<li>OTHER(övrigt)</li>"

        + "<br><br>Compatible with CaseType=ANMALAN_ATTEFALL:"
        + "<li>FURNISHING_OF_ADDITIONAL_DWELLING(inredning av ytterligare bostad)</li>"
        + "<li>ANCILLARY_BUILDING(komplementbyggnad)</li>"
        + "<li>ANCILLARY_HOUSING_BUILDING(komplementbostadsbyggnad)</li>"
        + "<li>DORMER(takkupa)</li>"
        + "<li>EXTENSION(tillbyggnad)</li>"

        + "<br><br>Compatible with CaseType=ENVIRONMENTAL:"
        + "<li>FOOD(Livsmedelsanläggning)</li>"
        + "<li>HEAT_PUMP(Värmepumpsanläggning)</li>")
public enum FacilityType {

    // ByggR - Ärendeklasser (Used for CaseType=NYBYGGNAD_ANSOKAN_OM_BYGGLOV)

    ONE_FAMILY_HOUSE("EB", "enbostadshus"),
    APARTMENT_BLOCK("FB", "flerbostadshus"),
    WEEKEND_COTTAGE("FRI", "fritidshus"),
    OFFICE_BUILDING("KB", "kontorsbyggnad"),
    INDUSTRIAL_BUILDING("IND", "industribyggnad"),
    GARAGE("GA", "garage"),
    CARPORT("CP", "carport"),
    STOREHOUSE("FÖRR", "förråd"),
    GREENHOUSE("VX", "växthus"),
    GUEST_HOUSE("GÄST", "gäststuga"),
    WAREHOUSE("LB", "lagerbyggnad"),
    WORKSHOP_BUILDING("VERK", "verkstadsbyggnad"),
    RESTAURANT("REST", "restaurang"),
    SCHOOL("SKOL", "skola"),
    PRESCHOOL("FÖRS", "förskola"),
    BUSINESS_PREMISES("VERL", "verksamhetslokal"),
    TRANSFORMER_STATION("TRA", "transformatorstation"),
    COMPLEMENT_BUILDING("KOMP", "komplementbyggnad"),
    DEPARTMENT_STORE("AFF", "affärshus"),
    BUILDING("BYGG", "byggnad"),
    BRIDGE_PIER("BRY", "brygga/pir"),
    SIGN("SKY", "skylt"),
    // Parkering & Cykelparkering
    PARKING("PAR", "parkering"),
    DEPOT("UPP", "upplag"),
    MARINA("SMÅBH", "småbåtshamn"),
    WALL("MUR", "mur"),
    PALING("PL", "plank"),
    RECYCLING_STATION("ÅTER", "återvinningsstation"),
    OTHER("ÖVRI", "övrigt"),

    // ByggR - Ärendeslag (Used for CaseType=ANMALAN_ATTEFALL)
    FURNISHING_OF_ADDITIONAL_DWELLING("INRED2", "inredning av ytterligare bostad"),
    ANCILLARY_BUILDING("NYKB", "för nybyggnad av komplementbyggnad"),
    ANCILLARY_HOUSING_BUILDING("NYKBH", "för nybyggnad av komplementbostadshus"),
    DORMER("TILLBTK", "för tillbyggnad med takkupa/takkupor"),
    EXTENSION("TILLBHUS", "för tillbyggnad av en- eller tvåbostadshus"),
    // ByggR - Ärendeslag (Used for CaseType=ANMALAN_ELDSTAD)
    FIREPLACE("A", "installation av eldstad"),
    FIREPLACE_SMOKECHANNEL("C", "installation av eldstad och rökkanal"),
    // ByggR - Ärendeslag (Used for CaseType=ANDRING_ANSOKAN_OM_BYGGLOV)
    RECONSTRUCTION("D", "ombyggnad av"),
    FACADE_CHANGE("F", "fasadändring av"),
    GLAZING_BALCONY("H", "inglasning av balkong"),
    USAGE_CHANGE("ÄNDR", "ändrad användning");


    private final String value;
    private final String description;

    FacilityType(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
