package se.sundsvall.casemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.casemanagement.service.scheduler.EcosStatusScheduler;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/EcosStatusSchedulerIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class EcosStatusSchedulerIT extends AbstractAppTest {

	@Autowired
	private EcosStatusScheduler ecosStatusScheduler;

	@Test
	void test1_checkAndUpdateEcosStatus() {
		setupCall();

		ecosStatusScheduler.checkAndUpdateEcosStatus();

		verify(1, postRequestedFor(urlPathMatching("/eventlog/2281/.*")));
	}

}
