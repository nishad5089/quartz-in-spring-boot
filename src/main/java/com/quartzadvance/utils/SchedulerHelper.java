package com.quartzadvance.utils;

import com.google.common.base.CaseFormat;
import org.springframework.context.ApplicationContext;

import java.util.HashSet;
import java.util.Set;


/**
 * @author Abdur Rahim Nishad
 * @since 2021/07/04
 */

public class SchedulerHelper {

    public static String getFullClassPathOfBean(String beanName, ApplicationContext context) {
        String str = CaseFormat.UPPER_CAMEL.converterTo(CaseFormat.LOWER_CAMEL).convert(beanName);
        return context.getBean(str).getClass().getCanonicalName();
    }

    /**
     * it marge two set and return new set
     *
     * @param simpleJobs
     * @param cronJobs
     * @param <T>        Types of Data
     * @return {@link <T>}
     */
    public static <T> Set<T> mergeSet(Set<T> simpleJobs, Set<T> cronJobs) {
        return new HashSet<T>() {
            {
                addAll(simpleJobs);
                addAll(cronJobs);
            }
        };
    }

}
