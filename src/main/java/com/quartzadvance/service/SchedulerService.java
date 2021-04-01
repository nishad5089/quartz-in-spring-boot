package com.quartzadvance.service;

import com.quartzadvance.entity.SchedulerJobInfo;

import java.util.List;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
public interface SchedulerService {
//    List<SchedulerJobInfo> getAllRunningJobs();
    /**
     * It start All the job schedulers that in the database.
     */
    void startAllSchedulers();

    /**
     * It Create New Job And Switch JobStore to persistence table
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName etc)
     */
    void scheduleNewJob(SchedulerJobInfo jobInfo);

    /**
     * It Update the job information and reschedule the information into JobStore
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     */
    void updateScheduleJob(SchedulerJobInfo jobInfo);

    /**
     * It takes JobName and removes the job from the JobStore
     *
     * @param jobName Name of the running job
     * @return {@link boolean}
     */
    boolean unScheduleJob(String jobName);

    /**
     * loops through all the triggers having a reference to this job, to un-schedule them
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    boolean deleteJob(SchedulerJobInfo jobInfo);

    /**
     * It pause the currently running job.
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    boolean pauseJob(SchedulerJobInfo jobInfo);

    /**
     * It resume the pausing jobs and job start running again
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    boolean resumeJob(SchedulerJobInfo jobInfo);

    /**
     * if there is needed any Immediate call/hit of a job then method can be called
     *
     * @param jobInfo JobInformation(Ex: timingInfo, JobName, jobGroup etc)
     * @return {@link boolean}
     */
    boolean startJobNow(SchedulerJobInfo jobInfo);

    /**
     * Shutdown JobStore
     */
    void shutdownScheduler();


}
