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
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casemanagement.integration.ecos.configuration.MinutMiljoConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.minutmiljo.url}", configuration = MinutMiljoConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MinutMiljoClient {

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SearchParty"
	})
	SearchPartyResponse searchParty(SearchParty searchParty);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateOrganizationParty"
	})
	CreateOrganizationPartyResponse createOrganizationParty(CreateOrganizationParty createOrganizationParty);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreatePersonParty"
	})
	CreatePersonPartyResponse createPersonParty(CreatePersonParty createPersonParty);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddPartyToFacility"
	})
	void addPartyToFacility(AddPartyToFacility addPartyToFacility);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddPartyToCase"
	})
	void addPartyToCase(AddPartyToCase addPartyToCase);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateOccurrenceOnCase"
	})
	void createOccurrenceOnCase(CreateOccurrenceOnCase createOccurrenceOnCase);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SearchCase"
	})
	SearchCaseResponse searchCase(SearchCase searchCase);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddDocumentsToCase"
	})
	void addDocumentsToCase(AddDocumentsToCase addDocumentsToCase);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/GetCase"
	})
	GetCaseResponse getCase(GetCase getCase);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateFoodFacility"
	})
	CreateFoodFacilityResponse createFoodFacility(CreateFoodFacility createFoodFacility);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateHeatPumpFacility"
	})
	CreateHeatPumpFacilityResponse createHeatPumpFacility(CreateHeatPumpFacility createHeatPumpFacility);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateIndividualSewageFacility"
	})
	CreateIndividualSewageFacilityResponse createIndividualSewageFacility(CreateIndividualSewageFacility createIndividualSewageFacility);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/CreateHealthProtectionFacility"
	})
	CreateHealthProtectionFacilityResponse createHealthProtectionFacility(CreateHealthProtectionFacility createHealthProtectionFacility);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/AddFacilityToCase"
	})
	void addFacilityToCase(AddFacilityToCase addFacilityToCase);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SearchFacility"
	})
	SearchFacilityResponse searchFacility(SearchFacility searchFacility);

	@PostMapping(consumes = MinutMiljoClientV2.TEXT_XML_UTF8, headers = {
		"SOAPAction=urn:Ecos.API.MinutMiljo.Service.V1/IMinutMiljoService/SaveFoodFacility2024RiskClassData"
	})
	void updateRiskClass(SaveFoodFacility2024RiskClassData data);
}
