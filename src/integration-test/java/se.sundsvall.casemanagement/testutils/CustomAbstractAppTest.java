package se.sundsvall.casemanagement.testutils;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import se.sundsvall.dept44.test.AbstractAppTest;

import java.util.List;

/**
 * Created this custom implementation so that we could reuse the old integration test files from the Quarkus project.
 * Now we can store all mappings and responses in common-folder and will not have a problem with that all files
 * are not used in all tests.
 */
public class CustomAbstractAppTest extends AbstractAppTest {

    @Override
    public void verifyAllStubs() {
        List<LoggedRequest> unmatchedRequests = this.wiremock.findAllUnmatchedRequests();
        if (!unmatchedRequests.isEmpty()) {
            List<String> unmatchedUrls = unmatchedRequests.stream().map(LoggedRequest::getUrl).toList();
            throw new AssertionError(String.format("The following requests was not matched: %s", unmatchedUrls));
        }
    }
}
