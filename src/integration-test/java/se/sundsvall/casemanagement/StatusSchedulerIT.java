package se.sundsvall.casemanagement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import se.sundsvall.casemanagement.service.scheduler.StatusScheduler;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/StatusSchedulerIT/", classes = Application.class)
@Sql({
        "/db/scripts/truncate.sql",
        "/db/scripts/testdata-it.sql"
})
class StatusSchedulerIT extends AbstractAppTest {

    @Autowired
    private StatusScheduler statusScheduler;

    @Test
    void test1_checkAndUpdateStatus() {
        // Initialize WireMock stubs
        setupCall();

        // Execute scheduler
        statusScheduler.checkAndUpdateStatus();

        // Verify eventlog was called for ByggR case (externalCaseId=3522) and ECOS case (externalCaseId=2222 or 2223)
        verify(2, postRequestedFor(urlPathMatching("/eventlog/2281/.*")));
    }
}
