package com.quartzadvance.Enums;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/07/04
 */
public enum Jobs {
    SIMPLE_JOB("SimpleJob"), CRON_JOB("CronJob");
    private String jobType;


    Jobs(String jobType) {
        this.jobType = jobType;
    }

    public String getJobType() {
        return jobType.toLowerCase();
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

}
