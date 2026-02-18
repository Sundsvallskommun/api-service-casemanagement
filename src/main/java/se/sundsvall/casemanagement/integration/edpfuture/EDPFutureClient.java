package se.sundsvall.casemanagement.integration.edpfuture;

import edpfuture.GetAuthorizedUsers;
import edpfuture.GetAuthorizedUsersResponse;
import edpfuture.GetBuildingsByAutorizationRoleV12;
import edpfuture.GetBuildingsByAutorizationRoleV12Response;
import edpfuture.GetRenhOrderTypesForServiceV17;
import edpfuture.GetRenhOrderTypesForServiceV17Response;
import edpfuture.GetServicesByBuildingIdForOrder;
import edpfuture.GetServicesByBuildingIdForOrderResponse;
import edpfuture.SubmitOrderTypeApplicationV14;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import se.sundsvall.casemanagement.integration.edpfuture.configuration.EDPFutureConfiguration;

import static se.sundsvall.casemanagement.integration.edpfuture.configuration.EDPFutureConfiguration.REGISTRATION_ID;

@FeignClient(name = REGISTRATION_ID,
	url = "${integration.edp-future.url}",
	configuration = EDPFutureConfiguration.class)
@CircuitBreaker(name = REGISTRATION_ID)
public interface EDPFutureClient {

	String TEXT_XML_UTF8 = "text/xml;charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=http://tempuri.org/IGetAuthorizedUser/GetAuthorizedUsers"
	})
	GetAuthorizedUsersResponse getAuthorizedUsers(final GetAuthorizedUsers request);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=http://tempuri.org/IGetBuildingsByAutorizationRoleV1_2/GetBuildingsByAutorizationRoleV1_2"
	})
	GetBuildingsByAutorizationRoleV12Response getBuildingsByAutorizationRoleV1_2(final GetBuildingsByAutorizationRoleV12 request);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=http://tempuri.org/IGetServicesByBuildingIdForOrder/GetServicesByBuildingIdForOrder"
	})
	GetServicesByBuildingIdForOrderResponse getServicesByBuildingIdForOrder(final GetServicesByBuildingIdForOrder request);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=http://tempuri.org/IGetRenhOrderTypesForServiceV1_7/GetRenhOrderTypesForServiceV1_7"
	})
	GetRenhOrderTypesForServiceV17Response getRenhOrderTypesForServiceV1_7(final GetRenhOrderTypesForServiceV17 request);

	@PostMapping(consumes = TEXT_XML_UTF8, headers = {
		"SOAPAction=http://tempuri.org/ISubmitOrderTypeApplicationV1_4/SubmitOrderTypeApplicationV1_4"
	})
	SubmitOrderTypeApplicationV14 submitOrderTypeApplicationV1_4(final SubmitOrderTypeApplicationV14 request);

}
