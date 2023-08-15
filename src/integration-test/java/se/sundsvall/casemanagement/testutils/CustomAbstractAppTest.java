package se.sundsvall.casemanagement.testutils;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.dept44.test.AbstractAppTest;

public abstract class CustomAbstractAppTest extends AbstractAppTest {

	private static final Logger LOG = LoggerFactory.getLogger(CustomAbstractAppTest.class);

	protected Optional<Duration> getVerificationDelay() {
		return Optional.empty();
	}

	@Override
	public boolean verifyAllStubs() {
		getVerificationDelay().ifPresent(verificationDelay -> {
			LOG.info("Waiting {} seconds before verification", verificationDelay.getSeconds());

			try {
				TimeUnit.SECONDS.sleep(verificationDelay.getSeconds());
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
		});

		return super.verifyAllStubs();
	}
}
