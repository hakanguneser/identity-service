package com.gastroblue.repository;

import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.enums.Zone;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, String> {

  Optional<UserEntity> findByUsername(String username);

  @Query(
      """
                    select u
                    from UserEntity u
                    where (:companyGroupId is null or u.companyGroupId = :companyGroupId)
                      and (:zone is null or u.zone = :zone)
                      and (:companyId is null or u.companyId = :companyId)
                      and (coalesce(:applicationRoleList, null) is null or u.applicationRole in :applicationRoleList)
                    """)
  List<UserEntity> findByCompanyGroupIdAndZoneIdAndCompanyIdAndApplicationRoleIn(
      @Param("companyGroupId") String companyGroupId,
      @Param("zone") Zone zone,
      @Param("companyId") String companyId,
      @Param("applicationRoleList") Set<ApplicationRole> applicationRoleList);

  @Modifying
  @Query(
      """
                    update UserEntity u
                    set u.lastSuccessLogin = :now,
                        u.lastSuccessLoginProduct = :lastSuccessLoginProduct,
                        u.passwordChangeRequired = case when (u.passwordExpiresAt is null or u.passwordExpiresAt < :now) then true else u.passwordChangeRequired end
                    where u.username = :username
                    """)
  void updateUserAfterSuccessfulLogin(
      @Param("username") String username,
      @Param("lastSuccessLoginProduct") ApplicationProduct lastSuccessLoginProduct,
      @Param("now") LocalDateTime now);
}
