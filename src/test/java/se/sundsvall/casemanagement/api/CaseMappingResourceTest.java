package se.sundsvall.casemanagement.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.zalando.problem.Problem;

import se.sundsvall.casemanagement.api.model.enums.CaseType;
import se.sundsvall.casemanagement.integration.db.model.CaseMapping;
import se.sundsvall.casemanagement.service.CaseMappingService;

@ExtendWith(MockitoExtension.class)
class CaseMappingResourceTest {

	@Mock
	private CaseMappingService caseMappingService;

	@InjectMocks
	private CaseMappingResource caseMappingResource;

	@Test
    void getCaseMapping() {

        when(caseMappingService.getCaseMapping(any(String.class))).thenReturn(CaseMapping.builder()
            .withCaseId("caseId")
            .withExternalCaseId("externalCaseId")
            .withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL)
            .withServiceName("serviceName")
            .withTimestamp(LocalDateTime.now())
            .build());

        final var result = caseMappingResource.getCaseMapping("externalCaseId");

        verify(caseMappingService).getCaseMapping(any(String.class));

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(1);
        assertThat(result.getBody().get(0).getCaseId()).isEqualTo("caseId");
        assertThat(result.getBody().get(0).getExternalCaseId()).isEqualTo("externalCaseId");
        assertThat(result.getBody().get(0).getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL);
        assertThat(result.getBody().get(0).getServiceName()).isEqualTo("serviceName");
        assertThat(result.getBody().get(0).getTimestamp()).isNotNull();
        assertThat(result.getBody().get(0).getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

	@Test
    void getAllCaseMappings() {

        when(caseMappingService.getAllCaseMappings()).thenReturn(List.of(CaseMapping.builder()
                .withCaseId("caseId")
                .withExternalCaseId("externalCaseId")
                .withCaseType(CaseType.REGISTRERING_AV_LIVSMEDEL)
                .withServiceName("serviceName")
                .withTimestamp(LocalDateTime.now())
                .build(),
            CaseMapping.builder()
                .withCaseId("caseId2")
                .withExternalCaseId("externalCaseId2")
                .withCaseType(CaseType.LOST_PARKING_PERMIT)
                .withServiceName("serviceName2")
                .withTimestamp(LocalDateTime.now())
                .build()));

        final var result = caseMappingResource.getCaseMapping(null);

        verify(caseMappingService).getAllCaseMappings();

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody()).hasSize(2);
        assertThat(result.getBody().get(0).getCaseId()).isEqualTo("caseId");
        assertThat(result.getBody().get(0).getExternalCaseId()).isEqualTo("externalCaseId");
        assertThat(result.getBody().get(0).getCaseType()).isEqualTo(CaseType.REGISTRERING_AV_LIVSMEDEL);
        assertThat(result.getBody().get(0).getServiceName()).isEqualTo("serviceName");
        assertThat(result.getBody().get(0).getTimestamp()).isNotNull();
        assertThat(result.getBody().get(0).getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(result.getBody().get(1).getCaseId()).isEqualTo("caseId2");
        assertThat(result.getBody().get(1).getExternalCaseId()).isEqualTo("externalCaseId2");
        assertThat(result.getBody().get(1).getCaseType()).isEqualTo(CaseType.LOST_PARKING_PERMIT);
        assertThat(result.getBody().get(1).getServiceName()).isEqualTo("serviceName2");
        assertThat(result.getBody().get(1).getTimestamp()).isNotNull();
        assertThat(result.getBody().get(1).getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

	@Test
    void getCeaseMappingNothingFound() {
        when(caseMappingService.getCaseMapping(any(String.class))).thenThrow(Problem.builder()
            .withTitle("No case mapping found")
            .withStatus(NOT_FOUND)
            .build());

        assertThatThrownBy(() -> caseMappingResource.getCaseMapping("externalCaseId"), "", Problem.class);
    }

	@Test
    void getAllCaseMappingsNothingFound() {
        when(caseMappingService.getAllCaseMappings()).thenThrow(Problem.builder()
            .withTitle("No case mappings found")
            .withStatus(NOT_FOUND)
            .build());

        assertThatThrownBy(() -> caseMappingResource.getCaseMapping(null), "", Problem.class)
            .hasMessage("No case mappings found");
    }
}
