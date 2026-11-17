-- ByggR stores change timestamps in UTC (without zone information). Before this release the ByggR status job
-- compared and stored these values as local time (Europe/Stockholm), so the stored lower bound is one hour behind
-- what has actually been covered. The job now works in UTC; move the stored value forward to match.
--
-- The value is stored as Europe/Stockholm wall time (Hibernate TimeZoneStorageType.NORMALIZE). The offset is one hour
-- while standard time (CET) applies, which is the case when this migration is released.
UPDATE execution_information
SET last_successful_execution = last_successful_execution + INTERVAL 1 HOUR
WHERE job_name = 'BYGGR_STATUS'
  AND last_successful_execution IS NOT NULL;
