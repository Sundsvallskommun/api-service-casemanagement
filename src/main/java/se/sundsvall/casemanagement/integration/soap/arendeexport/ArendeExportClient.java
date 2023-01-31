package se.sundsvall.casemanagement.integration.soap.arendeexport;

import static se.sundsvall.casemanagement.integration.soap.minutmiljo.MinutMiljoClientV2.TEXT_XML_UTF8;

import arendeexport.GetArende;
import arendeexport.GetArendeResponse;
import arendeexport.GetDocument;
import arendeexport.GetDocumentResponse;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import arendeexport.GetUpdatedArenden;
import arendeexport.GetUpdatedArendenResponse;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeResponse;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casemanagement.integration.soap.arendeexport.configuration.ArendeExportConfiguration;

@FeignClient(name = "arendeexport", url = "${integration.arendeexport.url}", configuration = ArendeExportConfiguration.class)
@CircuitBreaker(name = "arendeexport")
public interface ArendeExportClient {



    @PostMapping(consumes = TEXT_XML_UTF8, headers = {"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetUpdatedArenden"})
    GetUpdatedArendenResponse getUpdatedArenden(GetUpdatedArenden getUpdatedArenden);

    @PostMapping(consumes = TEXT_XML_UTF8, headers = {"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetDocument"})
    GetDocumentResponse getDocument(GetDocument getDocument);

    @PostMapping(consumes = TEXT_XML_UTF8, headers = {"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetArende"})
    GetArendeResponse getArende(GetArende getArende);

    @PostMapping(consumes = TEXT_XML_UTF8, headers = {"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/SaveNewArende"})
    SaveNewArendeResponse saveNewArende(SaveNewArende saveNewArende);

    @PostMapping(consumes = TEXT_XML_UTF8, headers = {"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/SaveNewHandelse"})
    SaveNewHandelseResponse saveNewHandelse(SaveNewHandelse saveNewHandelse);

    @PostMapping(consumes = TEXT_XML_UTF8, headers = {"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetRelateradeArendenByPersOrgNrAndRole"})
    GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRole(GetRelateradeArendenByPersOrgNrAndRole input);
}
