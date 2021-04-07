package com.quartzadvance.annotations;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/05/04
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface CronJob {

    String jobName() default "";

    String jobGroup() default "";

    String cronExpression() default "";

    boolean cronJob() default true;

    // Job job();

    long initialOffsetMs() default 1000L;

    int misFireInstruction() default CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY;

    boolean isDurable() default false;
}
