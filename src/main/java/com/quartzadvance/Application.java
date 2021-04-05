package com.quartzadvance;

import com.quartzadvance.annotations.NishadSchedular;
import com.quartzadvance.entity.SchedulerJobInfo;
import com.quartzadvance.jobs.SimpleJob;
import com.quartzadvance.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.Annotation;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class Application {

    private static SchedulerService schedulerService;

    public Application(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

        Reflections reflections = new Reflections("com.quartzadvance.jobs");
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(NishadSchedular.class);
        SchedulerJobInfo info;
        for (Class<? extends Object> annotatedClass : annotated) {
            Annotation annotation = annotatedClass.getAnnotation(NishadSchedular.class);
            NishadSchedular myAnnotation = (NishadSchedular) annotation;

//            info = new SchedulerJobInfo().setCronJob(myAnnotation.cronJob())
//                    .setCronExpression(myAnnotation.cronExpression())
//                    .setJobClass(myAnnotation.jobClass())
//                    .setIsDurable(myAnnotation.isDurable())
//                    .setJobName(myAnnotation.jobName())
//                    .setJobGroup(myAnnotation.jobGroup())
//                    .setInitialOffsetMs(myAnnotation.initialOffsetMs())
//                    .setMisFireInstruction(myAnnotation.misFireInstruction())
//                    .setRepeatTime(myAnnotation.repeatTime())
//                    .setTotalFireCount(myAnnotation.totalFireCount())
//                    .setRunForever(myAnnotation.runForever());

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
            schedulerService.createNewJob(info);

        }
    }
}
