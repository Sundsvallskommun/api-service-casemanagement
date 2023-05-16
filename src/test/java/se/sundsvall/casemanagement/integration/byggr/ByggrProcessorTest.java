package se.sundsvall.casemanagement.integration.byggr;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casemanagement.TestUtil.createPlanningPermissionCaseDTO;

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

import se.sundsvall.casemanagement.api.model.PlanningPermissionCaseDTO;
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
        var event = new IncomingByggrCase(ByggrProcessorTest.class, createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS));


        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(event.getPayload());


        when(caseRepository.findById(any(String.class)))
            .thenReturn(java.util.Optional.of(CaseEntity.builder().withId("id").withDto(new SerialClob(jsonString.toCharArray())).build()));

        byggrProcessor.handleIncomingErrand(event);

        verify(caseRepository, times(1)).findById(any());
        verify(caseRepository, times(1)).deleteById(any());
        verifyNoMoreInteractions(caseRepository);
        verify(service, times(1)).postCase(any(PlanningPermissionCaseDTO.class));

    }

    @Test
    void testHandleIncomingErrand_NoErrandFound() throws SQLException, JsonProcessingException {
        var event = new IncomingByggrCase(ByggrProcessorTest.class, new PlanningPermissionCaseDTO());

        byggrProcessor.handleIncomingErrand(event);

        verify(caseRepository, times(1)).findById(any());
        verifyNoMoreInteractions(caseRepository);
        verifyNoInteractions(service);

    }


    @Test
    void testHandleIncomingErrand_maximumFound() throws SQLException, JsonProcessingException {
        var event = new IncomingByggrCase(ByggrProcessorTest.class, createPlanningPermissionCaseDTO(CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV, AttachmentCategory.ANS));


        var objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        String jsonString = objectMapper.writeValueAsString(event.getPayload());


        when(caseRepository.findById(any(String.class)))
            .thenReturn(java.util.Optional.of(CaseEntity.builder().withId("id").withDto(new SerialClob(jsonString.toCharArray())).build()));

        when(service.postCase(any(PlanningPermissionCaseDTO.class))).thenThrow(new RuntimeException("test"));

        byggrProcessor.handleIncomingErrand(event);

        verify(caseRepository, times(1)).findById(any());
        verify(caseRepository, times(1)).save(any());
        verifyNoMoreInteractions(caseRepository);
        verify(service, times(3)).postCase(any(PlanningPermissionCaseDTO.class));

    }

}