package com.quartzadvance.jobs;

import com.quartzadvance.annotations.NishadSchedular;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@Slf4j
@NishadSchedular(jobName = "OTP SMS1", jobGroup = "SMS", jobClass = SimpleJob.class)
public class SimpleJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("SimpleJob Start................11");
//        IntStream.range(0, 10).forEach(i -> {
//            log.info("Counting - {}", i);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                log.error(e.getMessage(), e);
//            }
//        });
//        log.info("SimpleJob End................");
    }
}
