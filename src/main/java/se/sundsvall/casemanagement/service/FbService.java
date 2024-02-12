package se.sundsvall.casemanagement.service;

import java.util.ArrayList;
import java.util.List;

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
import se.sundsvall.casemanagement.integration.fb.FbClient;
import se.sundsvall.casemanagement.integration.fb.model.DataItem;
import se.sundsvall.casemanagement.integration.fb.model.FbPropertyInfo;
import se.sundsvall.casemanagement.integration.fb.model.GruppItem;
import se.sundsvall.casemanagement.integration.fb.model.ResponseDto;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

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

		final FbPropertyInfo propertyInfo = new FbPropertyInfo();

		final Registerbeteckningsreferens registerbeteckningsreferens = registerbeteckningService
			.getRegisterbeteckningsreferens(propertyDesignation);

		// Set FNR if the propertyDesignation in the response matches the request
		if (registerbeteckningsreferens != null) {

			final ResponseDto fnrResponse = fbClient.getPropertyInfoByUuid(
				List.of(registerbeteckningsreferens.getRegisterenhet()),
				fbDatabase,
				fbUsername,
				fbPassword);

			if ((fnrResponse != null)
				&& CaseUtil.notNullOrEmpty(fnrResponse.getData())) {

				propertyInfo.setFnr(fnrResponse.getData().getFirst().getFnr());

				final ResponseDto addressResponse = fbClient.getAddressInfoByUuid(
					List.of(registerbeteckningsreferens.getRegisterenhet()),
					fbDatabase,
					fbUsername,
					fbPassword);

				if ((addressResponse != null)
					&& CaseUtil.notNullOrEmpty(addressResponse.getData())
					&& CaseUtil.notNullOrEmpty(addressResponse.getData().getFirst().getGrupp())) {
					propertyInfo.setAdressplatsId(addressResponse.getData().getFirst().getGrupp().getFirst().getAdressplatsId());
				}

				if (propertyInfo.getFnr() != null) {
					return propertyInfo;
				}

			}
		}

		// If we reach this code, we did not find the right property
		throw Problem.valueOf(Status.BAD_REQUEST, String.format(Constants.ERR_MSG_PROPERTY_DESIGNATION_NOT_FOUND, propertyDesignation));
	}

	/**
	 * Get propertyOwners of specified property
	 *
	 * @param  propertyDesignation the specified property
	 * @return                     List of propertyOwners
	 */
	public List<StakeholderDTO> getPropertyOwnerByPropertyDesignation(String propertyDesignation) {
		final List<StakeholderDTO> propertyOwners = new ArrayList<>();

		final FbPropertyInfo propertyInfo = getPropertyInfoByPropertyDesignation(propertyDesignation);
		final ResponseDto lagfarenAgareResponse = fbClient.getPropertyOwnerByFnr(
			List.of(propertyInfo.getFnr()),
			fbDatabase,
			fbUsername,
			fbPassword);

		if ((lagfarenAgareResponse != null)
			&& CaseUtil.notNullOrEmpty(lagfarenAgareResponse.getData())
			&& CaseUtil.notNullOrEmpty(lagfarenAgareResponse.getData().getFirst().getGrupp())) {

			final List<String> uuidList = lagfarenAgareResponse.getData().getFirst().getGrupp().stream().map(GruppItem::getUuid).toList();

			if (CaseUtil.notNullOrEmpty(uuidList)) {
				final ResponseDto agareInfoResponse = fbClient.getPropertyOwnerInfoByUuid(uuidList,
					fbDatabase,
					fbUsername,
					fbPassword);

				agareInfoResponse.getData().forEach(data -> {
					if (data.getIdentitetsnummer() == null) {
						return;
					}

					final AddressDTO addressDTO = getAddressForPropertyOwnerByUuid(data.getIdentitetsnummer());

					if ((data.getJuridiskForm() != null) && Constants.FB_JURIDISK_FORM_PRIVATPERSON.equals(data.getJuridiskForm())) {
						if ((data.getGallandeFornamn() != null)
							&& (data.getGallandeEfternamn() != null)) {
							final PersonDTO personDTO = new PersonDTO();
							personDTO.setPersonalNumber(data.getIdentitetsnummer());
							personDTO.setFirstName(data.getGallandeFornamn());
							personDTO.setLastName(data.getGallandeEfternamn());
							if (addressDTO != null) {
								personDTO.setAddresses(List.of(addressDTO));
							}
							propertyOwners.add(personDTO);
						}
					} else if (data.getGallandeOrganisationsnamn() != null) {
						final OrganizationDTO organizationDTO = new OrganizationDTO();
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

		final ResponseDto response = fbClient.getPropertyOwnerAddressByPersOrgNr(List.of(persOrgNr),
			fbDatabase,
			fbUsername,
			fbPassword);

		if ((response != null)
			&& CaseUtil.notNullOrEmpty(response.getData())) {
			final AddressDTO addressDTO = new AddressDTO();

			final DataItem dataItem = response.getData().getFirst();

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
