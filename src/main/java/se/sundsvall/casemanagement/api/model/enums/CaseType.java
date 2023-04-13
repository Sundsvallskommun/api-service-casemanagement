package se.sundsvall.casemanagement.api.model.enums;


import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeMenining.*;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeMenining.BYGGR_ARENDEMENING_BYGGLOV_FOR_NYBYGGNAD_AV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeMenining.BYGGR_ARENDEMENING_BYGGLOV_FOR_TILLBYGGNAD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeMenining.BYGGR_ARENDEMENING_BYGGLOV_FOR_UPPSSATTANDE;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeMenining.BYGGR_ARENDEMENING_NYBYGGNAD_FORHANDSBESKED;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_ANDRAD_ANVADNING_STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_ANLAGGANDE_STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_ANORDNARE_STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_NYBYGGNAD_AV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_NYBYGGNAD_STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_TILLBYGGNAD_AV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.ArendeslagConstants.BYGGR_ARENDESLAG_UPPSATTANDE_SKYLT;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.GruppConstants.LOV_ANMALAN;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.GruppConstants.STRAND_SKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseRubrik.*;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseRubrik.RUBRIK_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseRubrik.RUBRIK_STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseSlag.HANDELSESLAG_ANMALAN_ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseSlag.HANDELSESLAG_BYGGLOV;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseSlag.HANDELSESLAG_FORHANDSBESKED;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseSlag.HANDELSESLAG_STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.HandelseTyp.*;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.TypConstants.ANMALAN;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.TypConstants.ATTEFALL;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.TypConstants.BYGGLOV_FOR;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.TypConstants.FORHANDSBESKED;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.TypConstants.STRANDSKYDD;
import static se.sundsvall.casemanagement.api.model.enums.CaseType.Value.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import net.minidev.json.annotate.JsonIgnore;

@Schema(example = NYBYGGNAD_ANSOKAN_OM_BYGGLOV)
public enum CaseType {
    
    // ===================  Byggr  ===================
    
    // BYGGR BYGGLOV
    NYBYGGNAD_ANSOKAN_OM_BYGGLOV(Value.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, BYGGR_ARENDESLAG_NYBYGGNAD_AV, LOV_ANMALAN,BYGGLOV_FOR, HANDELSETYP_ANSOKAN,RUBRIK_BYGGLOV,HANDELSESLAG_BYGGLOV,BYGGR_ARENDEMENING_BYGGLOV_FOR_NYBYGGNAD_AV),
    TILLBYGGNAD_ANSOKAN_OM_BYGGLOV(Value.TILLBYGGNAD_ANSOKAN_OM_BYGGLOV, BYGGR_ARENDESLAG_TILLBYGGNAD_AV,LOV_ANMALAN, BYGGLOV_FOR, HANDELSETYP_ANSOKAN,RUBRIK_BYGGLOV,HANDELSESLAG_BYGGLOV,BYGGR_ARENDEMENING_BYGGLOV_FOR_TILLBYGGNAD),
    UPPSATTANDE_SKYLT(Value.UPPSATTANDE_SKYLT, BYGGR_ARENDESLAG_UPPSATTANDE_SKYLT,LOV_ANMALAN, BYGGLOV_FOR, HANDELSETYP_ANSOKAN,RUBRIK_BYGGLOV,HANDELSESLAG_BYGGLOV,BYGGR_ARENDEMENING_BYGGLOV_FOR_UPPSSATTANDE),
    
    ANDRING_ANSOKAN_OM_BYGGLOV(Value.ANDRING_ANSOKAN_OM_BYGGLOV, null, LOV_ANMALAN,BYGGLOV_FOR, HANDELSETYP_ANSOKAN,RUBRIK_BYGGLOV,HANDELSESLAG_BYGGLOV, BYGGR_ARENDEMENING_BYGGLOV_ANDRING_ANSOKAN_OM),
    
    NYBYGGNAD_FORHANDSBESKED(Value.NYBYGGNAD_FORHANDSBESKED, BYGGR_ARENDESLAG_NYBYGGNAD_AV, LOV_ANMALAN, FORHANDSBESKED, HANDELSETYP_ANSOKAN, RUBRIK_FORHANDSBESKED, HANDELSESLAG_FORHANDSBESKED,BYGGR_ARENDEMENING_NYBYGGNAD_FORHANDSBESKED),
    
    // BYGGR STRANDSKYDD
    STRANDSKYDD_NYBYGGNAD(Value.STRANDSKYDD_NYBYGGNAD, BYGGR_ARENDESLAG_NYBYGGNAD_STRANDSKYDD, STRAND_SKYDD, STRANDSKYDD,HANDELSETYP_ANSOKAN,RUBRIK_STRANDSKYDD,HANDELSESLAG_STRANDSKYDD,BYGGR_ARENDEMENING_STRANDSKYDD_FOR_NYBYGGNAD),
    STRANDSKYDD_ANLAGGANDE(Value.STRANDSKYDD_ANLAGGANDE, BYGGR_ARENDESLAG_ANLAGGANDE_STRANDSKYDD, STRAND_SKYDD, STRANDSKYDD,HANDELSETYP_ANSOKAN,RUBRIK_STRANDSKYDD,HANDELSESLAG_STRANDSKYDD,BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANLAGGANDE),
    STRANDSKYDD_ANORDNANDE(Value.STRANDSKYDD_ANORDNANDE, BYGGR_ARENDESLAG_ANORDNARE_STRANDSKYDD, STRAND_SKYDD, STRANDSKYDD,HANDELSETYP_ANSOKAN,RUBRIK_STRANDSKYDD,HANDELSESLAG_STRANDSKYDD,BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANORDNANDE),
    STRANDSKYDD_ANDRAD_ANVANDNING(Value.STRANDSKYDD_ANDRAD_ANVANDNING, BYGGR_ARENDESLAG_ANDRAD_ANVADNING_STRANDSKYDD, STRAND_SKYDD, STRANDSKYDD,HANDELSETYP_ANSOKAN,RUBRIK_STRANDSKYDD,HANDELSESLAG_STRANDSKYDD,BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANDRAD_ANVANDNING),
    
    //BYGGR OTHER
    ANMALAN_ATTEFALL(Value.ANMALAN_ATTEFALL, null, LOV_ANMALAN, ATTEFALL, HANDELSETYP_ANMALAN, RUBRIK_ANMALAN_ATTEFALL, HANDELSESLAG_ANMALAN_ATTEFALL,null),
    ANMALAN_ELDSTAD(Value.ANMALAN_ELDSTAD, null, LOV_ANMALAN, ANMALAN, HANDELSETYP_ANMALAN,null, null,null),
    

    // ===================  ECOS  ===================
    REGISTRERING_AV_LIVSMEDEL(Value.REGISTRERING_AV_LIVSMEDEL),
    ANMALAN_INSTALLATION_VARMEPUMP(Value.ANMALAN_INSTALLATION_VARMEPUMP),
    ANSOKAN_TILLSTAND_VARMEPUMP(Value.ANSOKAN_TILLSTAND_VARMEPUMP),
    ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP(Value.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP),
    ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC(Value.ANMALAN_INSTALLTION_ENSKILT_AVLOPP_UTAN_WC),
    ANMALAN_ANDRING_AVLOPPSANLAGGNING(Value.ANMALAN_ANDRING_AVLOPPSANLAGGNING),
    ANMALAN_ANDRING_AVLOPPSANORDNING(Value.ANMALAN_ANDRING_AVLOPPSANORDNING),
    ANMALAN_HALSOSKYDDSVERKSAMHET(Value.ANMALAN_HALSOSKYDDSVERKSAMHET),
    UPPDATERING_RISKKLASSNING(Value.UPPDATERING_RISKKLASSNING),
    // ===================  Other  ===================
    PARKING_PERMIT(Value.PARKING_PERMIT),
    PARKING_PERMIT_RENEWAL(Value.PARKING_PERMIT_RENEWAL),
    LOST_PARKING_PERMIT(Value.LOST_PARKING_PERMIT);
    
    @Getter
    private final String text;
    @Getter
    @JsonIgnore
    private final String arendeslag;
    
    @Getter
    @JsonIgnore
    private final String grupp;
    
    @Getter
    @JsonIgnore
    private final String typ;
    @Getter
    @JsonIgnore
    private final String handelseTyp;
    @Getter
    @JsonIgnore
    private final String handelseRubrik;
    @Getter
    @JsonIgnore
    private final String handelseSlag;
    @Getter
    @JsonIgnore
    private final String arendeMening;
    
    CaseType(String text, String arendeslag, String grupp, String typ, String handelseTyp, String handelseRubrik, String handelseSlag, String arendeMening) {
        this.text = text;
        this.arendeslag = arendeslag;
        this.grupp = grupp;
        this.typ = typ;
        this.handelseTyp = handelseTyp;
        this.handelseRubrik = handelseRubrik;
        this.handelseSlag = handelseSlag;
        this.arendeMening = arendeMening;
    }
    
    
    CaseType(String text) {
        this.text = text;
        this.arendeslag = null;
        this.grupp = null;
        this.typ = null;
        this.handelseTyp = null;
        this.handelseRubrik = null;
        this.handelseSlag = null;
        this.arendeMening = null;
    }
    
    public static class Value {
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
    }
    
    public static class ArendeslagConstants {
        //BYGGLOV
        public static final String BYGGR_ARENDESLAG_NYBYGGNAD_AV = "A";
        public static final String BYGGR_ARENDESLAG_TILLBYGGNAD_AV = "B";
        //STRANDSKYDD
        public static final String BYGGR_ARENDESLAG_ANDRAD_ANVADNING_STRANDSKYDD = "ÄNDR";
        public static final String BYGGR_ARENDESLAG_ANORDNARE_STRANDSKYDD = "AO";
        public static final String BYGGR_ARENDESLAG_ANLAGGANDE_STRANDSKYDD = "A1";
        public static final String BYGGR_ARENDESLAG_NYBYGGNAD_STRANDSKYDD = "NYB";
        
        public static final String BYGGR_ARENDESLAG_UPPSATTANDE_SKYLT = "L";
    }
    
    public static class HandelseTyp{
        public static final String HANDELSETYP_ANSOKAN = "ANSÖKAN";
        public static final String HANDELSETYP_ANMALAN = "ANM";
    }
    
    public static class HandelseRubrik {
        public static final String RUBRIK_STRANDSKYDD ="Strandskyddsdispens";
        public static final String RUBRIK_FORHANDSBESKED = "Förhandsbesked";
        public static final String RUBRIK_BYGGLOV = "Bygglov";
        public static final String RUBRIK_ANMALAN_ATTEFALL = "Anmälan Attefall";

    
    }
    
    public static class HandelseSlag {
        public static final String HANDELSESLAG_ANMALAN_ATTEFALL = "ANMATT";
        public static final String HANDELSESLAG_BYGGLOV = "Bygglov";
        public static final String HANDELSESLAG_FORHANDSBESKED = "Förhand";
        public static final String HANDELSESLAG_STRANDSKYDD ="Strand";

    
    }
    
    public static class TypConstants {
        public static final String BYGGLOV_FOR = "BL";
        public static final String FORHANDSBESKED = "FÖRF";
        public static final String ATTEFALL = "ATTANM";
        public static final String STRANDSKYDD = "DI";
        public static final String ANMALAN = "ANM";
    }
    
    public static class GruppConstants {
        public static final String LOV_ANMALAN = "LOV";
        public static final String STRAND_SKYDD = "STRA";
    }
    
    public static class ArendeMenining {
        public static final String BYGGR_ARENDEMENING_NYBYGGNAD_FORHANDSBESKED = "Förhandsbesked för nybyggnad av";
        public static final String BYGGR_ARENDEMENING_BYGGLOV_FOR_NYBYGGNAD_AV = "Bygglov för nybyggnad av";
        public static final String BYGGR_ARENDEMENING_BYGGLOV_FOR_TILLBYGGNAD = "Bygglov för tillbyggnad av";
        public static final String BYGGR_ARENDEMENING_BYGGLOV_ANDRING_ANSOKAN_OM = "Bygglov för";
        public static final String BYGGR_ARENDEMENING_BYGGLOV_FOR_UPPSSATTANDE = "Bygglov för uppsättande av ";
    
        public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_NYBYGGNAD= "Strandskyddsdispens för nybyggnad av";
        public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANLAGGANDE= "Strandskyddsdispens för anläggande av";
        public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANORDNANDE= "Strandskyddsdispens för anordnare av";
        public static final String BYGGR_ARENDEMENING_STRANDSKYDD_FOR_ANDRAD_ANVANDNING= "Strandskyddsdispens för ändrad användning av";
    }
}
