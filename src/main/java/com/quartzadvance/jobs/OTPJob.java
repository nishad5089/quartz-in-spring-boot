package com.quartzadvance.jobs;

import com.quartzadvance.annotations.CronJob;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;


/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@Slf4j
@DisallowConcurrentExecution
//@CronJob(jobName = "OTP SMS", jobGroup = "SMS", cronExpression = "* * * ? * *")
//@CronJob(job = @Job(jobName = "Email", jobGroup = "SMS"), cronExpression = "* * * ? * *")
@CronJob
@Component("oTPJob")
public class OTPJob extends QuartzJobBean implements InterruptableJob {
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

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        System.out.println("Stopping thread... ");
    }
}
