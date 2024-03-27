package se.sundsvall.casemanagement.integration.casedata;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casemanagement.TestUtil.createAddressDTO;
import static se.sundsvall.casemanagement.TestUtil.createAttachmentDTO;
import static se.sundsvall.casemanagement.TestUtil.createCoordinatesDTO;
import static se.sundsvall.casemanagement.TestUtil.createExtraParameters;
import static se.sundsvall.casemanagement.TestUtil.createFacilityDTO;
import static se.sundsvall.casemanagement.TestUtil.createOtherCaseDTO;
import static se.sundsvall.casemanagement.TestUtil.createStakeholderDTO;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;

import generated.client.casedata.ContactInformationDTO;
import generated.client.casedata.PatchErrandDTO;

class CaseDataMapperTest {

	@Test
	void toAttachment() {
		final var attachmentDTO = AttachmentDTO.builder()
			.withExtension("pdf")
			.withCategory("someCategory")
			.withFile("someFile")
			.withName("someName")
			.withNote("someNote")
			.withExtraParameters(createExtraParameters())
			.build();
		final var errandNumber = "someErrandNumber";

		final var result = CaseDataMapper.toAttachment(attachmentDTO, errandNumber);

		assertThat(result).satisfies(bean -> {
			assertThat(bean.getExtension()).isEqualTo(attachmentDTO.getExtension());
			assertThat(bean.getCategory()).isEqualTo(attachmentDTO.getCategory());
			assertThat(bean.getFile()).isEqualTo(attachmentDTO.getFile());
			assertThat(bean.getName()).isEqualTo(attachmentDTO.getName());
			assertThat(bean.getNote()).isEqualTo(attachmentDTO.getNote());
			assertThat(bean.getExtraParameters()).isEqualTo(attachmentDTO.getExtraParameters());
			assertThat(bean.getErrandNumber()).isEqualTo(errandNumber);
		});
	}

	@Test
	void toErrandDTO() {
		final var otherCase = new OtherCaseDTO();
		final var attachmentDTO = createAttachmentDTO(AttachmentCategory.ANMALAN_VARMEPUMP);
		final PersonDTO stakeholderDTO = (PersonDTO) createStakeholderDTO(StakeholderType.PERSON, List.of("someRole"));
		final var facilityDTO = createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP);

		otherCase.setAttachments(List.of(attachmentDTO));
		otherCase.setStakeholders(List.of(stakeholderDTO));
		otherCase.setFacilities(List.of(facilityDTO));
		otherCase.setCaseType("caseType");
		otherCase.setCaseTitleAddition("caseTitleAddition");
		otherCase.setDescription("description");
		otherCase.setExternalCaseId("externalCaseId");
		otherCase.setExtraParameters(createExtraParameters());

		final var result = CaseDataMapper.toErrandDTO(otherCase);

		assertThat(result.getCaseType()).isEqualTo(otherCase.getCaseType());
		assertThat(result.getCaseTitleAddition()).isEqualTo(otherCase.getCaseTitleAddition());
		assertThat(result.getDescription()).isEqualTo(otherCase.getDescription());
		assertThat(result.getExternalCaseId()).isEqualTo(otherCase.getExternalCaseId());
		assertThat(result.getExtraParameters()).isEqualTo(otherCase.getExtraParameters());

		assertThat(result.getStakeholders()).isEqualTo(CaseDataMapper.toStakeholderDTOs(otherCase.getStakeholders()));
		assertThat(result.getFacilities()).isEqualTo(CaseDataMapper.toFacilityDTOs(otherCase.getFacilities()));
	}

	@Test
	void toFacilityDTOs() {
		final var facilities = List.of(createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP), createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP));

		final var result = CaseDataMapper.toFacilityDTOs(facilities);

		assertThat(result).hasSize(facilities.size());

		for (var dto : result) {
			assertThat(dto).isEqualTo(CaseDataMapper.toFacilityDTO(facilities.get(result.indexOf(dto))));
		}
	}

	@Test
	void toFacilityDTO() {
		final var facilityDTO = createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP);

		final var result = CaseDataMapper.toFacilityDTO(facilityDTO);

		assertThat(result).satisfies(facility -> {
			assertThat(facility.getMainFacility()).isEqualTo(facilityDTO.isMainFacility());
			assertThat(facility.getFacilityCollectionName()).isEqualTo(facilityDTO.getFacilityCollectionName());
			assertThat(facility.getDescription()).isEqualTo(facilityDTO.getDescription());
			assertThat(facility.getFacilityType()).isEqualTo(facilityDTO.getFacilityType());
			assertThat(facility.getExtraParameters()).isEqualTo(facilityDTO.getExtraParameters());
		});
	}

	@Test
	void toPatchErrandDTO() {
		final var otherCaseDTO = createOtherCaseDTO();

		final var result = CaseDataMapper.toPatchErrandDTO(otherCaseDTO);

		assertThat(result).satisfies(errandDTO -> {
			assertThat(errandDTO.getCaseType()).isEqualTo(PatchErrandDTO.CaseTypeEnum.fromValue(otherCaseDTO.getCaseType()));
			assertThat(errandDTO.getCaseTitleAddition()).isEqualTo(otherCaseDTO.getCaseTitleAddition());
			assertThat(errandDTO.getDescription()).isEqualTo(otherCaseDTO.getDescription());
			assertThat(errandDTO.getExternalCaseId()).isEqualTo(otherCaseDTO.getExternalCaseId());
			assertThat(errandDTO.getExtraParameters()).isEqualTo(otherCaseDTO.getExtraParameters());
		});
	}

	@Test
	void toStakeholderDTOs() {
		final var stakeholders = List.of(
			createStakeholderDTO(StakeholderType.PERSON, List.of("someRole")),
			createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("someRole")));
		final var personDTO = (PersonDTO) stakeholders.getFirst();
		final var organizationDTO = (OrganizationDTO) stakeholders.getLast();

		final var result = CaseDataMapper.toStakeholderDTOs(stakeholders);

		assertThat(result.getFirst()).satisfies(person -> {
			assertThat(person.getRoles()).isEqualTo(personDTO.getRoles());
			assertThat(person.getContactInformation()).isEqualTo(CaseDataMapper.toContactInformationDTO(personDTO));
			assertThat(person.getAddresses()).isEqualTo(CaseDataMapper.toStakeholderAddressDTOs(personDTO.getAddresses()));
			assertThat(person.getExtraParameters()).isEqualTo(personDTO.getExtraParameters());

			assertThat(person.getPersonId()).isEqualTo(personDTO.getPersonId());
			assertThat(person.getFirstName()).isEqualTo(personDTO.getFirstName());
			assertThat(person.getLastName()).isEqualTo(personDTO.getLastName());
			assertThat(person.getType()).isEqualTo(generated.client.casedata.StakeholderDTO.TypeEnum.PERSON);
		});

		assertThat(result.getLast()).satisfies(org -> {
			assertThat(org.getRoles()).isEqualTo(organizationDTO.getRoles());
			assertThat(org.getContactInformation()).isEqualTo(CaseDataMapper.toContactInformationDTO(organizationDTO));
			assertThat(org.getAddresses()).isEqualTo(CaseDataMapper.toStakeholderAddressDTOs(organizationDTO.getAddresses()));
			assertThat(org.getExtraParameters()).isEqualTo(organizationDTO.getExtraParameters());

			assertThat(org.getOrganizationNumber()).isEqualTo(organizationDTO.getOrganizationNumber());
			assertThat(org.getOrganizationName()).isEqualTo(organizationDTO.getOrganizationName());
			assertThat(org.getAuthorizedSignatory()).isEqualTo(organizationDTO.getAuthorizedSignatory());
			assertThat(org.getType()).isEqualTo(generated.client.casedata.StakeholderDTO.TypeEnum.ORGANIZATION);
		});

	}

	@Test
	void toFacilityAddressDTO() {
		final var addressDTO = createAddressDTO(List.of(AddressCategory.POSTAL_ADDRESS));

		final var result = CaseDataMapper.toFacilityAddressDTO(addressDTO);

		assertThat(result).satisfies(address -> {
			assertThat(address.getCity()).isEqualTo(addressDTO.getCity());
			assertThat(address.getCountry()).isEqualTo(addressDTO.getCountry());
			assertThat(address.getPostalCode()).isEqualTo(addressDTO.getPostalCode());
			assertThat(address.getStreet()).isEqualTo(addressDTO.getStreet());
			assertThat(address.getHouseNumber()).isEqualTo(addressDTO.getHouseNumber());
			assertThat(address.getCareOf()).isEqualTo(addressDTO.getCareOf());
			assertThat(address.getAttention()).isEqualTo(addressDTO.getAttention());
			assertThat(address.getPropertyDesignation()).isEqualTo(addressDTO.getPropertyDesignation());
			assertThat(address.getApartmentNumber()).isEqualTo(addressDTO.getAppartmentNumber());
			assertThat(address.getLocation()).isEqualTo(CaseDataMapper.toCoordinatesDTO(addressDTO.getLocation()));
			assertThat(address.getIsZoningPlanArea()).isEqualTo(addressDTO.getIsZoningPlanArea());
			assertThat(address.getInvoiceMarking()).isEqualTo(addressDTO.getInvoiceMarking());
			assertThat(address.getAddressCategory()).isEqualTo(generated.client.casedata.AddressDTO.AddressCategoryEnum.VISITING_ADDRESS);
		});
	}

	@Test
	void toStakeholderAddressDTOs() {
		final var address = createAddressDTO(List.of(AddressCategory.POSTAL_ADDRESS, AddressCategory.INVOICE_ADDRESS, AddressCategory.VISITING_ADDRESS));

		final var result = CaseDataMapper.toStakeholderAddressDTOs(List.of(address));

		assertThat(result).hasSize(3);
		for (var dto : result) {
			assertThat(dto.getAddressCategory()).isInstanceOf(generated.client.casedata.AddressDTO.AddressCategoryEnum.class);
			assertThat(dto.getStreet()).isEqualTo(address.getStreet());
			assertThat(dto.getHouseNumber()).isEqualTo(address.getHouseNumber());
			assertThat(dto.getApartmentNumber()).isEqualTo(address.getAppartmentNumber());
			assertThat(dto.getPostalCode()).isEqualTo(address.getPostalCode());
			assertThat(dto.getCity()).isEqualTo(address.getCity());
			assertThat(dto.getCountry()).isEqualTo(address.getCountry());
			assertThat(dto.getCareOf()).isEqualTo(address.getCareOf());
			assertThat(dto.getAttention()).isEqualTo(address.getAttention());
			assertThat(dto.getPropertyDesignation()).isEqualTo(address.getPropertyDesignation());
			assertThat(dto.getLocation()).isEqualTo(CaseDataMapper.toCoordinatesDTO(address.getLocation()));
			assertThat(dto.getIsZoningPlanArea()).isEqualTo(address.getIsZoningPlanArea());
			assertThat(dto.getInvoiceMarking()).isEqualTo(address.getInvoiceMarking());
		}
	}

	@Test
	void toCoordinatesDTO() {
		final var coordinates = createCoordinatesDTO();

		final var result = CaseDataMapper.toCoordinatesDTO(coordinates);

		assertThat(result).satisfies(coord -> {
			assertThat(coord.getLatitude()).isEqualTo(coordinates.getLatitude());
			assertThat(coord.getLongitude()).isEqualTo(coordinates.getLongitude());
		});
	}

	@Test
	void toContactInformationDTOs() {
		final var stakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of("someRole"));

		final var result = CaseDataMapper.toContactInformationDTO(stakeholder);

		assertThat(result).hasSize(3);
		assertThat(result.getFirst().getContactType()).isEqualTo(ContactInformationDTO.ContactTypeEnum.CELLPHONE);
		assertThat(result.getFirst().getValue()).isEqualTo(stakeholder.getCellphoneNumber());
		assertThat(result.get(1).getContactType()).isEqualTo(ContactInformationDTO.ContactTypeEnum.PHONE);
		assertThat(result.get(1).getValue()).isEqualTo(stakeholder.getPhoneNumber());
		assertThat(result.getLast().getContactType()).isEqualTo(ContactInformationDTO.ContactTypeEnum.EMAIL);
		assertThat(result.getLast().getValue()).isEqualTo(stakeholder.getEmailAddress());
	}
}
