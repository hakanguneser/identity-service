package com.gastroblue.service;

import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.entity.ApplicationVersionEntity;
import com.gastroblue.model.enums.DevicePlatform;
import com.gastroblue.repository.ApplicationVersionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationVersionService {

  private final ApplicationVersionRepository applicationVersionRepository;

  @Transactional(readOnly = true)
  public Optional<ApplicationVersionEntity> findActive(
      ApplicationProduct product, DevicePlatform platform) {
    return applicationVersionRepository.findByProductAndPlatformAndActiveTrue(product, platform);
  }
}
