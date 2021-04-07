package com.quartzadvance.annotations;

import org.quartz.SimpleTrigger;
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
public @interface SimpleJob {

    String jobName() default "";

    String jobGroup() default "";

    long repeatTime() default 1000L;

    int totalFireCount() default 0;

    long initialOffsetMs() default 1000L;

    boolean runForever() default true;

//    Job job();

    int misFireInstruction() default SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;

    boolean isDurable() default false;
}
