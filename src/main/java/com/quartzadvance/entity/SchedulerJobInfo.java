package com.quartzadvance.entity;

import lombok.Getter;
import lombok.Setter;

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
@Table(name = "scheduler_job_info")
public class SchedulerJobInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;

    private String jobGroup;

    private Class jobClass;

    private String cronExpression;

    private Long repeatTime;

    private int totalFireCount;

    private long initialOffsetMs;

    private Boolean cronJob;

    private boolean runForever;
}
