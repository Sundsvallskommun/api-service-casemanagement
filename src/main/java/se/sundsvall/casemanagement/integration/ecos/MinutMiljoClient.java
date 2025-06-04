package se.sundsvall.casemanagement.integration.ecos;

import static se.sundsvall.casemanagement.integration.ecos.configuration.MinutMiljoConfiguration.CLIENT_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import minutmiljo.AddDocumentsToCase;
import minutmiljo.AddFacilityToCase;
import minutmiljo.AddPartyToCase;
import minutmiljo.AddPartyToFacility;
import minutmiljo.CreateFoodFacility;
import minutmiljo.CreateFoodFacilityResponse;
import minutmiljo.CreateHealthProtectionFacility;
import minutmiljo.CreateHealthProtectionFacilityResponse;
import minutmiljo.CreateHeatPumpFacility;
import minutmiljo.CreateHeatPumpFacilityResponse;
import minutmiljo.CreateIndividualSewageFacility;
import minutmiljo.CreateIndividualSewageFacilityResponse;
import minutmiljo.CreateOccurrenceOnCase;
import minutmiljo.CreateOrganizationParty;
import minutmiljo.CreateOrganizationPartyResponse;
import minutmiljo.CreatePersonParty;
import minutmiljo.CreatePersonPartyResponse;
import minutmiljo.GetCase;
import minutmiljo.GetCaseResponse;
import minutmiljo.SaveFoodFacility2024RiskClassData;
import minutmiljo.SearchCase;
import minutmiljo.SearchCaseResponse;
import minutmiljo.SearchFacility;
import minutmiljo.SearchFacilityResponse;
import minutmiljo.SearchParty;
import minutmiljo.SearchPartyResponse;
import minutmiljoV2.RegisterDocument;
import minutmiljoV2.RegisterDocumentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casemanagement.integration.ecos.configuration.MinutMiljoConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.minutmiljo.url}", configuration = MinutMiljoConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MinutMiljoClient {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

	@PostMapping(path = "/MinutMiljoServiceV2.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V2/IMinutMiljoServiceV2/RegisterDocument"
	})
	RegisterDocumentResponse registerDocumentV2(RegisterDocument registerDocument);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SearchParty"
	})
	SearchPartyResponse searchParty(SearchParty searchParty);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateOrganizationParty"
	})
	CreateOrganizationPartyResponse createOrganizationParty(CreateOrganizationParty createOrganizationParty);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreatePersonParty"
	})
	CreatePersonPartyResponse createPersonParty(CreatePersonParty createPersonParty);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddPartyToFacility"
	})
	void addPartyToFacility(AddPartyToFacility addPartyToFacility);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddPartyToCase"
	})
	void addPartyToCase(AddPartyToCase addPartyToCase);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateOccurrenceOnCase"
	})
	void createOccurrenceOnCase(CreateOccurrenceOnCase createOccurrenceOnCase);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SearchCase"
	})
	SearchCaseResponse searchCase(SearchCase searchCase);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddDocumentsToCase"
	})
	void addDocumentsToCase(AddDocumentsToCase addDocumentsToCase);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/GetCase"
	})
	GetCaseResponse getCase(GetCase getCase);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateFoodFacility"
	})
	CreateFoodFacilityResponse createFoodFacility(CreateFoodFacility createFoodFacility);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateHeatPumpFacility"
	})
	CreateHeatPumpFacilityResponse createHeatPumpFacility(CreateHeatPumpFacility createHeatPumpFacility);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateIndividualSewageFacility"
	})
	CreateIndividualSewageFacilityResponse createIndividualSewageFacility(CreateIndividualSewageFacility createIndividualSewageFacility);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateHealthProtectionFacility"
	})
	CreateHealthProtectionFacilityResponse createHealthProtectionFacility(CreateHealthProtectionFacility createHealthProtectionFacility);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddFacilityToCase"
	})
	void addFacilityToCase(AddFacilityToCase addFacilityToCase);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SearchFacility"
	})
	SearchFacilityResponse searchFacility(SearchFacility searchFacility);

	@PostMapping(path = "/MinutMiljoService.svc", consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SaveFoodFacility2024RiskClassData"
	})
	void updateRiskClass(SaveFoodFacility2024RiskClassData data);
}
