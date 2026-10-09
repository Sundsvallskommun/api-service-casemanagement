package se.sundsvall.casemanagement.service;

import generated.client.casedata.CaseType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.integration.casedata.CaseDataClient;
import se.sundsvall.dept44.exception.ClientProblem;

import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

/**
 * Provides cached access to CaseData case types. Separated from CaseTypeRegistry to avoid Spring @Cacheable
 * self-invocation issues.
 */
@Component
public class CaseDataCaseTypeProvider {

	private static final Logger LOG = LoggerFactory.getLogger(CaseDataCaseTypeProvider.class);

	private final CaseDataClient caseDataClient;
	private final Map<String, Map<String, String>> lastKnownCaseTypes = new ConcurrentHashMap<>();

	public CaseDataCaseTypeProvider(final CaseDataClient caseDataClient) {
		this.caseDataClient = caseDataClient;
	}

	/**
	 * Fetches CaseData case types for a given municipalityId and namespace. Returns a map of caseType -> displayName.
	 * Cached with 15-minute TTL.
	 * <p>
	 * If CaseData cannot be reached, the case types last fetched for the namespace are returned instead, so that a CaseData
	 * outage does not reject cases that would otherwise be stored and delivered once CaseData is back. A client error
	 * (4xx), or a failure before any case types have been fetched for the namespace, is rethrown.
	 */
	@Cacheable(value = "caseDataCaseTypes", key = "#municipalityId + ':' + #namespace")
	public Map<String, String> getCaseDataTypesByNamespace(final String municipalityId, final String namespace) {
		LOG.debug("Fetching case types from CaseData for municipalityId: {}, namespace: {}",
			sanitizeForLogging(municipalityId), sanitizeForLogging(namespace));

		final var key = municipalityId + ":" + namespace;
		try {
			final var types = caseDataClient.getCaseTypes(municipalityId, namespace).stream()
				.collect(Collectors.toMap(CaseType::getType, CaseType::getDisplayName, (a, b) -> b, HashMap::new));
			lastKnownCaseTypes.put(key, types);
			return types;
		} catch (final RuntimeException e) {
			final var lastKnown = lastKnownCaseTypes.get(key);
			if (e instanceof ClientProblem || lastKnown == null) {
				throw e;
			}
			LOG.warn("Unable to fetch case types from CaseData for municipalityId: {}, namespace: {}, using the last fetched case types instead: {}",
				sanitizeForLogging(municipalityId), sanitizeForLogging(namespace), e.getMessage());
			return lastKnown;
		}
	}

}
