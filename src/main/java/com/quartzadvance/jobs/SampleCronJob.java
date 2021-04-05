package com.quartzadvance.jobs;

import com.quartzadvance.annotations.NishadSchedular;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;


/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@Slf4j
@DisallowConcurrentExecution
//@NishadSchedular(jobName = "OTP SMS", jobGroup = "SMS", jobClass = SampleCronJob.class, cronExpression = "* * * ? * *", cronJob = true)
public class SampleCronJob extends QuartzJobBean {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("SampleCronJob Start................");
//        IntStream.range(0, 59).forEach(i -> {
//            log.info("Counting - {}", i);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                log.error(e.getMessage(), e);
//            }
//        });
//        log.info("SampleCronJob End................");
    }
}
