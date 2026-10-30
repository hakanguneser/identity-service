package com.gastroblue.util;

import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.model.enums.ErrorCode;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Tenant and role checks shared by the facades.
 *
 * <p>ADMIN and APP_CLIENT (service token) are not bound to a company group. Every other role works
 * only inside its own company group; ZONE_MANAGER and below only on the companies in their token.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TenantAccess {

  /** ADMIN or the service token: not limited to one company group. */
  public static boolean isUnbound(SessionUser sessionUser) {
    ApplicationRole role = sessionUser.getApplicationRole();
    return role == ApplicationRole.ADMIN || role == ApplicationRole.APP_CLIENT;
  }

  /** Roles that see every company of their group (token company list is empty by design). */
  public static boolean isGroupWide(SessionUser sessionUser) {
    return isUnbound(sessionUser)
        || sessionUser.getApplicationRole() == ApplicationRole.GROUP_MANAGER;
  }

  public static SessionUser requireAdmin() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    if (sessionUser.getApplicationRole() != ApplicationRole.ADMIN) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED, "Only ADMIN can do this");
    }
    return sessionUser;
  }

  /** Read access: the caller's own company group, unless unbound. */
  public static SessionUser requireCompanyGroup(String companyGroupId) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    if (!isUnbound(sessionUser)
        && (companyGroupId == null
            || !Objects.equals(companyGroupId, sessionUser.companyGroupId()))) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Company group not accessible: " + companyGroupId);
    }
    return sessionUser;
  }

  /** Group level changes (group settings, companies, EULA): ADMIN or the group's GROUP_MANAGER. */
  public static SessionUser requireGroupManagement(String companyGroupId) {
    SessionUser sessionUser = requireCompanyGroup(companyGroupId);
    ApplicationRole role = sessionUser.getApplicationRole();
    if (role != ApplicationRole.ADMIN && role != ApplicationRole.GROUP_MANAGER) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Role " + role + " cannot manage company group settings");
    }
    return sessionUser;
  }

  /**
   * A company of the group: group-wide roles reach every company, the others only the companies in
   * their token.
   */
  public static SessionUser requireCompany(String companyGroupId, String companyId) {
    SessionUser sessionUser = requireCompanyGroup(companyGroupId);
    if (!isGroupWide(sessionUser) && !sessionUser.getCompanyIdsOrEmptyList().contains(companyId)) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Company not accessible: " + companyId);
    }
    return sessionUser;
  }

  /**
   * The target user belongs to the caller's company group. Only ADMIN manages users of every group;
   * the service token has no business with user definitions.
   */
  public static void requireSameCompanyGroup(SessionUser sessionUser, String userCompanyGroupId) {
    if (sessionUser.getApplicationRole() != ApplicationRole.ADMIN
        && (userCompanyGroupId == null
            || !Objects.equals(userCompanyGroupId, sessionUser.companyGroupId()))) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED, "User not accessible");
    }
  }
}
