package se.sundsvall.casemanagement.service.scheduler;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class EcosStatusScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(EcosStatusScheduler.class);

	private final EcosStatusWorker worker;
	private final Dept44HealthUtility dept44HealthUtility;
	private final List<String> municipalityIds;
	private final String jobName;

	public EcosStatusScheduler(final EcosStatusWorker worker,
		final Dept44HealthUtility dept44HealthUtility,
		@Value("${scheduler.ecos-status.municipality-ids}") final List<String> municipalityIds,
		@Value("${scheduler.ecos-status.name}") final String jobName) {
		this.worker = worker;
		this.dept44HealthUtility = dept44HealthUtility;
		this.municipalityIds = municipalityIds;
		this.jobName = jobName;
	}

	@Dept44Scheduled(
		cron = "${scheduler.ecos-status.cron}",
		name = "${scheduler.ecos-status.name}",
		lockAtMostFor = "${scheduler.ecos-status.lock-at-most-for}",
		maximumExecutionTime = "${scheduler.ecos-status.maximum-execution-time}")
	public void checkAndUpdateEcosStatus() {
		municipalityIds.forEach(municipalityId -> {
			try {
				worker.updateStatuses(municipalityId);
			} catch (final Exception e) {
				LOG.error("ECOS status update failed for municipality {}: {}", municipalityId, e.getMessage(), e);
				dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, e.getMessage());
			}
		});
	}

}
