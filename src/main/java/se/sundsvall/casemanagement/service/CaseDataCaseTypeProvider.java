package se.sundsvall.casemanagement.service;

import java.util.HashMap;
import java.util.Map;
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

	public CaseDataCaseTypeProvider(final CaseDataClient caseDataClient) {
		this.caseDataClient = caseDataClient;
	}

	/**
	 * Fetches CaseData case types for a given municipalityId and namespace. Returns a map of caseType -> displayName.
	 * Cached with 15-minute TTL.
	 */
	@Cacheable(value = "caseDataCaseTypes", key = "#municipalityId + ':' + #namespace")
	public Map<String, String> getCaseDataTypesByNamespace(final String municipalityId, final String namespace) {
		final var safeMunicipalityId = sanitizeForLogging(municipalityId);
		final var safeNamespace = sanitizeForLogging(namespace);

		LOG.debug("Fetching case types from CaseData for municipalityId: {}, namespace: {}", safeMunicipalityId, safeNamespace);
		try {
			final var types = caseDataClient.getCaseTypes(municipalityId, namespace);
			final var result = new HashMap<String, String>();
			for (final var type : types) {
				result.put(type.getType(), type.getDisplayName());
			}
			return Map.copyOf(result);
		} catch (final Exception e) {
			LOG.warn("Failed to fetch case types from CaseData for {}/{}: {}", safeMunicipalityId, safeNamespace, e.getMessage());
			return Map.of();
		}
	}

}
