package se.sundsvall.casemanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.RegisterbeteckningClient;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.service.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class RegisterbeteckningServiceTest {

    @InjectMocks
    private RegisterbeteckningService registerbeteckningService;

    @Mock
    private RegisterbeteckningClient registerbeteckningClient;

    @Test
    void testGetRegisterbeteckningsreferens() {
        String propertyDesignation = "TEST 1:1";

        List<Registerbeteckningsreferens> registerbeteckningsreferenser = new ArrayList<>();
        Registerbeteckningsreferens registerbeteckningsreferens = new Registerbeteckningsreferens();
        registerbeteckningsreferens.setBeteckningsid(UUID.randomUUID().toString());
        registerbeteckningsreferens.setBeteckning(propertyDesignation);
        registerbeteckningsreferens.setRegisterenhet(RandomStringUtils.random(10, true, false));
        registerbeteckningsreferenser.add(registerbeteckningsreferens);
        doReturn(registerbeteckningsreferenser).when(registerbeteckningClient).getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1);

        var result = registerbeteckningService.getRegisterbeteckningsreferens(propertyDesignation);
        assertEquals(registerbeteckningsreferens, result);
    }

    @Test
    void testGetRegisterbeteckningsreferensEmptyList() {
        String propertyDesignation = "TEST 1:1";

        doReturn(new ArrayList<>()).when(registerbeteckningClient).getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1);

        var result = registerbeteckningService.getRegisterbeteckningsreferens(propertyDesignation);
        assertNull(result);
    }

    @Test
    void testGetRegisterbeteckningsreferensUnexpectedResponse() {
        String propertyDesignation = "TEST 1:1";

        List<Registerbeteckningsreferens> registerbeteckningsreferenser = new ArrayList<>();
        Registerbeteckningsreferens registerbeteckningsreferens = new Registerbeteckningsreferens();
        registerbeteckningsreferens.setBeteckningsid(UUID.randomUUID().toString());
        // This is not the same as propertyDesignation - Therefor it should return null
        registerbeteckningsreferens.setBeteckning("SOME RANDOM PROPERTY DESIGNATION");
        registerbeteckningsreferens.setRegisterenhet(RandomStringUtils.random(10, true, false));
        registerbeteckningsreferenser.add(registerbeteckningsreferens);
        doReturn(registerbeteckningsreferenser).when(registerbeteckningClient).getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1);

        var result = registerbeteckningService.getRegisterbeteckningsreferens(propertyDesignation);
        assertNull(result);
    }

}
