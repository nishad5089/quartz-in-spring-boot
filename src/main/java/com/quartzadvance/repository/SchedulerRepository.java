package com.quartzadvance.repository;

import com.quartzadvance.entity.SchedulerJobInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Abdur Rahim Nishad
 * @since 2021/31/03
 */
@Repository
public interface SchedulerRepository extends JpaRepository<SchedulerJobInfo, Long> {
    SchedulerJobInfo findByJobName(String JobName);
}
