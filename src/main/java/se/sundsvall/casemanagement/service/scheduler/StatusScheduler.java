package se.sundsvall.casemanagement.service.scheduler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class StatusScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(StatusScheduler.class);

	private final StatusSchedulerWorker worker;
	private final Dept44HealthUtility dept44HealthUtility;
	private final List<String> municipalityIds;
	private final String jobName;

	public StatusScheduler(final StatusSchedulerWorker worker,
		final Dept44HealthUtility dept44HealthUtility,
		@Value("${scheduler.status.municipality-ids}") final List<String> municipalityIds,
		@Value("${scheduler.status.name}") final String jobName) {
		this.worker = worker;
		this.dept44HealthUtility = dept44HealthUtility;
		this.municipalityIds = municipalityIds;
		this.jobName = jobName;
	}

	@Dept44Scheduled(
		cron = "${scheduler.status.cron}",
		name = "${scheduler.status.name}",
		lockAtMostFor = "${scheduler.status.lock-at-most-for}",
		maximumExecutionTime = "${scheduler.status.maximum-execution-time}")
	public void checkAndUpdateStatus() {
		municipalityIds.forEach(municipalityId -> {
			try {
				worker.updateStatuses(municipalityId);
			} catch (final Exception e) {
				LOG.error("Status update failed for municipality {}: {}", municipalityId, e.getMessage(), e);
				dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, e.getMessage());
			}
		});
	}

}
