package se.sundsvall.casemanagement.service.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casemanagement.api.model.FutureCaseDTO;
import se.sundsvall.casemanagement.api.model.OtherCaseDTO;
import se.sundsvall.casemanagement.integration.db.CaseTypeRepository;
import se.sundsvall.casemanagement.integration.db.EcosCaseTypeConfigRepository;
import se.sundsvall.casemanagement.service.CaseTypeRegistry;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidatorTest {

	@Mock
	private CaseTypeRepository caseTypeRepository;

	@Mock
	private EcosCaseTypeConfigRepository ecosCaseTypeConfigRepository;

	@Mock
	private CaseTypeRegistry caseTypeRegistry;

	@InjectMocks
	private Validator validator;

	@Test
	void validateCaseDataErrand_withValidType() {
		final var otherCase = OtherCaseDTO.builder().withCaseType("PARKING_PERMIT").build();
		when(caseTypeRegistry.isCaseDataType("PARKING_PERMIT", "2281")).thenReturn(true);

		assertThatNoException().isThrownBy(() -> validator.validateCaseDataErrand(otherCase, "2281"));
	}

	@Test
	void validateCaseDataErrand_withInvalidType() {
		final var otherCase = OtherCaseDTO.builder().withCaseType("NONEXISTENT").build();
		when(caseTypeRegistry.isCaseDataType("NONEXISTENT", "2281")).thenReturn(false);

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> validator.validateCaseDataErrand(otherCase, "2281"))
			.withMessage("Bad Request: CaseType NONEXISTENT is not a valid CaseData type for municipality 2281");
	}

	@Test
	void validateFutureErrand_withQuantity() {
		final var futureCase = FutureCaseDTO.builder()
			.withExtraParameters(Map.of("Quantity", "1"))
			.build();

		assertThatNoException().isThrownBy(() -> validator.validateFutureErrand(futureCase));
	}

	@Test
	void validateFutureErrand_withNullExtraParameters() {
		final var futureCase = FutureCaseDTO.builder().build();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> validator.validateFutureErrand(futureCase))
			.withMessage("Bad Request: extraParameters must contain a non-blank value for key: Quantity");
	}

	@Test
	void validateFutureErrand_withMissingQuantity() {
		final var futureCase = FutureCaseDTO.builder()
			.withExtraParameters(Map.of("Something", "else"))
			.build();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> validator.validateFutureErrand(futureCase))
			.withMessage("Bad Request: extraParameters must contain a non-blank value for key: Quantity");
	}

	@Test
	void validateFutureErrand_withBlankQuantity() {
		final var params = new HashMap<String, String>();
		params.put("Quantity", "   ");
		final var futureCase = FutureCaseDTO.builder()
			.withExtraParameters(params)
			.build();

		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> validator.validateFutureErrand(futureCase))
			.withMessage("Bad Request: extraParameters must contain a non-blank value for key: Quantity");
	}

}
