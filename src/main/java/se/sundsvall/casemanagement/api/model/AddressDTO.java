package se.sundsvall.casemanagement.api.model;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.validation.EnvironmentalConstraints;
import se.sundsvall.casemanagement.api.validation.PlanningConstraints;
import se.sundsvall.casemanagement.util.Constants;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

	@NotEmpty
	@Schema(description = "An address can have one or more address categories. For example, it can be the same address that is to be used for mail and invoices.")
	private List<AddressCategory> addressCategories;

	@Schema(example = "Testargatan")
	private String street;

	@Schema(example = "18")
	private String houseNumber;

	@Schema(example = "123 45")
	private String postalCode;

	@Schema(example = "Sundsvall")
	private String city;

	@Schema(example = Constants.SWEDEN)
	private String country;

	@Schema(description = "c/o", example = "Test Testorsson")
	private String careOf;

	@Schema(example = "Test Testorsson")
	private String attention;

	@NotBlank(groups = {EnvironmentalConstraints.class, PlanningConstraints.class})
	@Schema(example = "SUNDSVALL BALDER 7:2")
	private String propertyDesignation;

	@Schema(example = "LGH 1001")
	private String appartmentNumber;

	private CoordinatesDTO location;

	private Boolean isZoningPlanArea;

	@Schema(description = "Only in combination with addressCategory: INVOICE_ADDRESS")
	private String invoiceMarking;

	private Map<String, String> extraParameters;

}
