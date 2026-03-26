package se.sundsvall.casemanagement.service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.integration.casedata.configuration.CaseDataProperties;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.model.CaseTypeEntity;

import static se.sundsvall.casemanagement.api.model.enums.SystemType.CASE_DATA;

/**
 * Central registry for case types. Static types (Byggr/Ecos/EdpFuture) are loaded from the database. Non-static types
 * are assumed to be CaseData types at deserialization time; actual validation against CaseData's metadata API happens
 * later when municipalityId is available.
 */
@Service
public class CaseTypeRegistry {

	private static final String OTHER = "OTHER";

	private final Map<String, SystemType> staticTypes;
	private final CaseDataCaseTypeProvider caseDataCaseTypeProvider;
	private final CaseDataProperties caseDataProperties;

	public CaseTypeRegistry(final CaseTypeRepository caseTypeRepository, final CaseDataCaseTypeProvider caseDataCaseTypeProvider, final CaseDataProperties caseDataProperties) {
		this.caseDataCaseTypeProvider = caseDataCaseTypeProvider;
		this.caseDataProperties = caseDataProperties;
		this.staticTypes = caseTypeRepository.findAll().stream()
			.collect(Collectors.toUnmodifiableMap(CaseTypeEntity::getName, CaseTypeEntity::getSystemType));
	}

	/**
	 * Resolves which system a case type belongs to. Static types are matched from the database. Any non-static type is
	 * assumed to be CASE_DATA — actual existence is validated later via
	 * {@link #isCaseDataType(String, String)} when municipalityId is known.
	 *
	 * @return the SystemType, or CASE_DATA for non-static types, or empty if null
	 */
	public Optional<SystemType> resolveSystem(final String caseType) {
		if (caseType == null) {
			return Optional.empty();
		}
		return Optional.of(Optional.ofNullable(staticTypes.get(caseType)).orElse(CASE_DATA));
	}

	/**
	 * Checks if the given caseType exists as a CaseData type for the given municipalityId by querying configured
	 * namespaces.
	 */
	public boolean isCaseDataType(final String caseType, final String municipalityId) {
		return !OTHER.equals(resolveNamespace(caseType, municipalityId));
	}

	/**
	 * Resolves the CaseData namespace for a given caseType and municipalityId. Looks up which namespace the caseType
	 * belongs to by querying all configured namespaces for that municipality.
	 */
	public String resolveNamespace(final String caseType, final String municipalityId) {
		if (caseType == null || municipalityId == null) {
			return OTHER;
		}
		final var namespacesForMunicipality = Optional.ofNullable(caseDataProperties.namespaces())
			.map(ns -> ns.get(municipalityId))
			.orElse(null);

		if (namespacesForMunicipality == null) {
			return OTHER;
		}

		for (final var namespace : namespacesForMunicipality) {
			final var typesInNamespace = caseDataCaseTypeProvider.getCaseDataTypesByNamespace(municipalityId, namespace);
			if (typesInNamespace.containsKey(caseType)) {
				return namespace;
			}
		}
		return OTHER;
	}

}
