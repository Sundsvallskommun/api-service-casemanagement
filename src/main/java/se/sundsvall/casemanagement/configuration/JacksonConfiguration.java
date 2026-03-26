package se.sundsvall.casemanagement.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import se.sundsvall.casemanagement.api.model.CaseDTO;
import se.sundsvall.casemanagement.api.model.CaseDTODeserializer;
import se.sundsvall.casemanagement.service.CaseTypeRegistry;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfiguration {

	@Bean
	JacksonModule caseDTOModule(@Lazy final CaseTypeRegistry caseTypeRegistry) {
		final var module = new SimpleModule("CaseDTOModule");
		module.addDeserializer(CaseDTO.class, new CaseDTODeserializer(caseTypeRegistry));
		return module;
	}

}
