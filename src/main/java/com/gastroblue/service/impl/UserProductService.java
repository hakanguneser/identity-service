package com.gastroblue.service.impl;

import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.repository.UserProductRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProductService {

  private final UserProductRepository userProductRepository;

  public UserProductEntity save(UserProductEntity entity) {
    return userProductRepository.save(entity);
  }

  public Optional<UserProductEntity> findByUserIdAndProduct(
      String userId, ApplicationProduct product) {
    return userProductRepository.findByUserIdAndProduct(userId, product);
  }

  public List<UserProductEntity> findByUserIdInAndProduct(
      List<String> userIds, ApplicationProduct product) {
    return userProductRepository.findByUserIdInAndProduct(userIds, product);
  }

  @Transactional
  public void updateLastSuccessLogin(String userId, ApplicationProduct product) {
    userProductRepository.updateLastSuccessLogin(userId, product, LocalDateTime.now());
  }

  @Transactional
  public void updateEulaAcceptedAt(String userId, ApplicationProduct product) {
    userProductRepository.updateEulaAcceptedAt(userId, product, LocalDateTime.now());
  }

  public long countActiveByCompanyIdAndProduct(String companyId, ApplicationProduct product) {
    return userProductRepository.countActiveByCompanyIdAndProduct(companyId, product);
  }
}
