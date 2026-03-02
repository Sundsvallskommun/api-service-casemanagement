package se.sundsvall.casemanagement.service.scheduler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class ByggrStatusScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(ByggrStatusScheduler.class);

	private final ByggrStatusWorker worker;
	private final Dept44HealthUtility dept44HealthUtility;
	private final List<String> municipalityIds;
	private final String jobName;

	public ByggrStatusScheduler(final ByggrStatusWorker worker,
		final Dept44HealthUtility dept44HealthUtility,
		@Value("${scheduler.byggr-status.municipality-ids}") final List<String> municipalityIds,
		@Value("${scheduler.byggr-status.name}") final String jobName) {
		this.worker = worker;
		this.dept44HealthUtility = dept44HealthUtility;
		this.municipalityIds = municipalityIds;
		this.jobName = jobName;
	}

	@Dept44Scheduled(
		cron = "${scheduler.byggr-status.cron}",
		name = "${scheduler.byggr-status.name}",
		lockAtMostFor = "${scheduler.byggr-status.lock-at-most-for}",
		maximumExecutionTime = "${scheduler.byggr-status.maximum-execution-time}")
	public void checkAndUpdateByggrStatus() {
		municipalityIds.forEach(municipalityId -> {
			try {
				worker.updateStatuses(municipalityId);
			} catch (final Exception e) {
				LOG.error("ByggR status update failed for municipality {}: {}", municipalityId, e.getMessage(), e);
				dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, e.getMessage());
			}
		});
	}

}
