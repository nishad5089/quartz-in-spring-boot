package com.quartzadvance;

import com.quartzadvance.annotations.CronJob;
import com.quartzadvance.annotations.SimpleJob;
import com.quartzadvance.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class Application {
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);
    private static SchedulerService schedulerService;
    private static ApplicationContext applicationContext;
    public Application(SchedulerService schedulerService, ApplicationContext applicationContext) {
        this.schedulerService = schedulerService;
        this.applicationContext = applicationContext;
    }

    public static void main(String[] args) {

      SpringApplication.run(Application.class, args);

        Map<String,Object> aggregates = applicationContext.getBeansWithAnnotation(CronJob.class);
       // Arrays.stream(context.getBeansWithAnnotation(SimpleJob.class)).forEach(LOG::info);
//        Reflections reflections = new Reflections("com.quartzadvance.jobs");
//        Set<Class<?>> cronJobs = reflections.getTypesAnnotatedWith(CronJob.class);
//        Set<Class<?>> simpleJobs = reflections.getTypesAnnotatedWith(SimpleJob.class);
//        SchedulerJobInfo info;
//
//        for (Class<? extends Object> annotatedClass : cronJobs) {
//            Annotation annotation = annotatedClass.getAnnotation(CronJob.class);
//            CronJob myAnnotation = (CronJob) annotation;
////            System.out.println(myAnnotation.job().jobName());
////            System.out.println(myAnnotation.cronExpression());
//
//
//            info = new SchedulerJobInfo()
//                    .setCronJob(myAnnotation.cronJob())
//                    .setCronExpression(myAnnotation.cronExpression())
//                    .setJobClass((Class<? extends QuartzJobBean>) annotatedClass)
//                    .setIsDurable(myAnnotation.job().isDurable())
//                    .setInitialOffsetMs(myAnnotation.job().initialOffsetMs())
//                    .setJobName(myAnnotation.job().jobName())
//                    .setMisFireInstruction(myAnnotation.job().misFireInstruction())
//                    .setJobGroup(myAnnotation.job().jobGroup());
//
//            System.out.println(info);
//            schedulerService.createNewJob(info);
//
//        }
//
//        for (Class<? extends Object> annotatedClass : simpleJobs) {
//            Annotation annotation = annotatedClass.getAnnotation(SimpleJob.class);
//            SimpleJob myAnnotation = (SimpleJob) annotation;
//            System.out.println(myAnnotation.job().jobName());
//            System.out.println(myAnnotation.job().jobGroup());
//
//            info = new SchedulerJobInfo()
//                    .setJobName(myAnnotation.job().jobName())
//                    .setJobGroup(myAnnotation.job().jobGroup())
//                    .setRepeatTime(myAnnotation.repeatTime())
//                    .setRunForever(myAnnotation.runForever())
//                    .setTotalFireCount(myAnnotation.totalFireCount())
//                    .setInitialOffsetMs(myAnnotation.job().initialOffsetMs())
//                    .setJobClass((Class<? extends QuartzJobBean>) annotatedClass)
//                    .setMisFireInstruction(myAnnotation.job().misFireInstruction())
//                    .setIsDurable(myAnnotation.job().isDurable());
//            System.out.println(info);
//            schedulerService.createNewJob(info);
//        }

//        System.out.println("==========");
//        for (Object aggregate : aggregates.values()) {
//            System.out.println("==========");
//            String aggregateType = aggregate.getClass().getCanonicalName();
//            String d = aggregate.getClass().getPackageName();
//            System.out.println(d);
//            System.out.println(aggregateType);
//        }
        System.out.println(schedulerService.getAllAnnotatedBeanByJobType("simpleJob"));
//        System.out.println(schedulerService.getAllJobsByScanningAnnotation("com.quartzadvance.jobs"));
    }
}
