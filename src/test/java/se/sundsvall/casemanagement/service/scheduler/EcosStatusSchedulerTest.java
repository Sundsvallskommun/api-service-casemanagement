package se.sundsvall.casemanagement.service.scheduler;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class EcosStatusSchedulerTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String JOB_NAME = "EcosStatusScheduler";

	@Mock
	private EcosStatusWorker workerMock;

	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;

	private EcosStatusScheduler scheduler;

	@BeforeEach
	void setUp() {
		scheduler = new EcosStatusScheduler(workerMock, dept44HealthUtilityMock, List.of(MUNICIPALITY_ID), JOB_NAME);
	}

	@Test
	void checkAndUpdateEcosStatus() {
		// Act
		scheduler.checkAndUpdateEcosStatus();

		// Assert
		verify(workerMock).updateStatuses(MUNICIPALITY_ID);
		verifyNoInteractions(dept44HealthUtilityMock);
		verifyNoMoreInteractions(workerMock);
	}

	@Test
	void checkAndUpdateEcosStatusWhenWorkerThrows() {
		// Arrange
		doThrow(new RuntimeException("Test error")).when(workerMock).updateStatuses(MUNICIPALITY_ID);

		// Act
		scheduler.checkAndUpdateEcosStatus();

		// Assert
		verify(workerMock).updateStatuses(MUNICIPALITY_ID);
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy(JOB_NAME, "Test error");
		verifyNoMoreInteractions(workerMock, dept44HealthUtilityMock);
	}

}
