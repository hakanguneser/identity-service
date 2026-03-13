package com.gastroblue.repository;

import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProductRepository extends JpaRepository<UserProductEntity, String> {

  Optional<UserProductEntity> findByUserIdAndProduct(String userId, ApplicationProduct product);

  List<UserProductEntity> findByUserIdInAndProduct(List<String> userIds, ApplicationProduct product);

  @Modifying
  @Query(
      """
      update UserProductEntity up
      set up.lastSuccessLogin = :now
      where up.userId = :userId and up.product = :product
      """)
  void updateLastSuccessLogin(
      @Param("userId") String userId,
      @Param("product") ApplicationProduct product,
      @Param("now") LocalDateTime now);

  @Modifying
  @Query(
      """
      update UserProductEntity up
      set up.eulaAcceptedAt = :now
      where up.userId = :userId and up.product = :product
      """)
  void updateEulaAcceptedAt(
      @Param("userId") String userId,
      @Param("product") ApplicationProduct product,
      @Param("now") LocalDateTime now);
}
