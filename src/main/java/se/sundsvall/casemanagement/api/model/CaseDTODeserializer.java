package se.sundsvall.casemanagement.api.model;

import se.sundsvall.casemanagement.api.model.enums.SystemType;
import se.sundsvall.casemanagement.service.CaseTypeRegistry;
import se.sundsvall.dept44.problem.Problem;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Custom Jackson deserializer that replaces @JsonSubTypes for polymorphic CaseDTO deserialization. Looks up the
 * caseType in the CaseTypeRegistry to determine which DTO subclass to use.
 */
public class CaseDTODeserializer extends StdDeserializer<CaseDTO> {

	private final CaseTypeRegistry caseTypeRegistry;

	public CaseDTODeserializer(final CaseTypeRegistry caseTypeRegistry) {
		super(CaseDTO.class);
		this.caseTypeRegistry = caseTypeRegistry;
	}

	private static Class<? extends CaseDTO> resolveTargetClass(final SystemType systemType) {
		return switch (systemType) {
			case BYGGR -> ByggRCaseDTO.class;
			case ECOS -> EcosCaseDTO.class;
			case CASE_DATA -> OtherCaseDTO.class;
			case EDPFUTURE -> FutureCaseDTO.class;
			default -> throw Problem.valueOf(BAD_REQUEST, "Unsupported system type: " + systemType);
		};
	}

	@Override
	public CaseDTO deserialize(final JsonParser parser, final DeserializationContext context) {
		final var node = context.readTree(parser);

		final var caseTypeNode = node.get("caseType");
		if (caseTypeNode == null || caseTypeNode.isNull()) {
			throw Problem.valueOf(BAD_REQUEST, "caseType is required");
		}

		final var caseType = caseTypeNode.asString();
		final var systemType = caseTypeRegistry.resolveSystem(caseType)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, "Unknown caseType: " + caseType));

		final Class<? extends CaseDTO> targetClass = resolveTargetClass(systemType);

		return context.readTreeAsValue(node, targetClass);
	}

}
