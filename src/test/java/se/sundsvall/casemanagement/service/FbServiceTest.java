package se.sundsvall.casemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casemanagement.TestUtil;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.integration.rest.fb.FbClient;
import se.sundsvall.casemanagement.integration.rest.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.rest.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.rest.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.service.util.Constants;
import se.sundsvall.casemanagement.testutils.TestConstants;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class FbServiceTest {

    @InjectMocks
    private FbService fbService;
    @Mock
    private FbClient fbClientMock;
    @Mock
    private RegisterbeteckningService registerbeteckningServiceMock;

    @Test
    void testGetPropertyInfoByPropertyDesignation() {
        String propertyDesignation = "TEST 1:1";

        mockFb(propertyDesignation);

        var result = fbService.getPropertyInfoByPropertyDesignation(propertyDesignation);
        assertEquals(TestConstants.FNR, result.getFnr());
        assertEquals(TestConstants.ADRESSPLATS_ID, result.getAdressplatsId());
    }

    @Test
    void testGetPropertyInfoByPropertyDesignationNotFound() {
        String propertyDesignation = "TEST 1:1";
        var problem = assertThrows(ThrowableProblem.class, () -> fbService.getPropertyInfoByPropertyDesignation(propertyDesignation));
        assertEquals(Status.BAD_REQUEST, problem.getStatus());
        assertEquals(Constants.ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND(propertyDesignation), problem.getDetail());
    }

    @Test
    void testGetPropertyOwnerByPropertyDesignation() {
        PersonDTO personDTOMock = (PersonDTO) TestUtil.createStakeholder(StakeholderType.PERSON, new ArrayList<>());
        TestUtil.mockFbPropertyOwners(fbClientMock, List.of(personDTOMock));

        String propertyDesignation = "TEST 1:1";
        mockFb(propertyDesignation);

        ResponseDto getPropertyOwnerAddressByPersOrgNrMockResponse = new ResponseDto();
        DataItem dataItem = new DataItem();
        AddressDTO personDTOMockAddressDTO = personDTOMock.getAddresses().get(0);
        dataItem.setUtdelningsadress1(personDTOMockAddressDTO.getStreet());
        dataItem.setLand(personDTOMockAddressDTO.getCountry());
        dataItem.setPostort(personDTOMockAddressDTO.getCity());
        dataItem.setPostnummer(personDTOMockAddressDTO.getPostalCode());
        dataItem.setCoAdress(personDTOMockAddressDTO.getCareOf());
        getPropertyOwnerAddressByPersOrgNrMockResponse.setData(List.of(dataItem));
        lenient().doReturn(getPropertyOwnerAddressByPersOrgNrMockResponse).when(fbClientMock).getPropertyOwnerAddressByPersOrgNr(any(), any(), any(), any());

        var result = fbService.getPropertyOwnerByPropertyDesignation(propertyDesignation);
        assertEquals(1, result.size());

        PersonDTO personResult = (PersonDTO) result.get(0);
        assertEquals(personDTOMock.getPersonalNumber(), personResult.getPersonalNumber());
        assertEquals(personDTOMock.getFirstName(), personResult.getFirstName());
        assertEquals(personDTOMock.getLastName(), personResult.getLastName());
        assertEquals(1, personResult.getAddresses().size());
        AddressDTO addressDTO = personResult.getAddresses().get(0);
        assertEquals(AddressCategory.POSTAL_ADDRESS, addressDTO.getAddressCategories().get(0));
        assertEquals(personDTOMockAddressDTO.getStreet(), addressDTO.getStreet());
        assertEquals(personDTOMockAddressDTO.getCountry(), addressDTO.getCountry());
        assertEquals(personDTOMockAddressDTO.getCity(), addressDTO.getCity());
        assertEquals(personDTOMockAddressDTO.getPostalCode(), addressDTO.getPostalCode());
        assertEquals(personDTOMockAddressDTO.getCareOf(), addressDTO.getCareOf());
    }

    private void mockFb(String propertyDesignation) {
        Registerbeteckningsreferens registerbeteckningsreferensMock = new Registerbeteckningsreferens();
        registerbeteckningsreferensMock.setBeteckning(TestConstants.PROPERTY_DESIGNATION_FILLA);
        registerbeteckningsreferensMock.setBeteckningsid("ny-4020855");
        registerbeteckningsreferensMock.setRegisterenhet("e19981ad-34b2-4e14-88f5-133f61ca85aa");

        doReturn(registerbeteckningsreferensMock).when(registerbeteckningServiceMock).getRegisterbeteckningsreferens(propertyDesignation);

        ResponseDto getPropertyInfoByUuidMockResponse = new ResponseDto();
        getPropertyInfoByUuidMockResponse.setData(List.of(new DataItem()));
        getPropertyInfoByUuidMockResponse.getData().get(0).setFnr(TestConstants.FNR);
        doReturn(getPropertyInfoByUuidMockResponse).when(fbClientMock).getPropertyInfoByUuid(any(), any(), any(), any());

        ResponseDto getAddressInfoByUuidMockResponse = new ResponseDto();
        getAddressInfoByUuidMockResponse.setData(List.of(new DataItem()));
        getAddressInfoByUuidMockResponse.getData().get(0).setGrupp(List.of(new GruppItem()));
        getAddressInfoByUuidMockResponse.getData().get(0).getGrupp().get(0).setAdressplatsId(TestConstants.ADRESSPLATS_ID);
        doReturn(getAddressInfoByUuidMockResponse).when(fbClientMock).getAddressInfoByUuid(any(), any(), any(), any());

    }
}
