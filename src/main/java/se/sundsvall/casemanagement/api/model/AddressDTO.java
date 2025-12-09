package se.sundsvall.casemanagement.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casemanagement.api.model.enums.AddressCategory;
import se.sundsvall.casemanagement.api.validation.ByggRConstraints;
import se.sundsvall.casemanagement.api.validation.EcosConstraints;
import se.sundsvall.casemanagement.util.Constants;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Address model")
public class AddressDTO implements Serializable {

	private static final long serialVersionUID = 7944510843548085883L;

	@NotEmpty
	@Schema(description = "An address can have one or more address categories. For example, it can be the same address that is to be used for mail and invoices.")
	private List<AddressCategory> addressCategories;

	@Schema(examples = "Testargatan")
	private String street;

	@Schema(examples = "18")
	private String houseNumber;

	@Schema(examples = "123 45")
	private String postalCode;

	@Schema(examples = "Sundsvall")
	private String city;

	@Schema(examples = Constants.SWEDEN)
	private String country;

	@Schema(description = "c/o", examples = "Test Testorsson")
	private String careOf;

	@Schema(examples = "Test Testorsson")
	private String attention;

	@NotBlank(groups = {
		EcosConstraints.class, ByggRConstraints.class
	})
	@Schema(examples = "SUNDSVALL BALDER 7:2")
	private String propertyDesignation;

	@Schema(examples = "LGH 1001")
	private String appartmentNumber;

	@Schema(description = "The address coordinates")
	private CoordinatesDTO location;

	@Schema(description = "Is the address in zoning plan area?")
	private Boolean isZoningPlanArea;

	@Schema(description = "Only in combination with addressCategory: INVOICE_ADDRESS")
	private String invoiceMarking;

	@Schema(description = "Extra parameters for the address.")
	private Map<String, String> extraParameters;

}
