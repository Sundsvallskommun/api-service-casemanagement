package se.sundsvall.casemanagement.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import se.sundsvall.casemanagement.integration.lantmateriet.RegisterbeteckningClient;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.util.Constants;

@ExtendWith(MockitoExtension.class)
class RegisterbeteckningServiceTest {

	@InjectMocks
	private RegisterbeteckningService registerbeteckningService;

	@Mock
	private RegisterbeteckningClient registerbeteckningClient;

	@Test
	void testGetRegisterbeteckningsreferens() {
		// Arrange
		final var propertyDesignation = "TEST 1:1";
		final var registerbeteckningsreferens = new Registerbeteckningsreferens();
		registerbeteckningsreferens.setBeteckningsid(UUID.randomUUID().toString());
		registerbeteckningsreferens.setBeteckning(propertyDesignation);
		registerbeteckningsreferens.setRegisterenhet(RandomStringUtils.random(10, true, false));
		//Mock
		when(registerbeteckningClient.getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1)).thenReturn(List.of(registerbeteckningsreferens));
		// Act
		final var result = registerbeteckningService.getRegisterbeteckningsreferens(propertyDesignation);
		// Assert
		assertThat(result).isEqualTo(registerbeteckningsreferens);
	}

	@Test
	void testGetRegisterbeteckningsreferensEmptyList() {
		// Arrange
		final String propertyDesignation = "TEST 1:1";
		//Mock
		when(registerbeteckningClient.getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1)).thenReturn(emptyList());
		// Act
		final var result = registerbeteckningService.getRegisterbeteckningsreferens(propertyDesignation);
		// Assert
		assertThat(result).isNull();
	}

	@Test
	void testGetRegisterbeteckningsreferensUnexpectedResponse() {
		// Arrange
		final var propertyDesignation = "TEST 1:1";

		final var registerbeteckningsreferenser = new ArrayList<Registerbeteckningsreferens>();
		final var registerbeteckningsreferens = new Registerbeteckningsreferens();
		registerbeteckningsreferens.setBeteckningsid(UUID.randomUUID().toString());
		// This is not the same as propertyDesignation - Therefore it should return null
		registerbeteckningsreferens.setBeteckning("SOME RANDOM PROPERTY DESIGNATION");
		registerbeteckningsreferens.setRegisterenhet(RandomStringUtils.random(10, true, false));
		registerbeteckningsreferenser.add(registerbeteckningsreferens);
		when(registerbeteckningClient.getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1)).thenReturn(registerbeteckningsreferenser);

		// Act
		final var result = registerbeteckningService.getRegisterbeteckningsreferens(propertyDesignation);

		// Assert
		assertThat(result).isNull();
	}

}
