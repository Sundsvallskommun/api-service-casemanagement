package se.sundsvall.casemanagement.service;

import java.util.List;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.integration.lantmateriet.RegisterbeteckningClient;
import se.sundsvall.casemanagement.integration.lantmateriet.model.Registerbeteckningsreferens;
import se.sundsvall.casemanagement.util.CaseUtil;
import se.sundsvall.casemanagement.util.Constants;

@Service
public class RegisterbeteckningService {

	private final RegisterbeteckningClient registerbeteckningClient;

	public RegisterbeteckningService(RegisterbeteckningClient registerbeteckningClient) {
		this.registerbeteckningClient = registerbeteckningClient;
	}

	public Registerbeteckningsreferens getRegisterbeteckningsreferens(String propertyDesignation) {

		final List<Registerbeteckningsreferens> registerbeteckningsreferenser = registerbeteckningClient.getRegisterbeteckningsreferenser(propertyDesignation, Constants.LANTMATERIET_REFERENS_STATUS_GALLANDE, 1);

		if (CaseUtil.notNullOrEmpty(registerbeteckningsreferenser)
			&& registerbeteckningsreferenser.getFirst().getBeteckning().equalsIgnoreCase(propertyDesignation)) {
			return registerbeteckningsreferenser.getFirst();
		}
		return null;
	}
}
