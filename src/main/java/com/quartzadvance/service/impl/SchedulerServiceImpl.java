package com.quartzadvance.service.impl;

import com.quartzadvance.Enums.Jobs;
import com.quartzadvance.annotations.CronJob;
import com.quartzadvance.annotations.SimpleJob;
import com.quartzadvance.entity.SchedulerJobInfo;
import com.quartzadvance.repository.SchedulerRepository;
import com.quartzadvance.service.SchedulerService;
import com.quartzadvance.utils.JobScheduleCreator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.reflections.Reflections;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

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

    private final ApplicationContext applicationContext;

    /**
     * It start All the job schedulers that in the database amd it store job into jobstore temporarily
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
                        this.configureSchedule(jobInfo, scheduler, jobDetail);
                    }
                    log.info("Job already exist");
                } catch (ClassNotFoundException e) {
                    log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
                } catch (SchedulerException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    /**
     * It Create New Job And Switch job to JobStore from persistence table
     *
     * @param jobInfo it represents Job scheduling information.(Ex: timingInfo, JobName etc)
     */
    @Override
    public void createNewJob(SchedulerJobInfo jobInfo) {
        try {
            SchedulerJobInfo info = schedulerRepository.findByJobName(jobInfo.getJobName());
            if (info != null) {
                log.info("This Job Already Exists in Database");
                //  jobInfo = info;
            }
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup())
                    .build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                this.configureSchedule(jobInfo, scheduler, jobDetail);
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
     * It update the job information and reschedule the information into JobStore
     *
     * @param jobInfo it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     */
    @Override
    public void updateScheduleJob(final SchedulerJobInfo jobInfo) {
        Trigger newTrigger = configureTrigger(jobInfo);
        try {
            schedulerFactoryBean.getScheduler().rescheduleJob(TriggerKey.triggerKey(jobInfo.getJobName()), newTrigger);
            schedulerRepository.save(jobInfo);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Remove the indicated Trigger from the scheduler.
     * If the related job does not have any other triggers, and the job is not durable, then the job will also be deleted.
     *
     * @param jobName Name of the running job which need to be unscheduled
     * @return {@code true} if the job successfully un-schedule,
     * {@code false} if the job don't un-schedule
     */
    @Override
    public boolean unScheduleJob(final String jobName) {
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
     * @param jobName it represent a running job name which need to be stopped
     * @return {@code true} if the job successfully deleted,
     * {@code false} if the job don't delete
     */
    @Override
    public boolean stopJob(final String jobName) {
        final SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
        try {
            return schedulerFactoryBean.getScheduler().deleteJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
        } catch (SchedulerException e) {
            log.error("Failed to delete job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * It pause the currently running job.
     * Job must have in jobstore
     *
     * @param jobName it represent a running job name which need to be paused.
     * @return {@code true} if the job successfully pause,
     * {@code false} if the job don't pause
     */
    @Override
    public boolean pauseJob(final String jobName) {
        final SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
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
     * Job must have in jobstore
     *
     * @param jobName it represent a running job name which need to be resume
     * @return {@code true} if the job successfully resume,
     * {@code false} if the job don't resume
     */
    @Override
    public boolean resumeJob(final String jobName) {
        final SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
        try {
            schedulerFactoryBean.getScheduler().resumeJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * if there is needed any Immediate trigger to a particular job then this method can be called
     * Job must have in jobstore for calling this method
     *
     * @param jobName it represent a running job name which need to be trigger instantly.
     * @return {@code true} if the job successfully start,
     * {@code false} if the job don't start
     */
    @Override
    public boolean triggerJobNow(final String jobName) {
        final SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
        try {
            schedulerFactoryBean.getScheduler().triggerJob(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to start new job - {}", jobInfo.getJobName(), e);
            return false;
        }
    }

    /**
     * It fetch all the job from jobstore with any group
     * Job must have in jobstore for calling this method
     *
     * @return {@link SchedulerJobInfo} it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     */
    @Override
    public List<SchedulerJobInfo> getAllRunningJobs() {
        try {
            return schedulerFactoryBean.getScheduler().getJobKeys(GroupMatcher.anyGroup())
                    .stream()
                    .map(jobKey -> {
                        try {
                            final JobDetail jobDetail = schedulerFactoryBean.getScheduler().getJobDetail(jobKey);
                            return (SchedulerJobInfo) jobDetail.getJobDataMap().get(jobKey.getName());
                        } catch (final SchedulerException e) {
                            log.error(e.getMessage(), e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (final SchedulerException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Find single Job by Job Name
     * Job must have in jobstore for calling this method
     *
     * @param jobName it represent a running job name by which we can find Job scheduling information
     * @return {@link SchedulerJobInfo} it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     */
    @Override
    public SchedulerJobInfo getRunningJob(final String jobName) {
        try {
            SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
            final JobDetail jobDetail = schedulerFactoryBean.getScheduler().getJobDetail(new JobKey(jobInfo.getJobName(), jobInfo.getJobGroup()));
            if (jobDetail == null) {
                log.error("Failed to find timer with ID '{}'", jobName);
                return null;
            }
            return (SchedulerJobInfo) jobDetail.getJobDataMap().get(jobName);
        } catch (final SchedulerException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Add the given job to the Scheduler, if it doesn't already exist.
     *
     * @param jobName it represent the jobName which need to be added.
     * @return {@code true} if the job was actually added,
     * {@code false} if it already existed before
     */

    @Override
    public boolean startJob(final String jobName) {
        SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
        if (jobInfo == null) {
            return false;
        }
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup()).build();
            if (!scheduler.checkExists(jobDetail.getKey())) {
                configureSchedule(jobInfo, scheduler, jobDetail);
                return true;
            } else {
                log.error("scheduleNewJobRequest.jobAlreadyExist");
                return false;
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * it stop All the Running jobs.
     */
    @Override
    public void stopAllJobs() {
        try {
            schedulerFactoryBean.getScheduler().getJobKeys(GroupMatcher.anyGroup()).forEach(res -> {
                try {
                    log.info("job stopping......");
                    schedulerFactoryBean.getScheduler().deleteJob(res);

                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            });
        } catch (final SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Check job exist with given name
     *
     * @param jobName jobName it represent the jobName is running or not.
     * @return {@code true} if the job is running.
     * {@code false} if the job is not running.
     */
    @Override
    public boolean isJobWithNamePresent(String jobName) {
        SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
        if (jobInfo == null) {
            return false;
        }
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup()).build();
            if (scheduler.checkExists(jobDetail.getKey())) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while checking job with name and group exist:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get the current state of job
     *
     * @param jobName jobName it represent the jobName which status need to be checked.
     * @return {@link String}
     */
    public String getJobState(String jobName) {
        SchedulerJobInfo jobInfo = schedulerRepository.findByJobName(jobName);
        if (jobInfo == null) {
            return "There is no job in Database named {} " + jobName;
        }
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) Class.forName(jobInfo.getJobClass()))
                    .withIdentity(jobInfo.getJobName(), jobInfo.getJobGroup()).build();

            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
            if (triggers != null && triggers.size() > 0) {
                for (Trigger trigger : triggers) {
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

                    if (Trigger.TriggerState.PAUSED.equals(triggerState)) {
                        return "PAUSED";
                    } else if (Trigger.TriggerState.BLOCKED.equals(triggerState)) {
                        return "BLOCKED";
                    } else if (Trigger.TriggerState.COMPLETE.equals(triggerState)) {
                        return "COMPLETE";
                    } else if (Trigger.TriggerState.ERROR.equals(triggerState)) {
                        return "ERROR";
                    } else if (Trigger.TriggerState.NONE.equals(triggerState)) {
                        return "NONE";
                    } else if (Trigger.TriggerState.NORMAL.equals(triggerState)) {
                        return "SCHEDULED";
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.error("Class Not Found - {}", jobInfo.getJobClass(), e);
        } catch (SchedulerException e) {
            System.out.println("SchedulerException while checking job with name and group exist:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Find All Jobs that are annotated with SimpleJob or CronJob by Jobtype
     *
     * @param jobType The type of the Job. Job can have two type SimpleJob or CronJob
     * @return {@link Set<String>}
     */

    @Override
    public Set<String> getAllAnnotatedBeanByJobType(String jobType) {
        if (Jobs.SIMPLE_JOB.getJobType().equals(jobType.toLowerCase())) {
            Set<String> simpleJobsSet = getSimpleJobs();
            return simpleJobsSet;
        } else {
            Set<String> cronJobsSet = getCronJobs();
            return cronJobsSet;
        }
    }

    /**
     * Create Job For Annotated Bean
     */
    public void createJobForAnnotatedBean() {
        Map<String, Object> simpleJobs = applicationContext.getBeansWithAnnotation(SimpleJob.class);
        Map<String, Object> cronJobs = applicationContext.getBeansWithAnnotation(CronJob.class);
        SchedulerJobInfo info;

        for (Object simpleJob : simpleJobs.values()) {
            Class<?> annotatedClass = simpleJob.getClass();
            Annotation annotation = annotatedClass.getAnnotation(SimpleJob.class);
            SimpleJob simpleJobAnnotation = (SimpleJob) annotation;
            info = new SchedulerJobInfo()
                    .setJobName(simpleJobAnnotation.jobName())
                    .setJobGroup(simpleJobAnnotation.jobGroup())
                    .setRepeatTime(simpleJobAnnotation.repeatTime())
                    .setRunForever(simpleJobAnnotation.runForever())
                    .setTotalFireCount(simpleJobAnnotation.totalFireCount())
                    .setInitialOffsetMs(simpleJobAnnotation.initialOffsetMs())
                    .setJobClass(annotatedClass.getCanonicalName())
                    .setMisFireInstruction(simpleJobAnnotation.misFireInstruction())
                    .setIsDurable(simpleJobAnnotation.isDurable());
            System.out.println(info);
            this.createNewJob(info);
        }

        for (Object cronJob : cronJobs.values()) {
            Class<?> annotatedClass = cronJob.getClass();
            Annotation annotation = annotatedClass.getAnnotation(CronJob.class);
            CronJob cronJobAnnotation = (CronJob) annotation;
            info = new SchedulerJobInfo()
                    .setCronJob(cronJobAnnotation.cronJob())
                    .setCronExpression(cronJobAnnotation.cronExpression())
                    .setJobClass(annotatedClass.getCanonicalName())
                    .setIsDurable(cronJobAnnotation.isDurable())
                    .setInitialOffsetMs(cronJobAnnotation.initialOffsetMs())
                    .setJobName(cronJobAnnotation.jobName())
                    .setMisFireInstruction(cronJobAnnotation.misFireInstruction())
                    .setJobGroup(cronJobAnnotation.jobGroup());

            System.out.println(info);
            this.createNewJob(info);

        }

    }


    /**
     * Get All Job that are annotated with SimpleJob or CronJob
     *
     * @return {@link Set<String>}
     */
    @Override
    public Set<String> getAllJobsByScanningAnnotation() {
        Set<String> cronJobs = getCronJobs();
        Set<String> simpleJobs = getSimpleJobs();
        return mergeSet(cronJobs, simpleJobs);
    }

    /**
     * it marge two set and return new set
     *
     * @param simpleJobs
     * @param cronJobs
     * @param <T>        Types of Data
     * @return
     */
    private <T> Set<T> mergeSet(Set<T> simpleJobs, Set<T> cronJobs) {
        return new HashSet<T>() {
            {
                addAll(simpleJobs);
                addAll(cronJobs);
            }
        };
    }

    private Set<String> getCronJobs() {
        Set<String> cronJobsSet = new HashSet<>();
        Map<String, Object> cronJobs = applicationContext.getBeansWithAnnotation(CronJob.class);
        for (Object cronJob : cronJobs.values()) {
            String aggregateType = cronJob.getClass().getCanonicalName();
            cronJobsSet.add(aggregateType);
        }
        return cronJobsSet;
    }

    private Set<String> getSimpleJobs() {
        Set<String> simpleJobsSet = new HashSet<>();
        Map<String, Object> simpleJobs = applicationContext.getBeansWithAnnotation(SimpleJob.class);
        for (Object simpleJob : simpleJobs.values()) {
            String aggregateType = simpleJob.getClass().getCanonicalName();
            simpleJobsSet.add(aggregateType);
        }
        return simpleJobsSet;
    }

    /**
     * configuring schedule for executing jobs and store it into jobstore
     *
     * @param jobInfo   it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     * @param scheduler it tell quartz to schedule the job using specific trigger
     * @param jobDetail Quartz does not store an actual instance of a Job class, but instead allows you to define an instance of one, through the use of a JobDetail.
     * @throws ClassNotFoundException
     * @throws SchedulerException
     */
    private void configureSchedule(SchedulerJobInfo jobInfo, Scheduler scheduler, JobDetail jobDetail) throws ClassNotFoundException, SchedulerException {
        jobDetail = scheduleCreator.createJob(jobInfo);
        Trigger trigger = configureTrigger(jobInfo);
        log.info("Starting Job.....");
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Trigger configuration for job
     *
     * @param jobInfo it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     * @return {@link Trigger}
     */
    private Trigger configureTrigger(SchedulerJobInfo jobInfo) {
        Trigger trigger;
        if (jobInfo.getCronJob()) {
            trigger = scheduleCreator.createCronTrigger(jobInfo);
        } else {
            trigger = scheduleCreator.createSimpleTrigger(jobInfo);
        }
        return trigger;
    }
}
