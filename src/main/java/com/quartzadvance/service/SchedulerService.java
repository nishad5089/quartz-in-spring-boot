package com.quartzadvance.service;

import com.quartzadvance.entity.SchedulerJobInfo;

import java.util.List;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
public interface SchedulerService {

    /**
     * It start All the job schedulers that in the database amd it store job into jobstore temporarily
     */
    void startAllSchedulers();

    /**
     * It Create New Job And Switch job to JobStore from persistence table
     *
     * @param jobInfo it represents Job scheduling information.(Ex: timingInfo, JobName etc)
     */
    void createNewJob(final SchedulerJobInfo jobInfo);

    /**
     * It update the job information and reschedule the information into JobStore
     *
     * @param jobInfo it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     */
    void updateScheduleJob(final SchedulerJobInfo jobInfo);

    /**
     * It takes JobName and removes the job from the JobStore
     *
     * @param jobName Name of the running job which need to be unscheduled
     * @return {@code true} if the job successfully un-schedule,
     * {@code false} if the job don't un-schedule
     */
    boolean unScheduleJob(final String jobName);

    /**
     * loops through all the triggers having a reference to this job, to un-schedule them
     *
     * @param jobName it represent a running job name which need to be stopped
     * @return {@code true} if the job successfully deleted,
     * {@code false} if the job don't delete
     */
    boolean stopJob(final String jobName);

    /**
     * it stop All the Running jobs.
     */
    void stopAllJobs();

    /**
     * It pause the currently running job.
     * Job must have in jobstore
     *
     * @param jobName it represent a running job name which need to be paused.
     * @return {@code true} if the job successfully pause,
     * {@code false} if the job don't pause
     */
    boolean pauseJob(final String jobName);

    /**
     * It resume the pausing jobs and job start running again
     * Job must have in jobstore
     *
     * @param jobName it represent a running job name which need to be resume
     * @return {@code true} if the job successfully resume,
     * {@code false} if the job don't resume
     */
    boolean resumeJob(final String jobName);

    /**
     * if there is needed any Immediate trigger to a particular job then this method can be called
     * Job must have in jobstore for calling this method
     *
     * @param jobName it represent a running job name which need to be trigger instantly.
     * @return {@code true} if the job successfully start,
     * {@code false} if the job don't start
     */
    boolean triggerJobNow(final String jobName);

    /**
     * Add the given job to the Scheduler, if it doesn't already exist.
     *
     * @param jobName it represent the jobName which need to be added.
     * @return {@code true} if the job was actually added,
     * {@code false} if it already existed before
     */
    boolean startJob(final String jobName);

    /**
     * It fetch all the job from jobstore with any group
     * Job must have in jobstore for calling this method
     *
     * @return {@link SchedulerJobInfo} it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     */
    List<SchedulerJobInfo> getAllRunningJobs();

    /**
     * Find single Job by Job Name
     * Job must have in jobstore for calling this method
     *
     * @param jobName it represent a running job name by which we can find Job scheduling information
     * @return {@link SchedulerJobInfo} it represents Job scheduling information(Ex: timingInfo, JobName, jobGroup etc).
     */
    SchedulerJobInfo getRunningJob(final String jobName);

}
