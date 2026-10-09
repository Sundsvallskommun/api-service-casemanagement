package se.sundsvall.casemanagement.service;

import generated.client.casedata.CaseType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import se.sundsvall.casemanagement.integration.casedata.CaseDataClient;

import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

/**
 * Provides cached access to CaseData case types. Separated from CaseTypeRegistry to avoid Spring @Cacheable
 * self-invocation issues.
 */
@Component
public class CaseDataCaseTypeProvider {

	private static final Logger LOG = LoggerFactory.getLogger(CaseDataCaseTypeProvider.class);

	private final CaseDataClient caseDataClient;
	private final Map<String, Map<String, String>> lastFetchedCaseTypes = new ConcurrentHashMap<>();

	public CaseDataCaseTypeProvider(final CaseDataClient caseDataClient) {
		this.caseDataClient = caseDataClient;
	}

	/**
	 * Fetches CaseData case types for a given municipalityId and namespace. Returns a map of caseType -> displayName.
	 * Cached with 15-minute TTL. A failed fetch is not cached. Each successful fetch is also kept as the last fetched case
	 * types, see {@link #getLastFetchedCaseDataTypes(String, String)}.
	 */
	@Cacheable(value = "caseDataCaseTypes", key = "#municipalityId + ':' + #namespace")
	public Map<String, String> getCaseDataTypesByNamespace(final String municipalityId, final String namespace) {
		LOG.debug("Fetching case types from CaseData for municipalityId: {}, namespace: {}",
			sanitizeForLogging(municipalityId), sanitizeForLogging(namespace));

		final var types = caseDataClient.getCaseTypes(municipalityId, namespace).stream()
			.collect(Collectors.toMap(CaseType::getType, CaseType::getDisplayName, (a, b) -> b, HashMap::new));
		lastFetchedCaseTypes.put(toKey(municipalityId, namespace), types);
		return types;
	}

	/**
	 * Returns the case types last fetched from CaseData for a given municipalityId and namespace, without calling
	 * CaseData. This lookup bypasses the cache, so a fallback on these case types is never cached: the next call to
	 * {@link #getCaseDataTypesByNamespace(String, String)} asks CaseData again.
	 */
	public Optional<Map<String, String>> getLastFetchedCaseDataTypes(final String municipalityId, final String namespace) {
		return Optional.ofNullable(lastFetchedCaseTypes.get(toKey(municipalityId, namespace)));
	}

	private static String toKey(final String municipalityId, final String namespace) {
		return municipalityId + ":" + namespace;
	}

}
