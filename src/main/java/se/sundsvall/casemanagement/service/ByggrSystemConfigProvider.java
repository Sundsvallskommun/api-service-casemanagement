package se.sundsvall.casemanagement.service;

import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.integration.db.ByggrStatusMappingRuleRepository;

@Component
public class ByggrSystemConfigProvider {

	private final ByggrStatusMappingRuleRepository statusMappingRuleRepository;

	public ByggrSystemConfigProvider(final ByggrStatusMappingRuleRepository statusMappingRuleRepository) {
		this.statusMappingRuleRepository = statusMappingRuleRepository;
	}

	private static boolean matches(final String ruleValue, final String inputValue) {
		return ruleValue == null || ruleValue.equals(inputValue);
	}

	private static String pickReturnValue(final String returnField, final String typ, final String slag, final String utfall) {
		return switch (returnField) {
			case "TYP" -> typ;
			case "SLAG" -> slag;
			case "UTFALL" -> utfall;
			default -> null;
		};
	}

	/**
	 * Maps a ByggR event (typ/slag/utfall) to an OEP status string using rules from the byggr_status_mapping table. Each
	 * rule matches on typ/slag/utfall (null = wildcard) and specifies which of the three input values to return as the
	 * status.
	 *
	 * @return the matched status value, or null if no rule matches (event is not status-relevant)
	 */
	public String resolveHandelseStatus(final String handelsetyp, final String handelseslag, final String handelseutfall) {
		return statusMappingRuleRepository.findAll().stream()
			.filter(rule -> matches(rule.getHandelseTyp(), handelsetyp))
			.filter(rule -> matches(rule.getHandelseSlag(), handelseslag))
			.filter(rule -> matches(rule.getHandelseUtfall(), handelseutfall))
			.findFirst()
			.map(rule -> pickReturnValue(rule.getReturnField(), handelsetyp, handelseslag, handelseutfall))
			.orElse(null);
	}

}
