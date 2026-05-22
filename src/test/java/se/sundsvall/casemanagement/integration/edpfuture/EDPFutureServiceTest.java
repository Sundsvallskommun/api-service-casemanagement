package se.sundsvall.casemanagement.integration.edpfuture;

import edpfuture.AllServices;
import edpfuture.ArrayOfAuthorizedUser;
import edpfuture.ArrayOfBuildingV12;
import edpfuture.ArrayOfCustomerBuildingInformationHolderV12;
import edpfuture.ArrayOfEServiceType;
import edpfuture.ArrayOfOrderRowV14;
import edpfuture.ArrayOfOrderTypeV17;
import edpfuture.ArrayOfRHService;
import edpfuture.AuthorizedUser;
import edpfuture.BuildingV12;
import edpfuture.CustomerBuildingInformationHolderV12;
import edpfuture.EServiceType;
import edpfuture.GetAuthorizedUsers;
import edpfuture.GetAuthorizedUsersResponse;
import edpfuture.GetBuildingsByAutorizationRoleV12;
import edpfuture.GetBuildingsByAutorizationRoleV12Response;
import edpfuture.GetRenhOrderTypesForServiceV17;
import edpfuture.GetRenhOrderTypesForServiceV17Response;
import edpfuture.GetServicesByBuildingIdForOrder;
import edpfuture.GetServicesByBuildingIdForOrderResponse;
import edpfuture.OperationResultOfAllServicesFbShh6Ke;
import edpfuture.OperationResultOfArrayOfAuthorizedUserFbShh6Ke;
import edpfuture.OperationResultOfArrayOfCustomerBuildingInformationHolderV12FbShh6Ke;
import edpfuture.OperationResultOfArrayOfOrderTypeV17FbShh6Ke;
import edpfuture.OrderRowV14;
import edpfuture.OrderTypeV14;
import edpfuture.OrderTypeV17;
import edpfuture.RHService;
import edpfuture.SubmitOrderTypeApplicationV14;
import generated.client.party.PartyType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.party.PartyIntegration;
import se.sundsvall.casemanagement.service.CaseMappingService;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class EDPFutureServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String EXTERNAL_CASE_ID = "ext-1";
	private static final String PARTY_ID = "3ed5bc30-6308-4fd5-a5a7-78d7f96f4438";
	private static final String IDENTITY_NUMBER = "202601052380";
	private static final String STREET = "Testgatan";
	private static final String HOUSE_NUMBER = "1";
	private static final String ADDRESS = "Testgatan 1";
	private static final String BUILDING_ID = "BLD-123";
	private static final String WASTE_TYPE = "Restavfall";
	private static final String ORDER_TYPE_TEXT = "Extra säck";
	private static final String QUANTITY = "1";
	private static final int CUSTOMER_ID = 42;
	private static final int SERVICE_ID = 99;

	@Mock
	private EDPFutureClient edpFutureClientMock;

	@Mock
	private CaseMappingService caseMappingServiceMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@InjectMocks
	private EDPFutureService edpFutureService;

	@Captor
	private ArgumentCaptor<GetAuthorizedUsers> getAuthorizedUsersCaptor;

	@Captor
	private ArgumentCaptor<GetBuildingsByAutorizationRoleV12> getBuildingsCaptor;

	@Captor
	private ArgumentCaptor<GetServicesByBuildingIdForOrder> getServicesCaptor;

	@Captor
	private ArgumentCaptor<GetRenhOrderTypesForServiceV17> getOrderTypesCaptor;

	@Captor
	private ArgumentCaptor<SubmitOrderTypeApplicationV14> submitOrderCaptor;

	@Test
	void handleOrderHappyPath() {
		final var dto = createFutureCaseDTO(PARTY_ID, STREET, HOUSE_NUMBER, QUANTITY);
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.thenReturn(Map.of(PartyType.PRIVATE, IDENTITY_NUMBER));
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.ORDER));
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse(ADDRESS));
		when(edpFutureClientMock.getServicesByBuildingIdForOrder(any()))
			.thenReturn(createGetServicesResponse(WASTE_TYPE));
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse(ORDER_TYPE_TEXT, true));

		edpFutureService.handleOrder(dto, MUNICIPALITY_ID);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verify(edpFutureClientMock).getAuthorizedUsers(getAuthorizedUsersCaptor.capture());
		assertThat(getAuthorizedUsersCaptor.getValue().getIdentityNumber()).isEqualTo(IDENTITY_NUMBER);
		verify(edpFutureClientMock).getBuildingsByAutorizationRoleV1_2(getBuildingsCaptor.capture());
		assertThat(getBuildingsCaptor.getValue().getIdentitynumber()).isEqualTo(IDENTITY_NUMBER);
		verify(edpFutureClientMock).submitOrderTypeApplicationV1_4(any());
		verify(caseMappingServiceMock).postCaseMapping(dto, EXTERNAL_CASE_ID, SystemType.EDPFUTURE, MUNICIPALITY_ID);
	}

	@Test
	void handleOrderPicksFirstPersonAmongMixedStakeholders() {
		final var dto = FutureCaseDTO.builder()
			.withExternalCaseId(EXTERNAL_CASE_ID)
			.withStakeholders(List.of(
				OrganizationDTO.builder().withOrganizationName("Acme AB").build(),
				PersonDTO.builder().withPersonId(PARTY_ID).build()))
			.withFacilities(List.of(FacilityDTO.builder()
				.withAddress(AddressDTO.builder().withStreet(STREET).withHouseNumber(HOUSE_NUMBER).build())
				.build()))
			.withExtraParameters(Map.of("Quantity", QUANTITY))
			.build();
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.thenReturn(Map.of(PartyType.PRIVATE, IDENTITY_NUMBER));
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.ORDER));
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse(ADDRESS));
		when(edpFutureClientMock.getServicesByBuildingIdForOrder(any()))
			.thenReturn(createGetServicesResponse(WASTE_TYPE));
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse(ORDER_TYPE_TEXT, true));

		edpFutureService.handleOrder(dto, MUNICIPALITY_ID);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
	}

	@Test
	void handleOrderNoPersonStakeholder() {
		final var dto = FutureCaseDTO.builder()
			.withExternalCaseId(EXTERNAL_CASE_ID)
			.withStakeholders(List.of(OrganizationDTO.builder().withOrganizationName("Acme AB").build()))
			.withFacilities(List.of(FacilityDTO.builder()
				.withAddress(AddressDTO.builder().withStreet(STREET).withHouseNumber(HOUSE_NUMBER).build())
				.build()))
			.withExtraParameters(Map.of("Quantity", QUANTITY))
			.build();

		assertThatThrownBy(() -> edpFutureService.handleOrder(dto, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_REQUEST.getReasonPhrase())
			.hasMessageContaining("FutureCase requires at least one PERSON stakeholder with personId");

		verifyNoInteractions(partyIntegrationMock, edpFutureClientMock, caseMappingServiceMock);
	}

	@Test
	void handleOrderPersonMissingPersonId() {
		final var dto = createFutureCaseDTO(null, STREET, HOUSE_NUMBER, QUANTITY);

		assertThatThrownBy(() -> edpFutureService.handleOrder(dto, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_REQUEST.getReasonPhrase())
			.hasMessageContaining("FutureCase requires at least one PERSON stakeholder with personId");

		verifyNoInteractions(partyIntegrationMock, edpFutureClientMock, caseMappingServiceMock);
	}

	@Test
	void handleOrderPartyReturnsNoPrivate() {
		final var dto = createFutureCaseDTO(PARTY_ID, STREET, HOUSE_NUMBER, QUANTITY);
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.thenReturn(Map.of());

		assertThatThrownBy(() -> edpFutureService.handleOrder(dto, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_REQUEST.getReasonPhrase())
			.hasMessageContaining("Could not resolve PRIVATE legalId for partyId: " + PARTY_ID);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);
		verifyNoInteractions(edpFutureClientMock, caseMappingServiceMock);
	}

	@Test
	void handleOrderFacilityAddressMissing() {
		final var dto = FutureCaseDTO.builder()
			.withExternalCaseId(EXTERNAL_CASE_ID)
			.withStakeholders(List.of(PersonDTO.builder().withPersonId(PARTY_ID).build()))
			.withFacilities(List.of(FacilityDTO.builder().build()))
			.withExtraParameters(Map.of("Quantity", QUANTITY))
			.build();
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.thenReturn(Map.of(PartyType.PRIVATE, IDENTITY_NUMBER));

		assertThatThrownBy(() -> edpFutureService.handleOrder(dto, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_REQUEST.getReasonPhrase())
			.hasMessageContaining("FutureCase facility must contain an address");

		verifyNoInteractions(edpFutureClientMock, caseMappingServiceMock);
	}

	@Test
	void handleOrderFacilityAddressBlank() {
		final var dto = createFutureCaseDTO(PARTY_ID, null, null, QUANTITY);
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.thenReturn(Map.of(PartyType.PRIVATE, IDENTITY_NUMBER));

		assertThatThrownBy(() -> edpFutureService.handleOrder(dto, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_REQUEST.getReasonPhrase())
			.hasMessageContaining("FutureCase facility address must contain street and/or houseNumber");

		verifyNoInteractions(edpFutureClientMock, caseMappingServiceMock);
	}

	@Test
	void handleOrderMissingQuantity() {
		final var dto = createFutureCaseDTO(PARTY_ID, STREET, HOUSE_NUMBER, null);
		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.thenReturn(Map.of(PartyType.PRIVATE, IDENTITY_NUMBER));

		assertThatThrownBy(() -> edpFutureService.handleOrder(dto, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_REQUEST.getReasonPhrase())
			.hasMessageContaining("FutureCase extraParameters must contain non-blank 'Quantity'");

		verifyNoInteractions(edpFutureClientMock, caseMappingServiceMock);
	}

	@Test
	void sendOrder() {
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.ORDER));
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse(ADDRESS));
		when(edpFutureClientMock.getServicesByBuildingIdForOrder(any()))
			.thenReturn(createGetServicesResponse(WASTE_TYPE));
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse(ORDER_TYPE_TEXT, true));

		edpFutureService.sendOrder(IDENTITY_NUMBER, ADDRESS, QUANTITY);

		verify(edpFutureClientMock).getAuthorizedUsers(getAuthorizedUsersCaptor.capture());
		assertThat(getAuthorizedUsersCaptor.getValue().getIdentityNumber()).isEqualTo(IDENTITY_NUMBER);

		verify(edpFutureClientMock).getBuildingsByAutorizationRoleV1_2(getBuildingsCaptor.capture());
		var buildingsRequest = getBuildingsCaptor.getValue();
		assertThat(buildingsRequest.getIdentitynumber()).isEqualTo(IDENTITY_NUMBER);
		assertThat(buildingsRequest.getCustomerIds().getString()).containsExactly(String.valueOf(CUSTOMER_ID));
		assertThat(buildingsRequest.getEServiceType()).isEqualTo(EServiceType.ORDER);

		verify(edpFutureClientMock).getServicesByBuildingIdForOrder(getServicesCaptor.capture());
		assertThat(getServicesCaptor.getValue().getBuildingID()).isEqualTo(BUILDING_ID);

		verify(edpFutureClientMock).getRenhOrderTypesForServiceV1_7(getOrderTypesCaptor.capture());
		assertThat(getOrderTypesCaptor.getValue().getServiceId()).isEqualTo(SERVICE_ID);

		verify(edpFutureClientMock).submitOrderTypeApplicationV1_4(submitOrderCaptor.capture());
		var submitRequest = submitOrderCaptor.getValue();
		assertThat(submitRequest.getCustomerId()).isEqualTo(String.valueOf(CUSTOMER_ID));
		assertThat(submitRequest.getBuildingId()).isEqualTo(BUILDING_ID);
		assertThat(submitRequest.getServiceId()).isEqualTo(SERVICE_ID);
		assertThat(submitRequest.getOrderType()).isNotNull();
		assertThat(submitRequest.getOrderType().getText()).isEqualTo(ORDER_TYPE_TEXT);

		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getCustomerId() {
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.ORDER));

		var result = edpFutureService.getCustomerId(IDENTITY_NUMBER);

		assertThat(result).isEqualTo(CUSTOMER_ID);
		verify(edpFutureClientMock).getAuthorizedUsers(getAuthorizedUsersCaptor.capture());
		assertThat(getAuthorizedUsersCaptor.getValue().getIdentityNumber()).isEqualTo(IDENTITY_NUMBER);
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getCustomerIdNoOrderEService() {
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.MY_SERVICES));

		assertThatThrownBy(() -> edpFutureService.getCustomerId(IDENTITY_NUMBER))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_GATEWAY.getReasonPhrase())
			.hasMessageContaining("No approved eService of type 'Order' found");

		verify(edpFutureClientMock).getAuthorizedUsers(any());
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getBuildingId() {
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse(ADDRESS));

		var result = edpFutureService.getBuildingId(ADDRESS, IDENTITY_NUMBER, CUSTOMER_ID);

		assertThat(result).isEqualTo(BUILDING_ID);
		verify(edpFutureClientMock).getBuildingsByAutorizationRoleV1_2(getBuildingsCaptor.capture());
		var request = getBuildingsCaptor.getValue();
		assertThat(request.getIdentitynumber()).isEqualTo(IDENTITY_NUMBER);
		assertThat(request.getCustomerIds().getString()).containsExactly(String.valueOf(CUSTOMER_ID));
		assertThat(request.getEServiceType()).isEqualTo(EServiceType.ORDER);
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getBuildingIdNoCustomerBuildingInformationHolder() {
		var response = new GetBuildingsByAutorizationRoleV12Response()
			.withGetBuildingsByAutorizationRoleV12Result(
				new OperationResultOfArrayOfCustomerBuildingInformationHolderV12FbShh6Ke()
					.withResultValue(new ArrayOfCustomerBuildingInformationHolderV12()));

		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any())).thenReturn(response);

		assertThatThrownBy(() -> edpFutureService.getBuildingId(ADDRESS, IDENTITY_NUMBER, CUSTOMER_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_GATEWAY.getReasonPhrase())
			.hasMessageContaining("No customer building information found");

		verify(edpFutureClientMock).getBuildingsByAutorizationRoleV1_2(any());
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getBuildingIdNoMatchingAddress() {
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse("Annan Adress 99"));

		assertThatThrownBy(() -> edpFutureService.getBuildingId(ADDRESS, IDENTITY_NUMBER, CUSTOMER_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_GATEWAY.getReasonPhrase())
			.hasMessageContaining("No building found for the given address");

		verify(edpFutureClientMock).getBuildingsByAutorizationRoleV1_2(any());
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void sendOrderVerifiesCorrectServiceId() {
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.ORDER));
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse(ADDRESS));
		when(edpFutureClientMock.getServicesByBuildingIdForOrder(any()))
			.thenReturn(createGetServicesResponse(WASTE_TYPE));
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse(ORDER_TYPE_TEXT, true));

		edpFutureService.sendOrder(IDENTITY_NUMBER, ADDRESS, QUANTITY);

		verify(edpFutureClientMock).getRenhOrderTypesForServiceV1_7(getOrderTypesCaptor.capture());
		assertThat(getOrderTypesCaptor.getValue().getServiceId()).isEqualTo(SERVICE_ID);
	}

	@Test
	void sendOrderNoMatchingService() {
		when(edpFutureClientMock.getAuthorizedUsers(any()))
			.thenReturn(createGetAuthorizedUsersResponse(EServiceType.ORDER));
		when(edpFutureClientMock.getBuildingsByAutorizationRoleV1_2(any()))
			.thenReturn(createGetBuildingsResponse(ADDRESS));
		when(edpFutureClientMock.getServicesByBuildingIdForOrder(any()))
			.thenReturn(createGetServicesResponse("Matavfall"));

		assertThatThrownBy(() -> edpFutureService.sendOrder(IDENTITY_NUMBER, ADDRESS, QUANTITY))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_GATEWAY.getReasonPhrase())
			.hasMessageContaining("No service found for the given building id");
	}

	@Test
	void getOrderType() {
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse(ORDER_TYPE_TEXT, true));

		var result = edpFutureService.getOrderType(SERVICE_ID, QUANTITY);

		assertThat(result).isNotNull();
		assertThat(result.getText()).isEqualTo(ORDER_TYPE_TEXT);
		assertThat(result.getOrderRows().getOrderRowV14().getFirst().getQuantity()).isEqualTo(1);
		verify(edpFutureClientMock).getRenhOrderTypesForServiceV1_7(getOrderTypesCaptor.capture());
		assertThat(getOrderTypesCaptor.getValue().getServiceId()).isEqualTo(SERVICE_ID);
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getOrderTypeNoExtraSack() {
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse("Annan typ", true));

		assertThatThrownBy(() -> edpFutureService.getOrderType(SERVICE_ID, QUANTITY))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_GATEWAY.getReasonPhrase())
			.hasMessageContaining("No order type found with the given name");

		verify(edpFutureClientMock).getRenhOrderTypesForServiceV1_7(any());
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void getOrderTypeNoOrderRows() {
		when(edpFutureClientMock.getRenhOrderTypesForServiceV1_7(any()))
			.thenReturn(createGetOrderTypesResponse(ORDER_TYPE_TEXT, false));

		assertThatThrownBy(() -> edpFutureService.getOrderType(SERVICE_ID, QUANTITY))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(BAD_GATEWAY.getReasonPhrase())
			.hasMessageContaining("No order rows found for the given order type");

		verify(edpFutureClientMock).getRenhOrderTypesForServiceV1_7(any());
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	@Test
	void submitOrderTypeApplication() {
		var orderType = new OrderTypeV14()
			.withText(ORDER_TYPE_TEXT)
			.withOrderRows(new ArrayOfOrderRowV14().withOrderRowV14(new OrderRowV14()));

		edpFutureService.submitOrderTypeApplication(CUSTOMER_ID, BUILDING_ID, SERVICE_ID, orderType);

		verify(edpFutureClientMock).submitOrderTypeApplicationV1_4(submitOrderCaptor.capture());
		var request = submitOrderCaptor.getValue();
		assertThat(request.getCustomerId()).isEqualTo(String.valueOf(CUSTOMER_ID));
		assertThat(request.getBuildingId()).isEqualTo(BUILDING_ID);
		assertThat(request.getServiceId()).isEqualTo(SERVICE_ID);
		assertThat(request.getOrderType()).isEqualTo(orderType);
		verifyNoMoreInteractions(edpFutureClientMock);
	}

	private FutureCaseDTO createFutureCaseDTO(final String personId, final String street, final String houseNumber, final String quantity) {
		final var extraParameters = new HashMap<String, String>();
		if (quantity != null) {
			extraParameters.put("Quantity", quantity);
		}
		return FutureCaseDTO.builder()
			.withExternalCaseId(EXTERNAL_CASE_ID)
			.withStakeholders(List.of(PersonDTO.builder().withPersonId(personId).build()))
			.withFacilities(List.of(FacilityDTO.builder()
				.withAddress(AddressDTO.builder().withStreet(street).withHouseNumber(houseNumber).build())
				.build()))
			.withExtraParameters(extraParameters)
			.build();
	}

	private GetAuthorizedUsersResponse createGetAuthorizedUsersResponse(final EServiceType eServiceType) {
		var user = new AuthorizedUser()
			.withCustomerId(EDPFutureServiceTest.CUSTOMER_ID)
			.withApprovedEservices(new ArrayOfEServiceType().withEServiceType(eServiceType));

		return new GetAuthorizedUsersResponse()
			.withGetAuthorizedUsersResult(
				new OperationResultOfArrayOfAuthorizedUserFbShh6Ke()
					.withResultValue(new ArrayOfAuthorizedUser().withAuthorizedUser(user)));
	}

	private GetBuildingsByAutorizationRoleV12Response createGetBuildingsResponse(final String address) {
		var building = new BuildingV12()
			.withID(EDPFutureServiceTest.BUILDING_ID)
			.withAddress(address);

		var holder = new CustomerBuildingInformationHolderV12()
			.withBuildingCollection(new ArrayOfBuildingV12().withBuildingV12(building));

		return new GetBuildingsByAutorizationRoleV12Response()
			.withGetBuildingsByAutorizationRoleV12Result(
				new OperationResultOfArrayOfCustomerBuildingInformationHolderV12FbShh6Ke()
					.withResultValue(new ArrayOfCustomerBuildingInformationHolderV12()
						.withCustomerBuildingInformationHolderV12(holder)));
	}

	private GetServicesByBuildingIdForOrderResponse createGetServicesResponse(final String wasteType) {
		var service = new RHService()
			.withID(EDPFutureServiceTest.SERVICE_ID)
			.withBuildingID(EDPFutureServiceTest.BUILDING_ID)
			.withWasteType(wasteType);

		return new GetServicesByBuildingIdForOrderResponse()
			.withGetServicesByBuildingIdForOrderResult(
				new OperationResultOfAllServicesFbShh6Ke()
					.withResultValue(new AllServices()
						.withRhServices(new ArrayOfRHService().withRHService(service))));
	}

	private GetRenhOrderTypesForServiceV17Response createGetOrderTypesResponse(final String text, final boolean withOrderRows) {
		var orderType = new OrderTypeV17().withText(text);

		if (withOrderRows) {
			orderType.withOrderRows(new ArrayOfOrderRowV14().withOrderRowV14(new OrderRowV14()));
		} else {
			orderType.withOrderRows(new ArrayOfOrderRowV14());
		}

		return new GetRenhOrderTypesForServiceV17Response()
			.withGetRenhOrderTypesForServiceV17Result(
				new OperationResultOfArrayOfOrderTypeV17FbShh6Ke()
					.withResultValue(new ArrayOfOrderTypeV17().withOrderTypeV17(orderType)));
	}

}
