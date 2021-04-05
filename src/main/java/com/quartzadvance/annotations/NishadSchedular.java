package com.quartzadvance.annotations;

import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.lang.annotation.*;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/04/04
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NishadSchedular {
    String jobName();

    String jobGroup();

    Class<? extends QuartzJobBean> jobClass();

    String cronExpression() default "";

    boolean cronJob() default false;

    long repeatTime() default 1000L;

    int totalFireCount() default 0;

    long initialOffsetMs() default 1000L;


    boolean runForever() default true;

    int misFireInstruction() default SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;

    boolean isDurable() default false;
}
