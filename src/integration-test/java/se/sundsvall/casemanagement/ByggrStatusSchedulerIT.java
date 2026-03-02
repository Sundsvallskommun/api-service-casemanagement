package se.sundsvall.casemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.casemanagement.service.scheduler.ByggrStatusScheduler;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/ByggrStatusSchedulerIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class ByggrStatusSchedulerIT extends AbstractAppTest {

	@Autowired
	private ByggrStatusScheduler byggrStatusScheduler;

	@Test
	void test1_checkAndUpdateByggrStatus() {
		setupCall();

		byggrStatusScheduler.checkAndUpdateByggrStatus();

		verify(1, postRequestedFor(urlPathMatching("/eventlog/2281/.*")));
	}

}
