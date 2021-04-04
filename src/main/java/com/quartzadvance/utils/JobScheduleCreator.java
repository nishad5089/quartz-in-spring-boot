package com.quartzadvance.utils;

import com.quartzadvance.entity.SchedulerJobInfo;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.*;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@Slf4j
@Component
public class JobScheduleCreator {

    /**
     * Create Quartz Job.
     *
     * @param SchedulerJobInfo it represents Job scheduling information.
     * @param isDurable Job needs to be persisted even after completion. if true, job will be persisted, not otherwise.
     * @return JobDetail object
     */
    public JobDetail createJob(SchedulerJobInfo jobInfo, boolean isDurable) throws ClassNotFoundException {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass((Class<? extends QuartzJobBean>) jobInfo.getJobClass());
        factoryBean.setDurability(isDurable);
        factoryBean.setName(jobInfo.getJobName());
        factoryBean.setGroup(jobInfo.getJobGroup());
        // set job data map
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(jobInfo.getJobName(), jobInfo);
        factoryBean.setJobDataMap(jobDataMap);

        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * Create cron trigger.
     *
     * @param triggerName        Trigger name.
     * @param startTime          Trigger start time.
     * @param cronExpression     Cron expression.
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
     * @return {@link CronTrigger}
     */
    public CronTrigger createCronTrigger(String triggerName, Date startTime, String cronExpression, int misFireInstruction) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(misFireInstruction);
        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return factoryBean.getObject();
    }

    /**
     * Create simple trigger.
     *
     * @param triggerName        Trigger name.
     * @param startTime          Trigger start time.
     * @param repeatTime         Job repeat period mills
     * @param misFireInstruction Misfire instruction (what to do in case of misfire happens).
     * @return {@link SimpleTrigger}
     */
    public SimpleTrigger createSimpleTrigger(String triggerName, Date startTime, Long repeatTime, int misFireInstruction) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setStartTime(startTime);
        factoryBean.setRepeatInterval(repeatTime);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        factoryBean.setMisfireInstruction(misFireInstruction);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}
