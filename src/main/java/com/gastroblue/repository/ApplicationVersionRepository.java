package com.gastroblue.repository;

import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.entity.ApplicationVersionEntity;
import com.gastroblue.model.enums.DevicePlatform;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationVersionRepository
    extends JpaRepository<ApplicationVersionEntity, String> {

  Optional<ApplicationVersionEntity> findByProductAndPlatformAndActiveTrue(
      ApplicationProduct product, DevicePlatform platform);
}
