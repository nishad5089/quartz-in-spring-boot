package com.quartzadvance;

import com.quartzadvance.jobs.SimpleJob;
import com.quartzadvance.service.SchedulerService;
import javassist.ClassPath;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class Application {

  //  private static SchedulerService schedulerService;

//    public Application(SchedulerService schedulerService) {
//        this.schedulerService = schedulerService;
//    }

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

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
        String str = SimpleJob.class.getName();
        System.out.println(str);
        System.out.println("===================");
       // System.out.println(schedulerService.getAllBeanForCronJob("com.quartzadvance.jobs"));
       // System.out.println(schedulerService.getPackage("com.quartzadvance.jobs.SimpleJob"));


      //  schedulerService.createJobForAnnotatedBean("com.quartzadvance.jobs");
    }
}
