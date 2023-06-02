package se.sundsvall.casemanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.casemanagement.integration.rest.lantmateriet.RegisterbeteckningClient;
import se.sundsvall.casemanagement.integration.rest.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.service.util.CaseUtil;
import se.sundsvall.casemanagement.service.util.Constants;

@Service
public class RegisterbeteckningService {

    private final RegisterbeteckningClient registerbeteckningClient;


    public RegisterbeteckningService(RegisterbeteckningClient registerbeteckningClient) {
        this.registerbeteckningClient = registerbeteckningClient;
    }

    public Registerbeteckningsreferens getRegisterbeteckningsreferens(String propertyDesignation) {

        List<Registerbeteckningsreferens> registerbeteckningsreferenser = registerbeteckningClient.getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1);

        if (CaseUtil.notNullOrEmpty(registerbeteckningsreferenser)
            && registerbeteckningsreferenser.get(0).getBeteckning().equalsIgnoreCase(propertyDesignation)) {
            return registerbeteckningsreferenser.get(0);
        } else {
            return null;
        }
    }

}
