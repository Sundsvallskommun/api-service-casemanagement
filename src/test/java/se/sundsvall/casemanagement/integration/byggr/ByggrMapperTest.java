package se.sundsvall.casemanagement.integration.byggr;

import arendeexport.Arende;
import arendeexport.ArendeIntressent;
import arendeexport.ArrayOfHandelse;
import arendeexport.Handelse;
import arendeexport.HandelseIntressent;
import arendeexport.IntressentAttention;
import arendeexport.SaveNewHandelse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.AbstractThrowableProblem;
import se.sundsvall.casemanagement.api.model.AddressDTO;
import se.sundsvall.casemanagement.api.model.AttachmentDTO;
import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.FacilityDTO;
import se.sundsvall.casemanagement.api.model.OrganizationDTO;
import se.sundsvall.casemanagement.api.model.PersonDTO;
import se.sundsvall.casemanagement.api.model.StakeholderDTO;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.api.model.enums.FacilityType;
import se.sundsvall.casemanagement.api.model.enums.StakeholderRole;
import se.sundsvall.casemanagement.api.model.enums.StakeholderType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeData;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.casemanagement.TestUtil.createByggRCaseDTO;
import static se.sundsvall.casemanagement.TestUtil.createHandelseIntressent;
import static se.sundsvall.casemanagement.TestUtil.createStakeholderDTO;
import static se.sundsvall.casemanagement.api.model.enums.AddressCategory.INVOICE_ADDRESS;
import static se.sundsvall.casemanagement.api.model.enums.AddressCategory.POSTAL_ADDRESS;
import static se.sundsvall.casemanagement.api.model.enums.FacilityType.BUSINESS_PREMISES;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_ATOM_KVITTENS;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_ELDSTAD;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_KOMPLETTERING_TILL_ADMIN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_MED_KRAV_PA_SVAR;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_SLUTBESKED;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSESLAG_UTSKICK_AV_REMISS;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_ATOMHANDELSE;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_BESLUT;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_HANDLING;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_REMISS;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSETYP_STATUS;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSEUTFALL_ATOM_KVITTENS_HL_BYTE;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_ANTECKNING;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_RIKTNING_IN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_RUBRIK_KOMPLETTERING_TILL_ADMIN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_HANDLING_STATUS_INKOMMEN;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMTYP_EPOST;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMTYP_HEMTELEFON;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_KOMTYP_MOBIL;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_STATUS_AVSLUTAT;
import static se.sundsvall.casemanagement.util.Constants.BYGGR_SYSTEM_HANDLAGGARE_SIGN;
import static se.sundsvall.casemanagement.util.Constants.HANDELSETYP_ANMALAN;

class ByggrMapperTest {

	@Test
	void setStakeholderFieldsOrg() {
		// Arrange
		final var org = new OrganizationDTO("someOrgName", "someOrgNr", "someSignature");
		final var intressent = new ArendeIntressent();
		// Act
		ByggrMapper.setStakeholderFields(org, null, intressent);
		// Assert
		assertThat(intressent.getNamn()).isEqualTo("someOrgName");
		assertThat(intressent.getPersOrgNr()).isEqualTo("someOrgNr");
		assertThat(intressent.isArForetag()).isTrue();
	}

	@Test
	void setStakeholderFieldsPerson() {

		// Arrange
		final var org = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		final var intressent = new ArendeIntressent();
		// Act
		ByggrMapper.setStakeholderFields(org, List.of("somePersonId"), intressent);

		// Assert
		assertThat(intressent.getFornamn()).isEqualTo("someFirstname");
		assertThat(intressent.getEfternamn()).isEqualTo("someLastName");
		assertThat(intressent.getPersOrgNr()).isEqualTo("somePersonalNumber");
		assertThat(intressent.isArForetag()).isFalse();
	}

	@Test
	void toSaveNewManuellHanteringHandelse() {

		// Act
		final var result = ByggrMapper.toSaveNewManuellHanteringHandelse("SomeDnr", "SomeNote");
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getMessage().getDnr()).isEqualTo("SomeDnr");
		assertThat(result.getMessage().getHandlaggarSign()).isEqualTo(BYGGR_SYSTEM_HANDLAGGARE_SIGN);
		assertThat(result.getMessage().getHandelse()).satisfies(handelse -> {
			assertThat(handelse.getAnteckning()).isEqualTo("SomeNote");
			assertThat(handelse.getRubrik()).isEqualTo(BYGGR_HANDELSE_RUBRIK_MANUELL_HANTERING);
			assertThat(handelse.getRiktning()).isEqualTo(BYGGR_HANDELSE_RIKTNING_IN);
			assertThat(handelse.getHandelsetyp()).isEqualTo(BYGGR_HANDELSETYP_STATUS);
			assertThat(handelse.getHandelseslag()).isEqualTo(BYGGR_HANDELSESLAG_MANUELL_HANTERING_KRAVS);
			assertThat(handelse.getStartDatum()).isCloseTo(now(), within(2, SECONDS));
		});

	}

	@Test
	void toHandelse() {
		final var dto = new ByggRCaseDTO();
		dto.setFacilities(List.of(new FacilityDTO()));
		final var caseType = CaseTypeData.builder()
			.withHandelseRubrik("someHandelseRubrik")
			.withHandelseSlag("SomeHandelseslag")
			.withHandelseTyp("SomeHandelseTyp")
			.build();
		final var result = ByggrMapper.toHandelse(dto, caseType);

		assertThat(result).isNotNull();
		assertThat(result.getStartDatum()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getRiktning()).isEqualTo(BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(result.getRubrik()).isEqualTo(caseType.getHandelseRubrik());
		assertThat(result.getHandelsetyp()).isEqualTo(caseType.getHandelseTyp());
		assertThat(result.getHandelseslag()).isEqualTo(caseType.getHandelseSlag());
	}

	@Test
	void toHandelseFireplaceFacility() {
		// Arrange
		final var dto = new ByggRCaseDTO();
		final var facility = new FacilityDTO();
		facility.setFacilityType(FacilityType.FIREPLACE.toString());
		dto.setFacilities(List.of(facility));
		final var caseType = CaseTypeData.builder()
			.withHandelseRubrik("someHandelseRubrik")
			.withHandelseSlag("SomeHandelseslag")
			.withHandelseTyp("SomeHandelseTyp")
			.build();
		// Act
		final var result = ByggrMapper.toHandelse(dto, caseType);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getStartDatum()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getRiktning()).isEqualTo(BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(result.getRubrik()).isEqualTo(BYGGR_HANDELSE_RUBRIK_ELDSTAD);
		assertThat(result.getHandelsetyp()).isEqualTo(caseType.getHandelseTyp());
		assertThat(result.getHandelseslag()).isEqualTo(BYGGR_HANDELSESLAG_ELDSTAD);
	}

	@Test
	void toHandelseSmokeChannelFacility() {
		// Arrange
		final var dto = new ByggRCaseDTO();
		final var facility = new FacilityDTO();
		facility.setFacilityType(FacilityType.FIREPLACE_SMOKECHANNEL.toString());
		dto.setFacilities(List.of(facility));
		final var caseType = CaseTypeData.builder()
			.withHandelseRubrik("someHandelseRubrik")
			.withHandelseSlag("SomeHandelseslag")
			.withHandelseTyp("SomeHandelseTyp")
			.build();
		// Act
		final var result = ByggrMapper.toHandelse(dto, caseType);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getStartDatum()).isCloseTo(now(), within(2, SECONDS));
		assertThat(result.getRiktning()).isEqualTo(BYGGR_HANDELSE_RIKTNING_IN);
		assertThat(result.getRubrik()).isEqualTo(BYGGR_HANDELSE_RUBRIK_ELDSTAD_ROKKANAL);
		assertThat(result.getHandelsetyp()).isEqualTo(caseType.getHandelseTyp());
		assertThat(result.getHandelseslag()).isEqualTo(BYGGR_HANDELSESLAG_ELDSTAD_ROKKANAL);
	}

	@Test
	void toArrayOfHandling() {

		// Arrange
		final var attachmentDTO = new AttachmentDTO();
		attachmentDTO.setFile("someFile");
		attachmentDTO.setName("someFileName");
		attachmentDTO.setMimeType("someContentType");
		attachmentDTO.setExtension("someExtension");
		attachmentDTO.setNote("someNote");
		attachmentDTO.setCategory("someCategory");
		final List<AttachmentDTO> attachmentDTOList = List.of(attachmentDTO);
		// Act
		final var result = ByggrMapper.toArrayOfHandling(attachmentDTOList);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getHandling()).hasSize(1);
		assertThat(result.getHandling().getFirst()).satisfies(
			handling -> {

				assertThat(handling.getAnteckning()).isEqualTo("someFileName");
				assertThat(handling.getStatus()).isEqualTo(BYGGR_HANDLING_STATUS_INKOMMEN);
				assertThat(handling.getTyp()).isEqualTo("someCategory");
				assertThat(handling.getDokument().getFil().getFilBuffer()).isEqualTo(Base64.getDecoder().decode(attachmentDTO.getFile().getBytes()));
				assertThat(handling.getDokument().getFil().getFilAndelse()).isEqualTo(attachmentDTO.getExtension().toLowerCase());
				assertThat(handling.getDokument().getBeskrivning()).isEqualTo(attachmentDTO.getNote());
			});

	}

	@Test
	void toHandelseHandling() {
		// Arrange
		final var attachmentDTO = new AttachmentDTO();
		attachmentDTO.setFile("someFile");
		attachmentDTO.setName("someFileName");
		attachmentDTO.setMimeType("someContentType");
		attachmentDTO.setExtension("someExtension");
		attachmentDTO.setNote("someNote");
		attachmentDTO.setCategory("someCategory");
		// Act
		final var result = ByggrMapper.toHandelseHandling(attachmentDTO);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getAnteckning()).isEqualTo("someFileName");
		assertThat(result.getStatus()).isEqualTo(BYGGR_HANDLING_STATUS_INKOMMEN);
		assertThat(result.getTyp()).isEqualTo("someCategory");
		assertThat(result.getDokument().getFil().getFilBuffer()).isEqualTo(Base64.getDecoder().decode(attachmentDTO.getFile().getBytes()));
		assertThat(result.getDokument().getFil().getFilAndelse()).isEqualTo(attachmentDTO.getExtension().toLowerCase());
		assertThat(result.getDokument().getBeskrivning()).isEqualTo(attachmentDTO.getNote());
	}

	@Test
	void toSaveNewArende() {
		// Arrange
		final var dto = new ByggRCaseDTO();
		dto.setAttachments(List.of());
		final var facility = new FacilityDTO();
		facility.setFacilityType(FacilityType.FIREPLACE_SMOKECHANNEL.toString());
		dto.setFacilities(List.of(facility));
		final var caseType = CaseTypeData.builder()
			.withHandelseRubrik("someHandelseRubrik")
			.withHandelseSlag("SomeHandelseslag")
			.withHandelseTyp("SomeHandelseTyp")
			.build();
		// Act
		final var result = ByggrMapper.toSaveNewArende(dto, caseType);
		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getMessage()).satisfies(
			saveNewArendeMessage -> {
				assertThat(saveNewArendeMessage.isAnkomststamplaHandlingar()).isTrue();
				assertThat(saveNewArendeMessage.getHandelse()).isNotNull();
				assertThat(saveNewArendeMessage.getHandlingar()).isNotNull();
				assertThat(saveNewArendeMessage.getHandlaggarSign()).isEqualTo(BYGGR_SYSTEM_HANDLAGGARE_SIGN);
			});
	}

	@Test
	void toSaveNewHandelseMessage() {
		// Arrange
		final var attachmentDTO = new AttachmentDTO();
		attachmentDTO.setFile("someFile");
		attachmentDTO.setName("someFileName");
		attachmentDTO.setMimeType("someContentType");
		attachmentDTO.setExtension("someExtension");
		attachmentDTO.setNote("someNote");
		attachmentDTO.setCategory("someCategory");
		final var attachmentDTOS = List.of(attachmentDTO);
		// Act
		final var result = ByggrMapper.toSaveNewHandelseMessage("someDnr", attachmentDTOS);
		// Assert
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("besvaradHandelseId");
		assertThat(result.getDnr()).isEqualTo("someDnr");
		assertThat(result.getHandlaggarSign()).isEqualTo(BYGGR_SYSTEM_HANDLAGGARE_SIGN);
		assertThat(result.getHandelse()).satisfies(handelse -> {
			assertThat(handelse.getAnteckning()).isEqualTo(BYGGR_HANDELSE_ANTECKNING);
			assertThat(handelse.getRubrik()).isEqualTo(BYGGR_HANDELSE_RUBRIK_KOMPLETTERING_TILL_ADMIN);
			assertThat(handelse.getRiktning()).isEqualTo(BYGGR_HANDELSE_RIKTNING_IN);
			assertThat(handelse.getHandelsetyp()).isEqualTo(BYGGR_HANDELSETYP_HANDLING);
			assertThat(handelse.getHandelseslag()).isEqualTo(BYGGR_HANDELSESLAG_KOMPLETTERING_TILL_ADMIN);
			assertThat(handelse.getStartDatum()).isCloseTo(now(), within(2, SECONDS));
		});

	}

	@Test
	void getInvoiceMarking() {

		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		personDto.setAddresses(List.of(AddressDTO.builder()
			.withAddressCategories(List.of(INVOICE_ADDRESS))
			.withInvoiceMarking("someInvoiceMarking")
			.build()));
		final var dto = new ByggRCaseDTO();
		dto.setStakeholders(List.of(personDto));
		// Act
		final var result = ByggrMapper.getInvoiceMarking(dto);
		// Assert
		assertThat(result).isEqualTo("someInvoiceMarking");
	}

	@Test
	void getInvoiceMarkingBlankInvoiceMarking() {

		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		personDto.setAddresses(List.of(AddressDTO.builder()
			.withAddressCategories(List.of(INVOICE_ADDRESS))
			.withInvoiceMarking(" ")
			.build()));
		final var dto = new ByggRCaseDTO();
		dto.setStakeholders(List.of(personDto));
		// Act
		final var result = ByggrMapper.getInvoiceMarking(dto);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void getInvoiceMarkingNullInvoiceMarking() {

		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		personDto.setAddresses(List.of(AddressDTO.builder()
			.withAddressCategories(List.of(INVOICE_ADDRESS))
			.withInvoiceMarking(null)
			.build()));
		final var dto = new ByggRCaseDTO();
		dto.setStakeholders(List.of(personDto));
		// Act
		final var result = ByggrMapper.getInvoiceMarking(dto);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void getInvoiceMarkingNoAdress() {

		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		final var dto = new ByggRCaseDTO();
		dto.setStakeholders(List.of(personDto));
		// Act
		final var result = ByggrMapper.getInvoiceMarking(dto);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void getInvoiceMarkingNoValidAdressCategory() {

		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		personDto.setAddresses(List.of(AddressDTO.builder()
			.withAddressCategories(List.of(POSTAL_ADDRESS))
			.withInvoiceMarking(null)
			.build()));
		final var dto = new ByggRCaseDTO();
		dto.setStakeholders(List.of(personDto));
		// Act
		final var result = ByggrMapper.getInvoiceMarking(dto);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void getArendeKlass() {

		final var facility = new FacilityDTO();
		facility.setFacilityType(BUSINESS_PREMISES.toString());
		// Arrange
		final var facilities = List.of(facility);
		// Act
		final var result = ByggrMapper.getArendeKlass(facilities);
		// Assert
		assertThat(result).isEqualTo(BUSINESS_PREMISES.getValue());
	}

	@Test
	void getMainOrOnlyArendeSlag() {
		// Arrange
		final var facility = new FacilityDTO();
		facility.setFacilityType(BUSINESS_PREMISES.toString());
		final var facilities = List.of(facility);
		// Act
		final var result = ByggrMapper.getMainOrOnlyArendeslag(facilities);
		// Assert
		assertThat(result).isEqualTo(BUSINESS_PREMISES.getValue());
	}

	@Test
	void setPostalAddressFields() {
		// Arrange
		final var addressDTO = new AddressDTO();
		addressDTO.setStreet("someStreet");
		addressDTO.setHouseNumber("someHouseNumber");
		addressDTO.setPostalCode("somePostalCode");
		addressDTO.setCity("someCity");
		addressDTO.setCountry("someCountry");
		addressDTO.setCareOf("someCareOf");

		final var intressent = new ArendeIntressent();
		// Act
		ByggrMapper.setPostalAddressFields(intressent, addressDTO);
		// Assert
		assertThat(intressent.getAdress()).isEqualTo("someStreet someHouseNumber");
		assertThat(intressent.getOrt()).isEqualTo("someCity");
		assertThat(intressent.getPostNr()).isEqualTo("somePostalCode");
		assertThat(intressent.getLand()).isEqualTo("someCountry");
		assertThat(intressent.getCoAdress()).isEqualTo("someCareOf");
	}

	@Test
	void setPostalAddressFieldsWithoutHouseNumber() {
		// Arrange
		final var addressDTO = new AddressDTO();
		addressDTO.setStreet("someStreet");
		addressDTO.setPostalCode("somePostalCode");
		addressDTO.setCity("someCity");
		addressDTO.setCountry("someCountry");
		addressDTO.setCareOf("someCareOf");

		final var intressent = new ArendeIntressent();
		// Act
		ByggrMapper.setPostalAddressFields(intressent, addressDTO);
		// Assert
		assertThat(intressent.getAdress()).isEqualTo("someStreet");
		assertThat(intressent.getOrt()).isEqualTo("someCity");
		assertThat(intressent.getPostNr()).isEqualTo("somePostalCode");
		assertThat(intressent.getLand()).isEqualTo("someCountry");
		assertThat(intressent.getCoAdress()).isEqualTo("someCareOf");
	}

	@Test
	void setOrganizationFields() {

		// Arrange
		final var org = new OrganizationDTO("someOrgName", "someOrgNr", "someSignature");
		final var intressent = new ArendeIntressent();
		// Act
		ByggrMapper.setOrganizationFields(intressent, org);
		// Assert
		assertThat(intressent.getNamn()).isEqualTo("someOrgName");
		assertThat(intressent.getPersOrgNr()).isEqualTo("someOrgNr");
		assertThat(intressent.isArForetag()).isTrue();
	}

	@Test
	void setPersonFields() {
		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		final var intressent = new ArendeIntressent();
		// Act
		ByggrMapper.setPersonFields(intressent, personDto);
		// Assert
		assertThat(intressent.getFornamn()).isEqualTo("someFirstname");
		assertThat(intressent.getEfternamn()).isEqualTo("someLastName");
		assertThat(intressent.getPersOrgNr()).isEqualTo("somePersonalNumber");
		assertThat(intressent.isArForetag()).isFalse();
	}

	@Test
	void toArrayOfRoles() {
		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		personDto.setRoles(List.of("PAYMENT_PERSON"));
		// Act
		final var result = ByggrMapper.toArrayOfRoles(personDto);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getRoll()).hasSize(1);
		assertThat(result.getRoll().getFirst()).isEqualTo("BETA");
	}

	@Test
	void toFakturaadress() {
		// Arrange
		final var addressDTO = new AddressDTO();
		addressDTO.setStreet("someStreet");
		addressDTO.setHouseNumber("someHouseNumber");
		addressDTO.setPostalCode("somePostalCode");
		addressDTO.setCity("someCity");
		addressDTO.setAttention("someAttention");
		addressDTO.setCountry("someCountry");
		addressDTO.setCareOf("someCareOf");
		// Act
		final var result = ByggrMapper.toFakturaadress(addressDTO);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getAdress()).isEqualTo("someStreet someHouseNumber");
		assertThat(result.getOrt()).isEqualTo("someCity");
		assertThat(result.getPostNr()).isEqualTo("somePostalCode");
		assertThat(result.getLand()).isEqualTo("someCountry");
		assertThat(result.getAttention()).isEqualTo("someAttention");

	}

	@Test
	void toFakturaadress_withoutHouseNumber() {
		// Arrange
		final var addressDTO = new AddressDTO();
		addressDTO.setStreet("someStreet");
		addressDTO.setPostalCode("somePostalCode");
		addressDTO.setCity("someCity");
		addressDTO.setAttention("someAttention");
		addressDTO.setCountry("someCountry");
		addressDTO.setCareOf("someCareOf");
		// Act
		final var result = ByggrMapper.toFakturaadress(addressDTO);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getAdress()).isEqualTo("someStreet");
		assertThat(result.getOrt()).isEqualTo("someCity");
		assertThat(result.getPostNr()).isEqualTo("somePostalCode");
		assertThat(result.getLand()).isEqualTo("someCountry");
		assertThat(result.getAttention()).isEqualTo("someAttention");

	}

	@Test
	void toByggrContactInfo() {
		// Arrange
		final var personDto = new PersonDTO();
		personDto.setCellphoneNumber("someCellphoneNumber");
		personDto.setPhoneNumber("somePhoneNumber");
		personDto.setEmailAddress("someEmailAddress");
		final var intressentAttention = new IntressentAttention();
		// Act
		final var result = ByggrMapper.toByggrContactInfo(personDto, intressentAttention);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getIntressentKommunikation()).hasSize(3);
		assertThat(result.getIntressentKommunikation()).satisfies(
			kommunikations -> kommunikations.forEach(
				kommunikation -> {
					assertThat(kommunikation.getAttention()).isEqualTo(intressentAttention);
					assertThat(kommunikation.getKomtyp()).isIn(BYGGR_KOMTYP_MOBIL, BYGGR_KOMTYP_HEMTELEFON, BYGGR_KOMTYP_EPOST);
					assertThat(kommunikation.isArAktiv()).isTrue();
					assertThat(kommunikation.getBeskrivning()).isIn("someCellphoneNumber", "somePhoneNumber", "someEmailAddress");
				}));

	}

	@Test
	void populateStakeholderListWithPropertyOwnerPersons() {

		// Arrange

		final var persondto = PersonDTO.builder()
			.withPersonId("somePersonId")
			.withPersonalNumber("somePersonalNumber")
			.withFirstName("someFirstName")
			.withLastName("someLastName")
			.withRoles(List.of("PROPERTY_OWNER"))
			.build();

		final var personDto2 = PersonDTO.builder()
			.withPersonId("somePersonId2")
			.withPersonalNumber("somePersonalNumber2")
			.withFirstName("someFirstName2")
			.withLastName("someLastName2")
			.withRoles(List.of("PROPERTY_OWNER"))
			.build();

		final List<StakeholderDTO> propertyOwnerList = List.of(persondto, personDto2);

		final List<PersonDTO> personList = List.of(persondto);

		final var stakeholderDTOList = new ArrayList<StakeholderDTO>();
		stakeholderDTOList.add(persondto);

		ByggrMapper.populateStakeholderListWithPropertyOwnerPersons(personList, stakeholderDTOList, propertyOwnerList);

		assertThat(stakeholderDTOList).isNotNull().hasSize(2);
		assertThat(stakeholderDTOList.getFirst()).isEqualTo(persondto);
		assertThat(stakeholderDTOList.getLast()).isEqualTo(personDto2);

	}

	@Test
	void populateStakeholderListWithPropertyOwnerOrganizations() {

		// Arrange
		final var organizationDTO = new OrganizationDTO("someOrgName", "someOrgNr", "someSignature");
		organizationDTO.setRoles(List.of("PROPERTY_OWNER"));
		final var organizationDTO2 = new OrganizationDTO("someOrgName2", "someOrgNr2", "someSignature2");
		organizationDTO.setRoles(List.of("PROPERTY_OWNER"));

		final List<StakeholderDTO> propertyOwnerList = List.of(organizationDTO, organizationDTO2);

		final List<OrganizationDTO> organizationList = List.of(organizationDTO);

		final var stakeholderDTOList = new ArrayList<StakeholderDTO>();
		stakeholderDTOList.add(organizationDTO);

		ByggrMapper.populateStakeholderListWithPropertyOwnerOrganizations(organizationList, stakeholderDTOList, propertyOwnerList);

		assertThat(stakeholderDTOList).isNotNull().hasSize(2);
		assertThat(stakeholderDTOList.getFirst()).isEqualTo(organizationDTO);
		assertThat(stakeholderDTOList.getLast()).isEqualTo(organizationDTO2);
	}

	@Test
	void getArendeBeskrivning() {

		// Arrange
		final var dto = new ByggRCaseDTO();
		final var facility = new FacilityDTO();
		facility.setFacilityType(FacilityType.FIREPLACE.toString());
		final var facility2 = new FacilityDTO();
		facility2.setFacilityType(FacilityType.WORKSHOP_BUILDING.toString());
		final var facility3 = new FacilityDTO();
		facility3.setFacilityType(FacilityType.WAREHOUSE.toString());
		dto.setFacilities(List.of(facility, facility2, facility3));
		dto.setCaseTitleAddition("some case title addition");
		// Act
		final var result = ByggrMapper.getArendeBeskrivning(dto, "someCaseDescription");
		// Assert
		assertThat(result).isEqualTo("someCaseDescription installation av eldstad, verkstadsbyggnad & lagerbyggnad samt some case title addition");
	}

	@Test
	void getArendeBeskrivningNoDescription() {

		// Arrange
		final var dto = new ByggRCaseDTO();
		dto.setFacilities(List.of());
		dto.setCaseTitleAddition("some case title addition");
		// Act
		final var result = ByggrMapper.getArendeBeskrivning(dto, "someCaseDescription");
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void filterPersonId() {
		// Arrange
		final var personDto = new PersonDTO("someFirstname", "someLastName", "somePersonId", "somePersonalNumber");
		final var personDto2 = new PersonDTO("someFirstname2", "someLastName2", "somePersonId2", "somePersonalNumber2");
		final var personDto3 = new PersonDTO();
		final var organizationDTO = new OrganizationDTO();
		final List<StakeholderDTO> stakeholderDTOList = List.of(personDto, personDto2, personDto3, organizationDTO);
		// Act
		final var result = ByggrMapper.filterPersonId(stakeholderDTOList);
		// Assert
		assertThat(result).isNotNull().hasSize(2).containsExactlyInAnyOrder("somePersonId", "somePersonId2");
	}

	@Test
	void toAdressCategoryPostal() {
		// Arrange
		final var organizationDTO = new OrganizationDTO();
		final var addressDTO = new AddressDTO();
		addressDTO.setAttention("someAttention");

		final var arendeIntressent = new ArendeIntressent();
		// Act
		ByggrMapper.toAdressCategory(organizationDTO, addressDTO, AddressCategory.POSTAL_ADDRESS, arendeIntressent);
		// Assert
		assertThat(arendeIntressent.getAttention()).isNotNull();
		assertThat(arendeIntressent.getAttention().getAttention()).isNotNull().isEqualTo("someAttention");
	}

	@Test
	void toAdressCategoryInvoice() {
		// Arrange
		final var organizationDTO = new OrganizationDTO();
		final var addressDTO = new AddressDTO();
		final var arendeIntressent = new ArendeIntressent();
		// Act
		ByggrMapper.toAdressCategory(organizationDTO, addressDTO, INVOICE_ADDRESS, arendeIntressent);
		// Assert
		assertThat(arendeIntressent.getFakturaAdress()).isNotNull();
	}

	@Test
	void toAdressCategoryInvoiceAsPerson() {
		// Arrange
		final var personDTO = new PersonDTO();
		final var addressDTO = new AddressDTO();
		final var arendeIntressent = new ArendeIntressent();
		// Act
		assertThatThrownBy(() -> ByggrMapper.toAdressCategory(personDTO, addressDTO, INVOICE_ADDRESS, arendeIntressent))
			.isInstanceOf(AbstractThrowableProblem.class)
			.hasMessageContaining("Bad Request: Stakeholders of type PERSON should not have an address with the addressCategory INVOICE_ADDRESS");
	}

	@Test
	void toAdressCategories() {
		// Arrange
		final var organizationDTO = new OrganizationDTO();
		final var addressDTO = new AddressDTO();
		addressDTO.setAttention("someAttention");
		addressDTO.setAddressCategories(List.of(INVOICE_ADDRESS, POSTAL_ADDRESS));
		final var arendeIntressent = new ArendeIntressent();
		// Act
		ByggrMapper.toAdressCategories(organizationDTO, addressDTO, arendeIntressent);
		// Assert
		assertThat(arendeIntressent.getFakturaAdress()).isNotNull();
		assertThat(arendeIntressent.getAttention()).isNotNull();
		assertThat(arendeIntressent.getAttention().getAttention()).isNotNull().isEqualTo("someAttention");
	}

	@Test
	void toAdressDTos() {
		// Arrange
		final var organizationDTO = OrganizationDTO.builder().withAddresses(
			List.of(AddressDTO.builder()
				.withAttention("someAttention")
				.withAddressCategories(List.of(INVOICE_ADDRESS, POSTAL_ADDRESS))
				.build()))
			.build();
		final var arendeIntressent = new ArendeIntressent();
		// Act
		ByggrMapper.toAdressDTos(organizationDTO, arendeIntressent);
		// Assert
		assertThat(arendeIntressent.getFakturaAdress()).isNotNull();
		assertThat(arendeIntressent.getAttention()).isNotNull();
		assertThat(arendeIntressent.getAttention().getAttention()).isNotNull().isEqualTo("someAttention");
	}

	@Test
	void toByggrStatus() {

		// Arrange
		final var arende = new Arende();
		arende.setDnr("someDnr");
		arende.setArendeId(123456);
		arende.setHandelseLista(new ArrayOfHandelse().withHandelse(new Handelse()
			.withHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR)
			.withHandelsetyp(BYGGR_HANDELSETYP_BESLUT)
			.withStartDatum(LocalDateTime.now())

		));
		final var caseMappings = List.of(
			CaseMapping.builder()
				.withCaseId("someCaseId")
				.withCaseType("someCaseType")
				.withServiceName("someServiceName")
				.build());

		// Act
		final var result = ByggrMapper.toByggrStatus(arende, "someCaseId", caseMappings);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("timestamp", "namespace");
		assertThat(result.getStatus()).isEqualTo("KOMPBYGG");
		assertThat(result.getTimestamp()).isCloseTo(LocalDateTime.now(), within(1, SECONDS));
	}

	@Test
	void toByggrStatusCaseClosedAndNoHandelseList() {

		// Arrange
		final var arende = new Arende();
		arende.setDnr("someDnr");
		arende.setArendeId(123456);
		arende.setAnkomstDatum(LocalDate.now());
		final var caseMappings = List.of(
			CaseMapping.builder()
				.withCaseId("someCaseId")
				.withCaseType("someCaseType")
				.withServiceName("someServiceName")
				.build());

		// Act && Assert
		var caseStatusDTO = ByggrMapper.toByggrStatus(arende, "someCaseId", caseMappings);
		assertThat(caseStatusDTO).isNotNull().hasNoNullFieldsOrPropertiesExcept("timestamp", "namespace");
		assertThat(caseStatusDTO.getStatus()).isEqualTo("Okänt");
		assertThat(caseStatusDTO.getTimestamp()).isEqualTo(LocalDate.now().atStartOfDay());
	}

	@Test
	void toByggrStatusCaseClosed() {

		// Arrange
		final var arende = new Arende();
		arende.setDnr("someDnr");
		arende.setArendeId(123456);
		arende.setStatus(BYGGR_STATUS_AVSLUTAT);
		arende.setAnkomstDatum(LocalDate.now());
		arende.setHandelseLista(new ArrayOfHandelse().withHandelse(new Handelse()
			.withHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR)
			.withHandelsetyp(BYGGR_HANDELSETYP_BESLUT)

		));
		final var caseMappings = List.of(
			CaseMapping.builder()
				.withCaseId("someCaseId")
				.withCaseType("someCaseType")
				.withServiceName("someServiceName")
				.build());

		// Act
		final var result = ByggrMapper.toByggrStatus(arende, "someCaseId", caseMappings);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("timestamp", "namespace");
		assertThat(result.getStatus()).isEqualTo("Avslutat");
		assertThat(result.getTimestamp()).isEqualTo(LocalDate.now().atStartOfDay());
	}

	@Test
	void buildCaseStatusDTO() {

		// Arrange
		final var arende = new Arende();
		arende.setDnr("someDnr");
		arende.setArendeId(12345);
		arende.setHandelseLista(new ArrayOfHandelse().withHandelse(new Handelse()
			.withHandelseslag(BYGGR_HANDELSESLAG_KOMPLETTERANDE_BYGGLOVHANDLINGAR)
			.withHandelsetyp(BYGGR_HANDELSETYP_BESLUT)

		));
		final var caseMappings = List.of(
			CaseMapping.builder()
				.withCaseId("someCaseId")
				.withCaseType("someCaseType")
				.withServiceName("someServiceName")
				.build());
		// Act
		final var result = ByggrMapper.buildCaseStatusDTO(arende, "someExternalCaseId", caseMappings);
		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("timestamp", "status", "namespace");
		assertThat(result.getCaseId()).isEqualTo("someDnr");
		assertThat(result.getErrandNumber()).isEqualTo(arende.getDnr());
		assertThat(result.getNamespace()).isNull();
		assertThat(result.getCaseType()).isEqualTo("someCaseType");
		assertThat(result.getServiceName()).isEqualTo("someServiceName");
		assertThat(result.getExternalCaseId()).isEqualTo("someExternalCaseId");
		assertThat(result.getStatus()).isEqualTo("Okänt");

	}

	@Test
	void testGetHandelseStatusInskickat() {
		// Arrange
		final String handelseTyp = HANDELSETYP_ANMALAN;

		// Act
		final String result = ByggrMapper.getHandelseStatus(handelseTyp, BYGGR_HANDELSESLAG_MED_KRAV_PA_SVAR, null);

		// Assert
		assertThat(result).isEqualTo(handelseTyp);
	}

	@Test
	void testGetHandelseStatusUnderBehandling() {
		// Arrange
		final String handelseslag = BYGGR_HANDELSESLAG_SLUTBESKED;
		// Act
		final String result = ByggrMapper.getHandelseStatus(BYGGR_HANDELSETYP_BESLUT, handelseslag, null);

		// Assert
		assertThat(result).isEqualTo(handelseslag);
	}

	@Test
	void testGetHandelseStatusUtskick() {
		// Arrange
		final String handelseslag = BYGGR_HANDELSESLAG_UTSKICK_AV_REMISS;

		// Act
		final String result = ByggrMapper.getHandelseStatus(BYGGR_HANDELSETYP_REMISS, handelseslag, null);

		// Assert
		assertThat(result).isEqualTo(handelseslag);
	}

	@Test
	void testGetHandelseStatusKompletterad() {
		// Arrange
		final String handelseUtfall = BYGGR_HANDELSEUTFALL_ATOM_KVITTENS_HL_BYTE;

		// Act
		final String result = ByggrMapper.getHandelseStatus(BYGGR_HANDELSETYP_ATOMHANDELSE, BYGGR_HANDELSESLAG_ATOM_KVITTENS, handelseUtfall);

		// Assert
		assertThat(result).isEqualTo(handelseUtfall);
	}

	@Test
	void testGetHandelseStatusNull() {
		// Arrange
		final String handelseTyp = "unknown";
		final String handelseSlag = "unknown";
		final String handelseUtfall = "unknown";

		// Act
		final String result = ByggrMapper.getHandelseStatus(handelseTyp, handelseSlag, handelseUtfall);

		// Assert
		assertThat(result).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"GRASV", "UNDERE", "BIL"
	})
	void createNeighborhoodNotificationArrayOfHandling(String category) {
		var byggRCase = createByggRCaseDTO(CaseType.NEIGHBORHOOD_NOTIFICATION, AttachmentCategory.ATTACHMENT);
		for (var attachment : byggRCase.getAttachments()) {
			attachment.setCategory(category);
		}
		var attachment = byggRCase.getAttachments().getFirst();

		var result = ByggrMapper.createNeighborhoodNotificationArrayOfHandling(byggRCase);

		assertThat(result.getHandling()).hasSize(1);
		assertThat(result.getHandling().getFirst().getTyp()).isEqualTo(category);
		assertThat(result.getHandling().getFirst().getDokument().getFil().getFilAndelse()).isEqualTo(attachment.getExtension());
		assertThat(result.getHandling().getFirst().getDokument().getFil().getFilBuffer()).isEqualTo(Base64.getDecoder().decode(attachment.getFile().getBytes()));
	}

	@ParameterizedTest
	@MethodSource("addAdditionalDocumentsHandelseArgumentProvider")
	void createAddAdditionalDocumentsHandelse(String handelseslag, String rubrik) {
		var handelseIntressent = new HandelseIntressent();
		var errandInformation = "errandInformation";

		var result = ByggrMapper.createAddAdditionalDocumentsHandelse(errandInformation, handelseIntressent, handelseslag);

		assertThat(result).isNotNull().satisfies(handelse -> {
			assertThat(handelse.getRiktning()).isEqualTo("In");
			assertThat(handelse.getRubrik()).isEqualTo(rubrik);
			assertThat(handelse.getStartDatum()).isCloseTo(LocalDateTime.now(), within(10, SECONDS));
			assertThat(handelse.getAnteckning()).isEqualTo(errandInformation);
			assertThat(handelse.getHandelsetyp()).isEqualTo("HANDLING");
			assertThat(handelse.getHandelseslag()).isEqualTo(handelseslag);
			assertThat(handelse.getIntressentLista().getIntressent()).isEqualTo(List.of(handelseIntressent));
		});
	}

	private static Stream<Arguments> addAdditionalDocumentsHandelseArgumentProvider() {
		return Stream.of(
			Arguments.of("GRASV", "Kompletterande handlingar"),
			Arguments.of("KOMPBYGG", "Kompletterande bygglovshandlingar"),
			Arguments.of("KOMPTEK", "Kompletterande tekniska handlingar"));
	}

	@Test
	void createAddCertifiedInspectorHandelseIntressent1() {
		var stakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT.name()));
		var extraParameters = Map.of("certificateAuthType", "certificateAuthType", "certificateNumber", "certificateNumber",
			"certificateIssuer", "certificateIssuer", "certificateValidDate", "2020-01-01");

		var result = ByggrMapper.createAddCertifiedInspectorHandelseIntressent(stakeholder, "stakeholderId", extraParameters);

		assertThat(result.getPersOrgNr()).isEqualTo("stakeholderId");
		assertThat(result.getAdress()).isEqualTo(stakeholder.getAddresses().getFirst().getStreet());
		assertThat(result.getPostNr()).isEqualTo(stakeholder.getAddresses().getFirst().getPostalCode());
		assertThat(result.getOrt()).isEqualTo(stakeholder.getAddresses().getFirst().getCity());
		assertThat(result.getIntressentKommunikationLista()).usingRecursiveComparison().isEqualTo(ByggrMapper.createArrayOfIntressentKommunikation(stakeholder));
		assertThat(result.getAktorbehorighetLista()).usingRecursiveComparison().isEqualTo(ByggrMapper.createAddCertifiedInspectorArrayOfAktorbehorighet(extraParameters));
		assertThat(result.isArForetag()).isFalse();
		assertThat(result.getRollLista().getRoll()).containsExactly("KOA");
		assertThat(result.getNamn()).isNull();
		assertThat(result.getFornamn()).isNotNull();
		assertThat(result.getEfternamn()).isNotNull();
	}

	@Test
	void createAddCertifiedInspectorHandelseIntressent2() {
		var stakeholder = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.APPLICANT.name()));
		var extraParameters = Map.of("certificateAuthType", "certificateAuthType", "certificateNumber", "certificateNumber",
			"certificateIssuer", "certificateIssuer", "certificateValidDate", "2020-01-01");

		var result = ByggrMapper.createAddCertifiedInspectorHandelseIntressent(stakeholder, "stakeholderId", extraParameters);

		assertThat(result.getPersOrgNr()).isEqualTo("stakeholderId");
		assertThat(result.getAdress()).isEqualTo(stakeholder.getAddresses().getFirst().getStreet());
		assertThat(result.getPostNr()).isEqualTo(stakeholder.getAddresses().getFirst().getPostalCode());
		assertThat(result.getOrt()).isEqualTo(stakeholder.getAddresses().getFirst().getCity());
		assertThat(result.getIntressentKommunikationLista()).usingRecursiveComparison().isEqualTo(ByggrMapper.createArrayOfIntressentKommunikation(stakeholder));
		assertThat(result.getAktorbehorighetLista()).usingRecursiveComparison().isEqualTo(ByggrMapper.createAddCertifiedInspectorArrayOfAktorbehorighet(extraParameters));
		assertThat(result.isArForetag()).isTrue();
		assertThat(result.getRollLista().getRoll()).containsExactly("KOA");
		assertThat(result.getNamn()).isNotNull();
		assertThat(result.getFornamn()).isNull();
		assertThat(result.getEfternamn()).isNull();
	}

	@Test
	void createAddCertifiedInspectorHandelse() {
		var errandInformation = "errandInformation";
		var handelseIntressent = createHandelseIntressent();

		var result = ByggrMapper.createAddCertifiedInspectorHandelse(errandInformation, handelseIntressent);

		assertThat(result.getRiktning()).isEqualTo("In");
		assertThat(result.getRubrik()).isEqualTo("Anmälan KA");
		assertThat(result.getStartDatum()).isCloseTo(LocalDateTime.now(), within(10, SECONDS));
		assertThat(result.getAnteckning()).isEqualTo(errandInformation);
		assertThat(result.getHandelsetyp()).isEqualTo("HANDLING");
		assertThat(result.getHandelseslag()).isEqualTo("KOMPL");
		assertThat(result.getIntressentLista().getIntressent()).isEqualTo(List.of(handelseIntressent));
	}

	@Test
	void createAlertCaseManagerEvent() {
		var dnr = "dnr";

		var result = ByggrMapper.createAlertCaseManagerEvent(dnr);

		assertThat(result).isNotNull().isInstanceOf(SaveNewHandelse.class).satisfies(saveNewHandelse -> {
			assertThat(saveNewHandelse.getMessage()).satisfies(message -> {
				assertThat(message.getDnr()).isEqualTo(dnr);
				assertThat(message.getHandlaggarSign()).isEqualTo("SYSTEM");
				assertThat(message.isAnkomststamplaHandlingar()).isFalse();
				assertThat(message.isAutoGenereraBeslutNr()).isFalse();

				assertThat(message.getHandelse()).satisfies(handelse -> {
					assertThat(handelse.getRiktning()).isEqualTo("In");
					assertThat(handelse.getRubrik()).isEqualTo("Manuell hantering krävs");
					assertThat(handelse.getStartDatum()).isCloseTo(LocalDateTime.now(), within(2, SECONDS));
					assertThat(handelse.getHandelseslag()).isEqualTo("MANHANT");
					assertThat(handelse.getHandelsetyp()).isEqualTo("STATUS");
					assertThat(handelse.isSekretess()).isFalse();
					assertThat(handelse.isMakulerad()).isFalse();
					assertThat(handelse.isArbetsmaterial()).isFalse();
				});
			});
		});
	}
}
