package se.sundsvall.casemanagement.integration.opene;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenEIntegrationTest {

	@Mock
	private OpeneClient openeClient;

	@InjectMocks
	private OpenEIntegration openEIntegration;

	@Test
	void confirmDelivery_throws() {
		doThrow(new RuntimeException()).when(openeClient).confirmDelivery(any());

		openEIntegration.confirmDelivery("1", "2", "3");

		verify(openeClient).confirmDelivery(any());
		verifyNoMoreInteractions(openeClient);
	}

	@Test
	void setStatus_throws() {
		doThrow(new RuntimeException()).when(openeClient).setStatus(any());

		openEIntegration.setStatus("1", "2", "3", "4");

		verify(openeClient).setStatus(any());
		verifyNoMoreInteractions(openeClient);
	}

}
