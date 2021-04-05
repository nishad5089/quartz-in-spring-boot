package com.quartzadvance.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.quartz.SimpleTrigger;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.persistence.*;
import java.io.Serializable;

/**
 * SchedulerJobInfo represent the timing-info and initial meta data for a job
 *
 * @author Abdur Rahim Nishad
 * @version 0.1
 */
@Getter
@Setter
@Entity
@ToString
@Accessors(chain = true)
@Table(name = "scheduler_job_info")
public class SchedulerJobInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String jobName;

    private String jobGroup;

    private Class<? extends QuartzJobBean> jobClass;

    private String cronExpression;

    private Long repeatTime;

    private Integer totalFireCount;

    @Column(nullable = false)
    private Long initialOffsetMs = 1000L;

    private Boolean cronJob = false;

    @Column(nullable = false)
    private Boolean runForever = true;

    @Column(nullable = false)
    private Integer misFireInstruction = SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW;

    private Boolean isDurable = false;
}
