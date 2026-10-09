package se.sundsvall.casemanagement.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeEntity;

import static java.util.Collections.emptyList;
import static se.sundsvall.casemanagement.api.model.enums.SystemType.CASE_DATA;

/**
 * Central registry for case types. Static types (Byggr/Ecos/EdpFuture) are looked up from the database. Non-static
 * types are assumed to be CaseData types at deserialization time; actual validation against CaseData's metadata API
 * happens later when
 * municipalityId is available.
 */
@Service
public class CaseTypeRegistry {

	private static final String OTHER = "OTHER";

	private final CaseTypeRepository caseTypeRepository;
	private final CaseDataCaseTypeProvider caseDataCaseTypeProvider;
	private final CaseDataProperties caseDataProperties;

	public CaseTypeRegistry(final CaseTypeRepository caseTypeRepository, final CaseDataCaseTypeProvider caseDataCaseTypeProvider, final CaseDataProperties caseDataProperties) {
		this.caseTypeRepository = caseTypeRepository;
		this.caseDataCaseTypeProvider = caseDataCaseTypeProvider;
		this.caseDataProperties = caseDataProperties;
	}

	/**
	 * Resolves which system a case type belongs to. Static types are matched from the database. Any non-static type is
	 * assumed to be CASE_DATA — actual existence is validated later via {@link #isCaseDataType(String, String)} when
	 * municipalityId is known.
	 *
	 * @return the SystemType, or CASE_DATA for non-static types, or empty if null
	 */
	public Optional<SystemType> resolveSystem(final String caseType) {
		if (caseType == null) {
			return Optional.empty();
		}
		return Optional.of(caseTypeRepository.findById(caseType)
			.map(CaseTypeEntity::getSystemType)
			.orElse(CASE_DATA));
	}

	/**
	 * Checks if the given caseType exists as a CaseData type for the given municipalityId by querying configured
	 * namespaces. A namespace whose case types cannot be fetched does not hide a match in another namespace; the failure
	 * is only rethrown when no other namespace contains the caseType, since the caseType cannot then be ruled out.
	 */
	public boolean isCaseDataType(final String caseType, final String municipalityId) {
		RuntimeException lookupFailure = null;
		for (final var namespace : namespacesFor(caseType, municipalityId)) {
			try {
				if (caseDataCaseTypeProvider.getCaseDataTypesByNamespace(municipalityId, namespace).containsKey(caseType)) {
					return true;
				}
			} catch (final RuntimeException e) {
				lookupFailure = e;
			}
		}
		if (lookupFailure != null) {
			throw lookupFailure;
		}
		return false;
	}

	/**
	 * Resolves the CaseData namespace for a given caseType and municipalityId. Looks up which namespace the caseType
	 * belongs to by querying the configured namespaces for that municipality in order. A namespace whose case types cannot
	 * be fetched fails the lookup rather than being skipped, as skipping it could resolve the caseType to a later
	 * namespace than the one it belongs to.
	 */
	public String resolveNamespace(final String caseType, final String municipalityId) {
		for (final var namespace : namespacesFor(caseType, municipalityId)) {
			if (caseDataCaseTypeProvider.getCaseDataTypesByNamespace(municipalityId, namespace).containsKey(caseType)) {
				return namespace;
			}
		}
		return OTHER;
	}

	private List<String> namespacesFor(final String caseType, final String municipalityId) {
		if (caseType == null || municipalityId == null) {
			return emptyList();
		}
		return Optional.ofNullable(caseDataProperties.namespaces())
			.map(namespaces -> namespaces.get(municipalityId))
			.orElse(emptyList());
	}

}
