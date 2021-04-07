package com.quartzadvance.service;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/05/04
 */
@Slf4j
@Component
public class TriggerListner implements org.quartz.TriggerListener {
//    private final SchedulerService schedulerService;

    @Override
    public String getName() {
        return "globalTrigger";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        System.out.println("TriggerListner.triggerFired()");
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        System.out.println("TriggerListner.vetoJobExecution()");
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        System.out.println("TriggerListner.triggerMisfired()");
        String jobName = trigger.getJobKey().getName();
        System.out.println("Job name: " + jobName + " is misfired");
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        log.info("Trigger complete");
    }
}
