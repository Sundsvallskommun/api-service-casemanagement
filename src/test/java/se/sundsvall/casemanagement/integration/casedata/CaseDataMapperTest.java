package se.sundsvall.casemanagement.integration.casedata;

import static generated.client.casedata.Stakeholder.TypeEnum.ORGANIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static se.sundsvall.casemanagement.TestUtil.createAddressDTO;
import static se.sundsvall.casemanagement.TestUtil.createAttachmentDTO;
import static se.sundsvall.casemanagement.TestUtil.createCoordinatesDTO;
import static se.sundsvall.casemanagement.TestUtil.createExtraParameters;
import static se.sundsvall.casemanagement.TestUtil.createFacilityDTO;
import static se.sundsvall.casemanagement.TestUtil.createOtherCaseDTO;
import static se.sundsvall.casemanagement.TestUtil.createStakeholderDTO;
import static se.sundsvall.casemanagement.integration.casedata.CaseDataMapper.toContactInformation;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;

import generated.client.casedata.ContactInformation;
import generated.client.casedata.Errand;
import generated.client.casedata.PatchErrand;
import generated.client.casedata.Stakeholder;

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
	void toErrand() {
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

		final var result = CaseDataMapper.toErrand(otherCase);

		assertThat(result.getCaseType()).isEqualTo(otherCase.getCaseType());
		assertThat(result.getCaseTitleAddition()).isEqualTo(otherCase.getCaseTitleAddition());
		assertThat(result.getDescription()).isEqualTo(otherCase.getDescription());
		assertThat(result.getExternalCaseId()).isEqualTo(otherCase.getExternalCaseId());
		assertThat(result.getExtraParameters()).isEqualTo(otherCase.getExtraParameters());
		assertThat(result.getPriority()).isEqualTo(Errand.PriorityEnum.MEDIUM);

		assertThat(result.getStakeholders()).isEqualTo(CaseDataMapper.toStakeholders(otherCase.getStakeholders()));
		assertThat(result.getFacilities()).isEqualTo(CaseDataMapper.toFacilities(otherCase.getFacilities()));
	}

	@Test
	void toFacilities() {
		final var facilities = List.of(createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP), createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP));

		final var result = CaseDataMapper.toFacilities(facilities);

		assertThat(result).hasSize(facilities.size());

		for (final var dto : result) {
			assertThat(dto).isEqualTo(CaseDataMapper.toFacility(facilities.get(result.indexOf(dto))));
		}
	}

	@Test
	void toFacility() {
		final var facilityDTO = createFacilityDTO(CaseType.ANSOKAN_TILLSTAND_VARMEPUMP);

		final var result = CaseDataMapper.toFacility(facilityDTO);

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
		otherCaseDTO.getExtraParameters().put("application.priority", "HIGH");

		final var result = CaseDataMapper.toPatchErrand(otherCaseDTO);

		assertThat(result).satisfies(errandDTO -> {
			assertThat(errandDTO.getCaseType()).isEqualTo(PatchErrand.CaseTypeEnum.fromValue(otherCaseDTO.getCaseType()));
			assertThat(errandDTO.getCaseTitleAddition()).isEqualTo(otherCaseDTO.getCaseTitleAddition());
			assertThat(errandDTO.getDescription()).isEqualTo(otherCaseDTO.getDescription());
			assertThat(errandDTO.getExternalCaseId()).isEqualTo(otherCaseDTO.getExternalCaseId());
			assertThat(errandDTO.getExtraParameters()).isEqualTo(otherCaseDTO.getExtraParameters());
			assertThat(errandDTO.getPriority()).isEqualTo(PatchErrand.PriorityEnum.HIGH);
			assertThat(errandDTO.getFacilities()).isEqualTo(CaseDataMapper.toFacilities(otherCaseDTO.getFacilities()));
		});
	}

	@Test
	void toStakeholders() {

		//Arrange
		final var organizationDTOOrgnumberWithoutHyphen = (OrganizationDTO) createStakeholderDTO(StakeholderType.ORGANIZATION, List.of("someRole"));
		organizationDTOOrgnumberWithoutHyphen.setOrganizationNumber(organizationDTOOrgnumberWithoutHyphen.getOrganizationNumber().replace("-", ""));

		final var role = "someRole";
		final var role2 = "someOtherRole";
		final var role3 = "thirdRole";
		final var rolemap = Map.of("roles", role + "," + role2 + ", " + role3);
		final var stakeholders = List.of(
			createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(role2)),
			organizationDTOOrgnumberWithoutHyphen,
			createStakeholderDTO(StakeholderType.PERSON, List.of(role)));

		stakeholders.forEach(stakeholderDTO -> stakeholderDTO.setExtraParameters(rolemap));
		final var organizationDTO = (OrganizationDTO) stakeholders.getFirst();
		final var personDTO = (PersonDTO) stakeholders.getLast();

		//Act
		final var result = CaseDataMapper.toStakeholders(stakeholders);

		assertThat(result).hasSize(3).extracting(
			Stakeholder::getRoles,
			Stakeholder::getContactInformation,
			Stakeholder::getAddresses,
			Stakeholder::getExtraParameters,
			Stakeholder::getOrganizationNumber,
			Stakeholder::getOrganizationName,
			Stakeholder::getAuthorizedSignatory,
			Stakeholder::getPersonId,
			Stakeholder::getFirstName,
			Stakeholder::getLastName,
			Stakeholder::getType).containsExactly(
			tuple(List.of(role2, role3, role),
				toContactInformation(organizationDTO),
				CaseDataMapper.toStakeholderAddresses(organizationDTO.getAddresses()),
				organizationDTO.getExtraParameters(),
				organizationDTO.getOrganizationNumber(),
				organizationDTO.getOrganizationName(),
				organizationDTO.getAuthorizedSignatory(),
				null,
				null,
				null,
				ORGANIZATION),
			tuple(List.of(role2, role3, role),
				toContactInformation(organizationDTOOrgnumberWithoutHyphen),
				CaseDataMapper.toStakeholderAddresses(organizationDTOOrgnumberWithoutHyphen.getAddresses()),
				organizationDTOOrgnumberWithoutHyphen.getExtraParameters(),
				organizationDTOOrgnumberWithoutHyphen.getOrganizationNumber().substring(0, 6) + "-" + organizationDTOOrgnumberWithoutHyphen.getOrganizationNumber().substring(6),
				organizationDTOOrgnumberWithoutHyphen.getOrganizationName(),
				organizationDTOOrgnumberWithoutHyphen.getAuthorizedSignatory(),
				null,
				null,
				null,
				ORGANIZATION),
			tuple(List.of(role2, role3, role),
				toContactInformation(personDTO),
				CaseDataMapper.toStakeholderAddresses(personDTO.getAddresses()),
				personDTO.getExtraParameters(),
				null,
				null,
				null,
				personDTO.getPersonId(),
				personDTO.getFirstName(),
				personDTO.getLastName(),
				Stakeholder.TypeEnum.PERSON)
		);
	}

	@Test
	void toFacilityAddress() {
		final var addressDTO = createAddressDTO(List.of(AddressCategory.POSTAL_ADDRESS));

		final var result = CaseDataMapper.toFacilityAddress(addressDTO);

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
			assertThat(address.getLocation()).isEqualTo(CaseDataMapper.toCoordinates(addressDTO.getLocation()));
			assertThat(address.getIsZoningPlanArea()).isEqualTo(addressDTO.getIsZoningPlanArea());
			assertThat(address.getInvoiceMarking()).isEqualTo(addressDTO.getInvoiceMarking());
			assertThat(address.getAddressCategory()).isEqualTo(generated.client.casedata.Address.AddressCategoryEnum.VISITING_ADDRESS);
		});
	}

	@Test
	void toStakeholderAddresses() {
		final var address = createAddressDTO(List.of(AddressCategory.POSTAL_ADDRESS, AddressCategory.INVOICE_ADDRESS, AddressCategory.VISITING_ADDRESS));

		final var result = CaseDataMapper.toStakeholderAddresses(List.of(address));

		assertThat(result).hasSize(3);
		for (final var dto : result) {
			assertThat(dto.getAddressCategory()).isInstanceOf(generated.client.casedata.Address.AddressCategoryEnum.class);
			assertThat(dto.getStreet()).isEqualTo(address.getStreet());
			assertThat(dto.getHouseNumber()).isEqualTo(address.getHouseNumber());
			assertThat(dto.getApartmentNumber()).isEqualTo(address.getAppartmentNumber());
			assertThat(dto.getPostalCode()).isEqualTo(address.getPostalCode());
			assertThat(dto.getCity()).isEqualTo(address.getCity());
			assertThat(dto.getCountry()).isEqualTo(address.getCountry());
			assertThat(dto.getCareOf()).isEqualTo(address.getCareOf());
			assertThat(dto.getAttention()).isEqualTo(address.getAttention());
			assertThat(dto.getPropertyDesignation()).isEqualTo(address.getPropertyDesignation());
			assertThat(dto.getLocation()).isEqualTo(CaseDataMapper.toCoordinates(address.getLocation()));
			assertThat(dto.getIsZoningPlanArea()).isEqualTo(address.getIsZoningPlanArea());
			assertThat(dto.getInvoiceMarking()).isEqualTo(address.getInvoiceMarking());
		}
	}

	@Test
	void toCoordinates() {
		final var coordinates = createCoordinatesDTO();

		final var result = CaseDataMapper.toCoordinates(coordinates);

		assertThat(result).satisfies(coord -> {
			assertThat(coord.getLatitude()).isEqualTo(coordinates.getLatitude());
			assertThat(coord.getLongitude()).isEqualTo(coordinates.getLongitude());
		});
	}

	@Test
	void toRoles() {

		final var roles = List.of("role1", "role2", "role3");

		final var result = CaseDataMapper.toRoles(roles, null);

		assertThat(result).isEqualTo(roles);
	}

	@Test
	void toContactInformationDTOs() {
		final var stakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of("someRole"));

		final var result = toContactInformation(stakeholder);

		assertThat(result).hasSize(3);
		assertThat(result.getFirst().getContactType()).isEqualTo(ContactInformation.ContactTypeEnum.CELLPHONE);
		assertThat(result.getFirst().getValue()).isEqualTo(stakeholder.getCellphoneNumber());
		assertThat(result.get(1).getContactType()).isEqualTo(ContactInformation.ContactTypeEnum.PHONE);
		assertThat(result.get(1).getValue()).isEqualTo(stakeholder.getPhoneNumber());
		assertThat(result.getLast().getContactType()).isEqualTo(ContactInformation.ContactTypeEnum.EMAIL);
		assertThat(result.getLast().getValue()).isEqualTo(stakeholder.getEmailAddress());
	}

}
