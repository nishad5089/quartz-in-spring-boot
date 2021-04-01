package com.quartzadvance.controller;

import com.quartzadvance.entity.SchedulerJobInfo;
import com.quartzadvance.repository.SchedulerRepository;
import com.quartzadvance.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("runAll")
    public void runAllJobs() {
        schedulerService.startAllSchedulers();
    }

    @PostMapping("create")
    public void createJob() {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName("jkjkj");
//        schedulerJobInfo.setRepeatTime(1000L);
//        schedulerJobInfo.setJobName("jkjkj");
//        schedulerJobInfo.setJobClass("com.quartzadvance.jobs.SimpleJob");
//        schedulerJobInfo.setCronJob(false);
//        schedulerJobInfo.setJobGroup("Test Job3");
        schedulerService.scheduleNewJob(schedulerJobInfo);
    }

    @DeleteMapping("delete/{jobName}")
    public Boolean deleteJob(@PathVariable String jobName) {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName(jobName);
        return schedulerService.deleteJob(schedulerJobInfo);
    }

    @GetMapping("pause/{jobName}")
    public Boolean pushJob(@PathVariable String jobName) {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName(jobName);
        return schedulerService.pauseJob(schedulerJobInfo);
    }

    @GetMapping("resume/{jobName}")
    public Boolean resumeJob(@PathVariable String jobName) {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName(jobName);
        return schedulerService.resumeJob(schedulerJobInfo);
    }

    @GetMapping("start/{jobName}")
    public Boolean startJob(@PathVariable String jobName) {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName(jobName);
        return schedulerService.startJobNow(schedulerJobInfo);
    }

    @GetMapping("unschedule/{jobName}")
    public Boolean unScheduleJob(@PathVariable String jobName) {
        return schedulerService.unScheduleJob(jobName);
    }

    @PutMapping("/update/{jobName}")
    public void updateJob(@PathVariable String jobName) {
        SchedulerJobInfo schedulerJobInfo = repository.findByJobName(jobName);
        schedulerJobInfo.setRepeatTime(10000L);
        schedulerService.updateScheduleJob(schedulerJobInfo);
    }

    @GetMapping("destroyAllJob")
    public void destroy() {
        schedulerService.shutdownScheduler();
    }
}
