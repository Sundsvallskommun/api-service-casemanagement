package se.sundsvall.casemanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.integration.rest.fb.FbClient;
import se.sundsvall.casemanagement.integration.rest.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.rest.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.rest.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.rest.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.service.util.CaseUtil;
import se.sundsvall.casemanagement.service.util.Constants;

import java.util.ArrayList;
import java.util.List;

@Service
public class FbService {

    @Value("${integration.fb.username}")
    private String fbUsername;
    @Value("${integration.fb.password}")
    private String fbPassword;
    @Value("${integration.fb.database}")
    private String fbDatabase;

    private final FbClient fbClient;
    private final RegisterbeteckningService registerbeteckningService;

    public FbService(FbClient fbClient, RegisterbeteckningService registerbeteckningService) {
        this.fbClient = fbClient;
        this.registerbeteckningService = registerbeteckningService;
    }

    public FbPropertyInfo getPropertyInfoByPropertyDesignation(String propertyDesignation) {
        propertyDesignation = propertyDesignation.trim().toUpperCase();

        FbPropertyInfo propertyInfo = new FbPropertyInfo();

        Registerbeteckningsreferens registerbeteckningsreferens = registerbeteckningService
                .getRegisterbeteckningsreferens(propertyDesignation);

        // Set FNR if the propertyDesignation in the response matches the request
        if (registerbeteckningsreferens != null) {

            ResponseDto fnrResponse = fbClient.getPropertyInfoByUuid(
                    List.of(registerbeteckningsreferens.getRegisterenhet()),
                    fbDatabase,
                    fbUsername,
                    fbPassword);

            if (fnrResponse != null
                    && CaseUtil.notNullOrEmpty(fnrResponse.getData())) {

                propertyInfo.setFnr(fnrResponse.getData().get(0).getFnr());

                ResponseDto addressResponse = fbClient.getAddressInfoByUuid(
                        List.of(registerbeteckningsreferens.getRegisterenhet()),
                        fbDatabase,
                        fbUsername,
                        fbPassword);

                if (addressResponse != null
                        && CaseUtil.notNullOrEmpty(addressResponse.getData())
                        && CaseUtil.notNullOrEmpty(addressResponse.getData().get(0).getGrupp())) {
                    propertyInfo.setAdressplatsId(addressResponse.getData().get(0).getGrupp().get(0).getAdressplatsId());
                }

                if (propertyInfo.getFnr() != null) {
                    return propertyInfo;
                }

            }
        }

        // If we reach this code, we did not find the right property
        throw Problem.valueOf(Status.BAD_REQUEST, Constants.ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND(propertyDesignation));
    }

    /**
     * Get propertyOwners of specified property
     *
     * @param propertyDesignation the specified property
     * @return List of propertyOwners
     */
    public List<StakeholderDTO> getPropertyOwnerByPropertyDesignation(String propertyDesignation) {
        List<StakeholderDTO> propertyOwners = new ArrayList<>();

        FbPropertyInfo propertyInfo = getPropertyInfoByPropertyDesignation(propertyDesignation);
        ResponseDto lagfarenAgareResponse = fbClient.getPropertyOwnerByFnr(
                List.of(propertyInfo.getFnr()),
                fbDatabase,
                fbUsername,
                fbPassword);

        if (lagfarenAgareResponse != null
                && CaseUtil.notNullOrEmpty(lagfarenAgareResponse.getData())
                && CaseUtil.notNullOrEmpty(lagfarenAgareResponse.getData().get(0).getGrupp())) {

            List<String> uuidList = lagfarenAgareResponse.getData().get(0).getGrupp().stream().map(GruppItem::getUuid).toList();

            if (CaseUtil.notNullOrEmpty(uuidList)) {
                ResponseDto agareInfoResponse = fbClient.getPropertyOwnerInfoByUuid(uuidList,
                        fbDatabase,
                        fbUsername,
                        fbPassword);

                agareInfoResponse.getData().forEach(data -> {
                    if (data.getIdentitetsnummer() == null) {
                        return;
                    }

                    AddressDTO addressDTO = getAddressForPropertyOwnerByUuid(data.getIdentitetsnummer());

                    if (data.getJuridiskForm() != null && data.getJuridiskForm().equals(Constants.FB_JURIDISK_FORM_PRIVATPERSON)) {
                        if (data.getGallandeFornamn() != null
                                && data.getGallandeEfternamn() != null) {
                            PersonDTO personDTO = new PersonDTO();
                            personDTO.setPersonalNumber(data.getIdentitetsnummer());
                            personDTO.setFirstName(data.getGallandeFornamn());
                            personDTO.setLastName(data.getGallandeEfternamn());
                            if (addressDTO != null) {
                                personDTO.setAddresses(List.of(addressDTO));
                            }
                            propertyOwners.add(personDTO);
                        }
                    } else if (data.getGallandeOrganisationsnamn() != null) {
                        OrganizationDTO organizationDTO = new OrganizationDTO();
                        organizationDTO.setOrganizationNumber(data.getIdentitetsnummer());
                        organizationDTO.setOrganizationName(data.getGallandeOrganisationsnamn());
                        if (addressDTO != null) {
                            organizationDTO.setAddresses(List.of(addressDTO));
                        }
                        propertyOwners.add(organizationDTO);
                    }
                });
            }
        }

        propertyOwners.forEach(propertyOwner -> propertyOwner.setRoles(List.of(StakeholderRole.PROPERTY_OWNER)));

        return propertyOwners;
    }

    private AddressDTO getAddressForPropertyOwnerByUuid(String persOrgNr) {

        ResponseDto response = fbClient.getPropertyOwnerAddressByPersOrgNr(List.of(persOrgNr),
                fbDatabase,
                fbUsername,
                fbPassword);

        if (response != null
                && CaseUtil.notNullOrEmpty(response.getData())) {
            AddressDTO addressDTO = new AddressDTO();

            DataItem dataItem = response.getData().get(0);

            addressDTO.setAddressCategories(List.of(AddressCategory.POSTAL_ADDRESS));
            addressDTO.setCountry(dataItem.getLand() != null ? dataItem.getLand() : Constants.SWEDEN);
            addressDTO.setCity(dataItem.getPostort());
            addressDTO.setPostalCode(dataItem.getPostnummer());
            addressDTO.setCareOf(dataItem.getCoAdress());

            String streetAddress;
            if (dataItem.getUtdelningsadress1() != null) {
                streetAddress = dataItem.getUtdelningsadress1();
            } else if (dataItem.getUtdelningsadress2() != null) {
                streetAddress = dataItem.getUtdelningsadress2();
            } else if (dataItem.getUtdelningsadress3() != null) {
                streetAddress = dataItem.getUtdelningsadress3();
            } else if (dataItem.getUtdelningsadress4() != null) {
                streetAddress = dataItem.getUtdelningsadress4();
            } else {
                streetAddress = null;
            }
            addressDTO.setStreet(streetAddress);

            return addressDTO;
        }

        return null;
    }


}
