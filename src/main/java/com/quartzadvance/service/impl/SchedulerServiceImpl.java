package com.quartzadvance.service.impl;

import com.quartzadvance.annotations.NishadSchedular;
import com.quartzadvance.utils.JobScheduleCreator;
import com.quartzadvance.entity.SchedulerJobInfo;
import com.quartzadvance.repository.SchedulerRepository;
import com.quartzadvance.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
                    JobDetail jobDetail = JobBuilder.newJob((Class<? extends QuartzJobBean>) jobInfo.getJobClass())
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
                jobInfo = info;
            }
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            JobDetail jobDetail = JobBuilder.newJob(jobInfo.getJobClass())
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
     * It takes JobName and removes the job from the JobStore
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
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            JobDetail jobDetail = JobBuilder.newJob(jobInfo.getJobClass())
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

    @Override
    public void createJobForAnnotatedBean(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(NishadSchedular.class);
        SchedulerJobInfo info;
        for (Class<?> annotatedClass : annotated) {
            Annotation annotation = annotatedClass.getAnnotation(NishadSchedular.class);
            NishadSchedular myAnnotation = (NishadSchedular) annotation;
            info = new SchedulerJobInfo().setCronJob(myAnnotation.cronJob())
                    .setCronExpression(myAnnotation.cronExpression())
                    .setJobClass(myAnnotation.jobClass())
                    .setIsDurable(myAnnotation.isDurable())
                    .setJobName(myAnnotation.jobName())
                    .setJobGroup(myAnnotation.jobGroup())
                    .setInitialOffsetMs(myAnnotation.initialOffsetMs())
                    .setMisFireInstruction(myAnnotation.misFireInstruction())
                    .setRepeatTime(myAnnotation.repeatTime())
                    .setTotalFireCount(myAnnotation.totalFireCount())
                    .setRunForever(myAnnotation.runForever());

            System.out.println(info);
            this.createNewJob(info);

        }
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
    @PreDestroy
    public void shutdownScheduler() {
        try {
            schedulerFactoryBean.getScheduler().shutdown();
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }
    }
}
