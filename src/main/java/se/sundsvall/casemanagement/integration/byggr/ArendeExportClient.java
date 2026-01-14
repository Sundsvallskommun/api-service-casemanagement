package se.sundsvall.casemanagement.integration.byggr;

import arendeexport.GetArende;
import arendeexport.GetArendeResponse;
import arendeexport.GetDocument;
import arendeexport.GetDocumentResponse;
import arendeexport.GetIntressent;
import arendeexport.GetIntressentResponse;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRole;
import arendeexport.GetRelateradeArendenByPersOrgNrAndRoleResponse;
import arendeexport.GetRemisserByPersOrgNr;
import arendeexport.GetRemisserByPersOrgNrResponse;
import arendeexport.GetUpdatedArenden;
import arendeexport.GetUpdatedArendenResponse;
import arendeexport.SaveNewArende;
import arendeexport.SaveNewArendeResponse;
import arendeexport.SaveNewHandelse;
import arendeexport.SaveNewHandelseResponse;
import arendeexport.SaveNewRemissvar;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casemanagement.integration.byggr.configuration.ArendeExportConfiguration;

@FeignClient(name = "arendeexport", url = "${integration.arendeexport.url}", configuration = ArendeExportConfiguration.class)
@CircuitBreaker(name = "arendeexport")
public interface ArendeExportClient {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetUpdatedArenden"
	})
	GetUpdatedArendenResponse getUpdatedArenden(GetUpdatedArenden getUpdatedArenden);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetDocument"
	})
	GetDocumentResponse getDocument(GetDocument getDocument);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetArende"
	})
	GetArendeResponse getArende(GetArende getArende);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/SaveNewArende"
	})
	SaveNewArendeResponse saveNewArende(SaveNewArende saveNewArende);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/SaveNewHandelse"
	})
	SaveNewHandelseResponse saveNewHandelse(SaveNewHandelse saveNewHandelse);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetRelateradeArendenByPersOrgNrAndRole"
	})
	GetRelateradeArendenByPersOrgNrAndRoleResponse getRelateradeArendenByPersOrgNrAndRole(GetRelateradeArendenByPersOrgNrAndRole input);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetIntressent"
	})
	GetIntressentResponse getIntressent(GetIntressent input);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/GetRemisserByPersOrgNr"
	})
	GetRemisserByPersOrgNrResponse getRemisserByPersOrgNr(GetRemisserByPersOrgNr getRemisserByPersOrgNr);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=www.tekis.se/ServiceContract/V4/IExportArenden/SaveNewRemissvar"
	})
	void saveNewRemissvar(SaveNewRemissvar saveNewRemissvar);
}
