package se.sundsvall.casemanagement.integration.edpfuture;

import edpfuture.ArrayOfstring;
import edpfuture.BuildingV12;
import edpfuture.EServiceType;
import edpfuture.GetAuthorizedUsers;
import edpfuture.GetAuthorizedUsersResponse;
import edpfuture.GetBuildingsByAutorizationRoleV12;
import edpfuture.GetBuildingsByAutorizationRoleV12Response;
import edpfuture.GetRenhOrderTypesForServiceV17;
import edpfuture.GetRenhOrderTypesForServiceV17Response;
import edpfuture.GetServicesByBuildingIdForOrder;
import edpfuture.GetServicesByBuildingIdForOrderResponse;
import edpfuture.OrderTypeV14;
import edpfuture.RHService;
import edpfuture.SubmitOrderTypeApplicationV14;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class EDPFutureService {

	private final EDPFutureClient edpFutureClient;
	private final CaseMappingService caseMappingService;

	public EDPFutureService(final EDPFutureClient edpFutureClient, final CaseMappingService caseMappingService) {
		this.edpFutureClient = edpFutureClient;
		this.caseMappingService = caseMappingService;
	}

	public void handleOrder(final FutureCaseDTO futureCaseDTO, final String municipalityId) {
		var identityNumber = futureCaseDTO.getExtraParameters().get("IdentityNumber");
		var address = futureCaseDTO.getExtraParameters().get("Address");

		sendOrder(identityNumber, address);
		caseMappingService.postCaseMapping(futureCaseDTO, futureCaseDTO.getExternalCaseId(), SystemType.EDPFUTURE, municipalityId);
	}

	public void sendOrder(final String identityNumber, final String address) {
		var customerId = getCustomerId(identityNumber);
		var buildingId = getBuildingId(address, identityNumber, customerId);
		var serviceId = getServiceId(buildingId);
		var orderType = getOrderType(serviceId);
		submitOrderTypeApplication(customerId, buildingId, serviceId, orderType);
	}

	/**
	 * Gets the customerId for a given identity number. A customer can have multiple customerIds, but we are only interested
	 * in the one that has an approved eService of type "Order".
	 *
	 * @param  identityNumber the legal id number of the customer.
	 * @return                the customerId associated with the approved eService of type "Order" for the given identity
	 *                        number.
	 */
	public int getCustomerId(final String identityNumber) {
		var request = createGetAuthorizedUsersRequest(identityNumber);
		var response = edpFutureClient.getAuthorizedUsers(request);

		return getCustomerId(response);
	}

	/**
	 * Extracts the customerId from the GetAuthorizedUsersResponse. It looks for an authorized user that has an approved
	 * eService of type "Order" and returns the associated customerId.
	 *
	 * @param  response GetAuthorizedUsersResponse containing the list of authorized users and their approved eServices.
	 * @return          customerId for given identity number that has an approved eService of type "Order".
	 */
	private int getCustomerId(final GetAuthorizedUsersResponse response) {
		var users = response.getGetAuthorizedUsersResult().getResultValue().getAuthorizedUser();
		var user = users.stream()
			.filter(authorizedUser -> authorizedUser.getApprovedEservices().getEServiceType().stream()
				.anyMatch(service -> "Order".equals(service.value())))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY,
				"Failed to retrieve customerId from EDP Future. No approved eService of type 'Order' found for the given identity number."));

		return user.getCustomerId();
	}

	private GetAuthorizedUsers createGetAuthorizedUsersRequest(final String identityNumber) {
		var request = new GetAuthorizedUsers();
		request.setIdentityNumber(identityNumber);
		return request;
	}

	/**
	 * Gets the buildingId for a given address, identity number, and customerId. It retrieves the list of buildings
	 * associated with the customerId and identity number and returns the buildingId of the building that matches the given
	 * address.
	 *
	 * @param  address        the given address to filter the buildings by.
	 * @param  identityNumber the legal id number of the customer.
	 * @param  customerId     the customerId associated with the customer.
	 * @return                the buildingId of the building that matches the given address.
	 */
	public String getBuildingId(final String address, final String identityNumber, final int customerId) {
		var request = createGetBuildingsByAutorizationRoleV12(identityNumber, customerId);
		var response = edpFutureClient.getBuildingsByAutorizationRoleV1_2(request);
		return getBuilding(response, address).getID();
	}

	/**
	 * Extracts the buildingId from the GetBuildingsByAutorizationRoleV12Response. It looks for a building that matches the
	 * given address and returns its buildingId.
	 *
	 * @param  response GetBuildingsByAutorizationRoleV12Response containing the list of buildings associated with the
	 *                  customerId and identity number.
	 * @param  address  the given address to filter the buildings by.
	 * @return          Building for the given identity number and customerId that matches the given address.
	 */
	private BuildingV12 getBuilding(final GetBuildingsByAutorizationRoleV12Response response, final String address) {
		var informationHolder = response.getGetBuildingsByAutorizationRoleV12Result().getResultValue().getCustomerBuildingInformationHolderV12().stream()
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY,
				"Failed to retrieve buildingId from EDP Future. No customer building information found."));

		return informationHolder.getBuildingCollection().getBuildingV12().stream()
			.filter(buildingV12 -> buildingV12.getAddress().contains(address))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY,
				"Failed to retrieve buildingId from EDP Future. No building found for the given address."));
	}

	private GetBuildingsByAutorizationRoleV12 createGetBuildingsByAutorizationRoleV12(final String identityNumber, final int customerId) {
		var request = new GetBuildingsByAutorizationRoleV12();
		request.setIdentitynumber(identityNumber);
		request.setCustomerIds(new ArrayOfstring().withString(String.valueOf(customerId)));
		request.setEServiceType(EServiceType.ORDER);
		return request;
	}

	/**
	 * Gets the serviceId for a given buildingId. It retrieves the list of services associated with the buildingId and
	 * returns the serviceId of the service that has a waste type of "Restavfall".
	 *
	 * @param  buildingId the buildingId to retrieve the services for.
	 * @return            the serviceId of the service that has a waste type of "Restavfall" for the given buildingId.
	 */
	private int getServiceId(final String buildingId) {
		var request = createGetServicesByBuildingIdForOrder(buildingId);
		var response = edpFutureClient.getServicesByBuildingIdForOrder(request);

		return getRhService(response, buildingId).getID();
	}

	/**
	 * Gets the service with waste type "Restavfall" for the given buildingId from the
	 * GetServicesByBuildingIdForOrderResponse. It looks for a service that matches the given buildingId and has a waste
	 * type of "Restavfall", and returns it.
	 *
	 * @param  response   GetServicesByBuildingIdForOrderResponse containing the list of services associated with the
	 *                    buildingId.
	 * @param  buildingId the buildingId to filter the services by.
	 * @return            the service with waste type "Restavfall" for the given buildingId.
	 */
	private RHService getRhService(final GetServicesByBuildingIdForOrderResponse response, final String buildingId) {
		var rhServices = response.getGetServicesByBuildingIdForOrderResult().getResultValue().getRhServices().getRHService();
		return rhServices.stream()
			.filter(service -> buildingId.equals(service.getBuildingID()) && "Restavfall".equals(service.getWasteType()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY,
				"Failed to retrieve serviceId from EDP Future. No service found for the given building id."));
	}

	private GetServicesByBuildingIdForOrder createGetServicesByBuildingIdForOrder(final String buildingId) {
		var request = new GetServicesByBuildingIdForOrder();
		request.setBuildingID(buildingId);
		return request;
	}

	/**
	 * Retrieves the order type associated with the given service ID.
	 *
	 * @param  serviceId the ID of the service for which the order type is to be retrieved
	 * @return           the order type corresponding to the specified service ID
	 */
	public OrderTypeV14 getOrderType(final int serviceId) {
		var request = createGetRenhOrderTypesForServiceV17(serviceId);
		var response = edpFutureClient.getRenhOrderTypesForServiceV1_7(request);

		return getOrderType(response);
	}

	/**
	 * Extracts the order type with the name "Extra säck" from the GetRenhOrderTypesForServiceV17Response. It looks for an
	 * order type that matches the given serviceId and has the name "Extra säck", and returns it. If no such order type is
	 * found, it throws
	 * an exception.
	 *
	 * @param  response GetRenhOrderTypesForServiceV17Response containing the list of order types associated with the
	 *                  serviceId.
	 * @return          the order type with the name "Extra säck" for the given serviceId.
	 */
	private OrderTypeV14 getOrderType(final GetRenhOrderTypesForServiceV17Response response) {
		var orderType = response.getGetRenhOrderTypesForServiceV17Result().getResultValue().getOrderTypeV17().stream()
			.filter(service -> "Extra säck".equals(service.getText()))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(BAD_GATEWAY, "Failed to retrieve order type from EDP Future. No order type found with the given name."));

		// Sets the number of "Extra säck" to order.
		orderType.getOrderRows().getOrderRowV14().stream()
			.findFirst()
			.ifPresentOrElse(row -> row.setQuantity(1), () -> {
				throw Problem.valueOf(BAD_GATEWAY, "Failed to retrieve order type from EDP Future. No order rows found for the given order type.");
			});

		return orderType;
	}

	private GetRenhOrderTypesForServiceV17 createGetRenhOrderTypesForServiceV17(final int serviceId) {
		var request = new GetRenhOrderTypesForServiceV17();
		request.setServiceId(serviceId);
		return request;

	}

	/**
	 * Submits an order for the given customerId, buildingId, serviceId, and orderType. It creates a
	 * SubmitOrderTypeApplicationV14 request with the provided parameters and sends it to EDP Future.
	 *
	 * @param customerId the customerId associated with the given legal identity number.
	 * @param buildingId the buildingId associated with the customerId and the given address.
	 * @param serviceId  the serviceId associated with the buildingId and waste type "Restavfall".
	 * @param orderType  the order type associated with the serviceId and with the name "Extra säck".
	 */
	public void submitOrderTypeApplication(final int customerId, final String buildingId, final int serviceId, final OrderTypeV14 orderType) {
		var request = createSubmitOrderTypeApplicationV14(customerId, buildingId, serviceId, orderType);
		edpFutureClient.submitOrderTypeApplicationV1_4(request);
	}

	private SubmitOrderTypeApplicationV14 createSubmitOrderTypeApplicationV14(final int customerId, final String buildingId, final int serviceId, final OrderTypeV14 orderType) {
		var request = new SubmitOrderTypeApplicationV14();
		request.setCustomerId(String.valueOf(customerId));
		request.setBuildingId(buildingId);
		request.setServiceId(serviceId);
		request.setOrderType(orderType);
		return request;
	}

}
