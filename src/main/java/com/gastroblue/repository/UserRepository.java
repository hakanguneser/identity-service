package com.gastroblue.repository;

import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ApplicationRole;
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
                    where u.companyGroupId = :companyGroupId
                      and (coalesce(:companyIdList, null) is null  or u.companyId in :companyIdList)
                      and (coalesce(:applicationRoleList, null) is null or u.applicationRole in :applicationRoleList)
                    """)
  List<UserEntity> findAccessibleUsers(
      @Param("companyGroupId") String companyGroupId,
      @Param("companyIdList") List<String> companyIdList,
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
