package com.quartzadvance.controller;

import com.quartzadvance.entity.SchedulerJobInfo;
import com.quartzadvance.jobs.SimpleJob;
import com.quartzadvance.repository.SchedulerRepository;
import com.quartzadvance.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class QuartzController {

    private final SchedulerService schedulerService;

    private final SchedulerRepository repository;

    @GetMapping("runAll")
    public void runAllJobs() {
        schedulerService.startAllSchedulers();
    }

    @PostMapping("create")
    public void createJob() {
        SchedulerJobInfo schedulerJobInfo = new SchedulerJobInfo();
        schedulerJobInfo.setRepeatTime(1000L);
        schedulerJobInfo.setJobName("email send");
        schedulerJobInfo.setJobClass(SimpleJob.class.getName());
        // schedulerJobInfo.setTotalFireCount(10);
        schedulerJobInfo.setRunForever(true);
        schedulerJobInfo.setJobGroup("Test Job3");
        System.out.println(schedulerJobInfo);
        schedulerService.createNewJob(schedulerJobInfo);
    }

    @GetMapping("start/{jobName}")
    public boolean startJob(@PathVariable String jobName) {
        return schedulerService.startJob(jobName);
    }

    @DeleteMapping("stop/{jobName}")
    public Boolean stopJob(@PathVariable String jobName) {
        return schedulerService.stopJob(jobName);
    }

    @GetMapping("pause/{jobName}")
    public Boolean pushJob(@PathVariable String jobName) {
        return schedulerService.pauseJob(jobName);
    }

    @GetMapping("resume/{jobName}")
    public Boolean resumeJob(@PathVariable String jobName) {
        return schedulerService.resumeJob(jobName);
    }

    @GetMapping("triggernow/{jobName}")
    public Boolean triggerJobNow(@PathVariable String jobName) {
        return schedulerService.triggerJobNow(jobName);
    }

    @GetMapping("unschedule/{jobName}")
    public Boolean unScheduleJob(@PathVariable String jobName) {
        return schedulerService.unScheduleJob(jobName);
    }

    @PutMapping("/update/{jobName}")
    public void updateJob(@PathVariable String jobName) {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName(jobName);
        schedulerJobInfo.setRunForever(false);
        schedulerJobInfo.setTotalFireCount(10);
        schedulerJobInfo.setIsDurable(true);
        schedulerService.updateScheduleJob(schedulerJobInfo);
    }

    @GetMapping("runningjobs")
    public List<SchedulerJobInfo> getAllRunningJobs() {
        return schedulerService.getAllRunningJobs();
    }

    @GetMapping("job/{jobName}")
    public SchedulerJobInfo getRunningJobByJobName(@PathVariable String jobName) {
        return schedulerService.getRunningJob(jobName);
    }

    @GetMapping("stopall")
    public void stopAllJobs() {
        schedulerService.stopAllJobs();
    }

    @GetMapping("/get-state/{jobName}")
    public String getJobState(@PathVariable String jobName) {
        return schedulerService.getJobState(jobName);
    }

    @GetMapping("/isRunning/{jobName}")
    public Boolean isJobWithNamePresent(@PathVariable String jobName) {
        return schedulerService.isJobWithNamePresent(jobName);
    }
}
