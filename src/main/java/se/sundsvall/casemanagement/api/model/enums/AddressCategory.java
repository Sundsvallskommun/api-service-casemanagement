package se.sundsvall.casemanagement.api.model.enums;

public enum AddressCategory {
    POSTAL_ADDRESS("Postadress"), INVOICE_ADDRESS("Fakturaadress"), VISITING_ADDRESS("Besöksadress");

    private final String text;

    AddressCategory(String text) {
        this.text = text;
    }
}
