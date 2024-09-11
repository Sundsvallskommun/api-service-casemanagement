package se.sundsvall.casemanagement.integration.byggr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.createByggRCaseDTO;

import java.sql.SQLException;

import javax.sql.rowset.serial.SerialClob;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casemanagement.api.model.ByggRCaseDTO;
import se.sundsvall.casemanagement.api.model.enums.AttachmentCategory;
import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.configuration.RetryProperties;
import se.sundsvall.casemanagement.integration.db.CaseRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseEntity;
import se.sundsvall.casemanagement.service.event.IncomingByggrCase;

import arendeexport.SaveNewArendeResponse2;
import dev.failsafe.RetryPolicy;

@ExtendWith(MockitoExtension.class)
class ByggrProcessorTest {

	private static final String MUNICIPALITY_ID = "2281";

	@InjectMocks
	ByggrProcessor byggrProcessor;

	@Spy
	RetryProperties properties;

	@Mock
	private ByggrService service;

	@Mock
	private RetryPolicy<SaveNewArendeResponse2> retryPolicy;

	@Spy
	private CaseRepository caseRepository;

	@Test
	void testHandleIncomingErrand() throws SQLException, JsonProcessingException {
		final var event = new IncomingByggrCase(ByggrProcessorTest.class, createByggRCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION), MUNICIPALITY_ID);

		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final var jsonString = objectMapper.writeValueAsString(event.getPayload());

		when(caseRepository.findByIdAndMunicipalityId(any(String.class), eq(MUNICIPALITY_ID)))
			.thenReturn(java.util.Optional.of(CaseEntity.builder().withId("id").withDto(new SerialClob(jsonString.toCharArray())).build()));

		byggrProcessor.handleIncomingErrand(event);

		verify(caseRepository, times(1)).findByIdAndMunicipalityId(any(String.class), eq(MUNICIPALITY_ID));
		verifyNoMoreInteractions(caseRepository);
		verify(service, times(1)).saveNewCase(any(ByggRCaseDTO.class), eq(MUNICIPALITY_ID));

		assertThat(caseRepository.findAll()).isEmpty();
	}

	@Test
	void testHandleIncomingErrand_NoErrandFound() throws SQLException, JsonProcessingException {
		final var event = new IncomingByggrCase(ByggrProcessorTest.class, new ByggRCaseDTO(), MUNICIPALITY_ID);

		byggrProcessor.handleIncomingErrand(event);

		verify(caseRepository, times(1)).findByIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID));
		verifyNoMoreInteractions(caseRepository);
		verifyNoInteractions(service);

	}

	@Test
	void testHandleIncomingErrand_maximumFound() throws SQLException, JsonProcessingException {
		final var event = new IncomingByggrCase(ByggrProcessorTest.class, createByggRCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.BUILDING_PERMIT_APPLICATION), MUNICIPALITY_ID);

		final var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
		final String jsonString = objectMapper.writeValueAsString(event.getPayload());

		when(caseRepository.findByIdAndMunicipalityId(any(String.class), eq(MUNICIPALITY_ID)))
			.thenReturn(java.util.Optional.of(CaseEntity.builder().withId("id").withDto(new SerialClob(jsonString.toCharArray())).build()));

		when(service.saveNewCase(any(ByggRCaseDTO.class), eq(MUNICIPALITY_ID))).thenThrow(new RuntimeException("test"));

		byggrProcessor.handleIncomingErrand(event);

		verify(caseRepository, times(1)).findByIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID));
		verify(caseRepository, times(1)).save(any());
		verifyNoMoreInteractions(caseRepository);
		verify(service, times(3)).saveNewCase(any(ByggRCaseDTO.class), eq(MUNICIPALITY_ID));

	}

}
