package com.capstone.ai_insite.metric.repository;

import com.capstone.ai_insite.metric.entity.AbstractSeoulRegionalSourceEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SeoulRegionalSourceJpaRepository<
    E extends AbstractSeoulRegionalSourceEntity<?>
> extends JpaRepository<E, Long> {

    List<E> findAllByMetricPeriodId(Long metricPeriodId);

    List<E> findByMetricPeriodIdAndRegionIdIn(
        Long metricPeriodId,
        Collection<Long> regionIds
    );
}
