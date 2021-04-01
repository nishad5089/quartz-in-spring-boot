package com.quartzadvance;

import com.quartzadvance.service.SchedulerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class QuartzadvanceApplication {
//    @Autowired
//    private static SchedulerService schedulerService;

    public static void main(String[] args) throws Exception{

        SpringApplication.run(QuartzadvanceApplication.class, args);
//        log.info("Schedule all new scheduler jobs at app startup - starting");
//        try {
//            schedulerService.startAllSchedulers();
//            log.info("Schedule all new scheduler jobs at app startup - complete");
//        } catch (Exception ex) {
//            log.error("Schedule all new scheduler jobs at app startup - error", ex);
//        }
    }
}
