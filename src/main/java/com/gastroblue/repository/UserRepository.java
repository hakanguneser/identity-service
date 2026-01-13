package com.gastroblue.repository;

import com.gastroblue.model.base.User;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.enums.Zone;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, String> {
  List<UserEntity> findByCompanyId(String companyId);

  Optional<UserEntity> findByUsername(String username);

  @Query(
      """
    select new com.gastroblue.model.base.User (
        u.id,
        u.companyId,
        u.companyGroupId,
        u.username,
        u.password,
        u.departments,
        u.applicationRole,
        u.email,
        u.language,
        u.active,
        u.name,
        u.surname,
        u.phone,
        u.gender,
        c.companyName,
        cg.name,
        u.zone
    )
    from UserEntity u
    left join CompanyEntity c on u.companyId = c.id
    left join CompanyGroupEntity cg on u.companyGroupId = cg.id
    where (:companyGroupId is null or u.companyGroupId = :companyGroupId)
      and (:companyId is null or u.companyId = :companyId)
      and u.applicationRole in (:applicationRoleList)
""")
  List<User> findCompanyUsersByRole(
      @Param("companyGroupId") String companyGroupId,
      @Param("companyId") String companyId,
      @Param("applicationRoleList") List<ApplicationRole> applicationRoleList);

  @Query(
      """
    select u
    from UserEntity u
    where (:companyGroupId is null or u.companyGroupId = :companyGroupId)
      and (:zoneId is null or u.zone = :zone)
      and (:companyId is null or u.companyId = :companyId)
      and (coalesce(:applicationRoleList, null) is null or u.applicationRole in :applicationRoleList)
    """)
  List<UserEntity> findByCompanyGroupIdAndZoneIdAndCompanyIdAndApplicationRoleIn(
      @Param("companyGroupId") String companyGroupId,
      @Param("zoneId") Zone zone,
      @Param("companyId") String companyId,
      @Param("applicationRoleList") Set<ApplicationRole> applicationRoleList);
}
