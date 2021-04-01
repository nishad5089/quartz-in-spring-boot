package com.quartzadvance.service.impl;

import com.quartzadvance.service.JobScheduleCreator;
import com.quartzadvance.entity.SchedulerJobInfo;
import com.quartzadvance.repository.SchedulerRepository;
import com.quartzadvance.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@Slf4j
@Transactional
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SchedulerServiceImpl implements SchedulerService {

    private final SchedulerFactoryBean schedulerFactoryBean;

    private final SchedulerRepository schedulerRepository;

    private final JobScheduleCreator scheduleCreator;

    /**
     * It start All the job schedulers that in the database.
     */
    @Override
    public void startAllSchedulers() {
        List<SchedulerJobInfo> jobInfoList = schedulerRepository.findAll();
        if (jobInfoList != null) {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            jobInfoList.forEach(jobInfo -> {
                try {
                    JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                            .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup()).build();
                    if (!scheduler.checkExists(jobDetail.getKey())) {
                        Trigger trigger;
                        jobDetail = scheduleCreator.createJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                                false, jobInfo.getJobName(), jobInfo.getJobGroup());

                        if (jobInfo.getCronJob() && CronExpression.isValidExpression(jobInfo.getCronExpression())) {
                            trigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(System.currentTimeMillis() + 1000),
                                    jobInfo.getCronExpression(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);

                        } else {
                            trigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(System.currentTimeMillis() + 2000),
                                    jobInfo.getRepeatTime(), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                        }
                        scheduler.scheduleJob(jobDetail, trigger);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
                } catch (SchedulerException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    /**
     * It Create New Job And Switch JobStore to persistence table
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName etc)
     */
    @Override
    public void scheduleNewJob(SchedulerJobInfo jobInfo) {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {

                jobDetail = scheduleCreator.createJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()),
                        false, jobInfo.getJobName(), jobInfo.getJobGroup());

                Trigger trigger;
                if (jobInfo.getCronJob()) {
                    trigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(), jobInfo.getCronExpression(),
                            SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                } else {
                    trigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(),
                            SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
                }

                scheduler.scheduleJob(jobDetail, trigger);
                schedulerRepository.save(jobInfo);
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * It Update the job information and reschedule the information into JobStore
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     */
    @Override
    public void updateScheduleJob(SchedulerJobInfo jobInfo) {
        Trigger newTrigger;
        if (jobInfo.getCronJob()) {
            newTrigger = scheduleCreator.createCronTrigger(jobInfo.getJobName(), new Date(), jobInfo.getCronExpression(),
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        } else {
            newTrigger = scheduleCreator.createSimpleTrigger(jobInfo.getJobName(), new Date(), jobInfo.getRepeatTime(),
                    SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
        }
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName()), newTrigger);
            schedulerRepository.save(jobInfo);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * It takes JobName and removes the job from the JobStore
     *
     * @param jobName Name of the running job
     * @return {@link boolean}
     */
    @Override
    public boolean unScheduleJob(String jobName) {
        try {
            return schedulerFactoryBean.getScheduler().unscheduleJob(new TriggerKey(jobName));
        } catch (SchedulerException e) {
            log.error("Failed to un-schedule job - {}", jobName, e);
            return false;
        }
    }

    /**
     * loops through all the triggers having a reference to this job, to un-schedule them
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    @Override
    public boolean deleteJob(SchedulerJobInfo jobInfo) {
        try {
            return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
        } catch (SchedulerException e) {
            log.error("Failed to delete job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * It pause the currently running job.
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    @Override
    public boolean pauseJob(SchedulerJobInfo jobInfo) {
        try {
            schedulerFactoryBean.getScheduler().pauseJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to pause job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * It resume the pausing jobs and job start running again
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    @Override
    public boolean resumeJob(SchedulerJobInfo jobInfo) {
        try {
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * if there is needed any Immediate call/hit of a job then method can be called
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    @Override
    public boolean startJobNow(SchedulerJobInfo jobInfo) {
        try {
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * Initialize JobStore
     */
    @PostConstruct
    public void init() {
        try {
            schedulerFactoryBean.getScheduler().start();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }
    /**
     * Shutdown JobStore
     */
    @Override
    public void shutdownScheduler() {
        try {
            schedulerFactoryBean.getScheduler().shutdown();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }
}
