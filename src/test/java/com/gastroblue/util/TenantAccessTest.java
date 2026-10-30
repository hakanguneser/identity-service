package com.gastroblue.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantAccessTest {

  @AfterEach
  void logout() {
    TestSession.logout();
  }

  @Test
  void adminReachesEveryGroupAndCompany() {
    TestSession.login(ApplicationRole.ADMIN, null, List.of());

    assertThatCode(() -> TenantAccess.requireCompanyGroup("other")).doesNotThrowAnyException();
    assertThatCode(() -> TenantAccess.requireGroupManagement("other")).doesNotThrowAnyException();
    assertThatCode(() -> TenantAccess.requireCompany("other", "c9")).doesNotThrowAnyException();
    assertThatCode(TenantAccess::requireAdmin).doesNotThrowAnyException();
  }

  @Test
  void groupManagerStaysInItsGroup() {
    TestSession.login(ApplicationRole.GROUP_MANAGER, "g1", List.of());

    assertThatCode(() -> TenantAccess.requireGroupManagement("g1")).doesNotThrowAnyException();
    assertThatCode(() -> TenantAccess.requireCompany("g1", "any")).doesNotThrowAnyException();
    assertThatThrownBy(() -> TenantAccess.requireCompanyGroup("g2"))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> TenantAccess.requireGroupManagement("g2"))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(TenantAccess::requireAdmin).isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void zoneManagerReachesOnlyItsCompanies() {
    TestSession.login(ApplicationRole.ZONE_MANAGER, "g1", List.of("c1"));

    assertThatCode(() -> TenantAccess.requireCompany("g1", "c1")).doesNotThrowAnyException();
    assertThatThrownBy(() -> TenantAccess.requireCompany("g1", "c2"))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> TenantAccess.requireGroupManagement("g1"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void emptyCompanyListMeansNoCompanyForNonGroupWideRoles() {
    SessionUser zoneManager = TestSession.login(ApplicationRole.ZONE_MANAGER, "g1", List.of());

    assertThat(TenantAccess.isGroupWide(zoneManager)).isFalse();
    assertThatThrownBy(() -> TenantAccess.requireCompany("g1", "c1"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void userOfAnotherGroupIsNotAccessible() {
    SessionUser manager = TestSession.login(ApplicationRole.COMPANY_MANAGER, "g1", List.of("c1"));

    assertThatCode(() -> TenantAccess.requireSameCompanyGroup(manager, "g1"))
        .doesNotThrowAnyException();
    assertThatThrownBy(() -> TenantAccess.requireSameCompanyGroup(manager, "g2"))
        .isInstanceOf(AccessDeniedException.class);
    assertThatThrownBy(() -> TenantAccess.requireSameCompanyGroup(manager, null))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void serviceTokenReadsButDoesNotManageUsers() {
    SessionUser client = TestSession.login(ApplicationRole.APP_CLIENT, null, List.of());

    assertThatCode(() -> TenantAccess.requireCompanyGroup("g1")).doesNotThrowAnyException();
    assertThatThrownBy(() -> TenantAccess.requireSameCompanyGroup(client, "g1"))
        .isInstanceOf(AccessDeniedException.class);
  }
}
