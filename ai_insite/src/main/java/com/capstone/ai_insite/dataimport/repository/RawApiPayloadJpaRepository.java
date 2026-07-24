package com.capstone.ai_insite.dataimport.repository;

import com.capstone.ai_insite.dataimport.entity.RawApiPayloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawApiPayloadJpaRepository extends JpaRepository<RawApiPayloadEntity, Long> {
}
