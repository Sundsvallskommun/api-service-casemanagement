package se.sundsvall.casemanagement.api.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Address category")
public enum AddressCategory {
	POSTAL_ADDRESS, INVOICE_ADDRESS, VISITING_ADDRESS
}
