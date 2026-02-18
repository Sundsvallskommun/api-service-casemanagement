package se.sundsvall.casemanagement.integration.fb.model;

import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.allOf;

class DataItemTest {

	@Test
	void testBean() {
		MatcherAssert.assertThat(DataItem.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testFields() {
		DataItem object = new DataItem();
		object.setCoAdress("coAdress");
		object.setPostort("postort");
		object.setPostnummer("postnummer");
		object.setLand("land");
		object.setFnr(123);
		object.setGrupp(List.of(new GruppItem()));
		object.setUtdelningsadress1("utdelningsadress1");
		object.setUtdelningsadress2("utdelningsadress2");
		object.setUtdelningsadress3("utdelningsadress3");
		object.setUtdelningsadress4("utdelningsadress4");
		object.setAdresstyp("adresstyp");
		object.setIdentitetsnummer("identitetsnummer");
		object.setJuridiskForm("juridiskForm");
		object.setKommun("kommun");
		object.setBeteckning("beteckning");
		object.setTrakt("trakt");
		object.setBeteckningsnummer("beteckningsnummer");
		object.setUuid(UUID.randomUUID().toString());
		object.setGallandeOrganisationsnamn("gallandeOrganisationsnamn");
		object.setGallandeFornamn("gallandeFornamn");
		object.setGallandeEfternamn("gallandeEfternamn");

		Assertions.assertThat(object).isNotNull().hasNoNullFieldsOrProperties();
	}
}
