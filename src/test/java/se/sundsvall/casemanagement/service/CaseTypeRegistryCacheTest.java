package se.sundsvall.casemanagement.service;

import generated.client.casedata.CaseType;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.casemanagement.Application;
import se.sundsvall.casemanagement.integration.casedata.CaseDataClient;
import se.sundsvall.dept44.exception.ServerProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;

/**
 * Exercises the CaseData case type lookup through the real Spring cache: a successful fetch is cached, while a fallback
 * on the last fetched case types is not, so CaseData is asked again on the next lookup.
 */
@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class CaseTypeRegistryCacheTest {

	private static final String CACHE_NAME = "caseDataCaseTypes";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE = "NAMESPACE_1";

	@MockitoBean
	private CaseDataClient caseDataClient;

	@Autowired
	private CaseTypeRegistry caseTypeRegistry;

	@Autowired
	private CacheManager cacheManager;

	@BeforeEach
	void setUp() {
		expireCachedCaseTypes();
	}

	@Test
	void successfulFetchIsCached() {
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE)).thenReturn(List.of(caseType("PARKING_PERMIT")));

		assertThat(caseTypeRegistry.resolveNamespace("PARKING_PERMIT", MUNICIPALITY_ID)).isEqualTo(NAMESPACE);
		assertThat(caseTypeRegistry.resolveNamespace("PARKING_PERMIT", MUNICIPALITY_ID)).isEqualTo(NAMESPACE);

		verify(caseDataClient).getCaseTypes(MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void fallbackOnLastFetchedTypesIsNotCached() {
		when(caseDataClient.getCaseTypes(MUNICIPALITY_ID, NAMESPACE))
			.thenReturn(List.of(caseType("PARKING_PERMIT")))
			.thenThrow(new ServerProblem(BAD_GATEWAY, "case-data error: {status=500 Internal Server Error}"))
			.thenReturn(List.of(caseType("PARKING_PERMIT"), caseType("LOST_PARKING_PERMIT")));

		// Fetched from CaseData and cached
		assertThat(caseTypeRegistry.resolveNamespace("PARKING_PERMIT", MUNICIPALITY_ID)).isEqualTo(NAMESPACE);

		// The cache entry expires while CaseData is unavailable: the last fetched case types are used
		expireCachedCaseTypes();
		assertThat(caseTypeRegistry.resolveNamespace("PARKING_PERMIT", MUNICIPALITY_ID)).isEqualTo(NAMESPACE);

		// CaseData answers again: the fallback was not cached, so a case type added meanwhile is found at once
		assertThat(caseTypeRegistry.resolveNamespace("LOST_PARKING_PERMIT", MUNICIPALITY_ID)).isEqualTo(NAMESPACE);

		verify(caseDataClient, times(3)).getCaseTypes(MUNICIPALITY_ID, NAMESPACE);
	}

	private void expireCachedCaseTypes() {
		Objects.requireNonNull(cacheManager.getCache(CACHE_NAME)).clear();
	}

	private static CaseType caseType(final String type) {
		return new CaseType().type(type).displayName(type);
	}

}
