package com.gastroblue.repository;

import com.gastroblue.model.entity.CompanyGroupEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyGroupRepository extends JpaRepository<CompanyGroupEntity, String> {
  Optional<CompanyGroupEntity> findByGroupCode(String groupCode);
}
