package com.gastroblue.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gastroblue.client.ExpoPushClient;
import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.helper.mail.service.IMailService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.commons.shared.enums.Language;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.request.LanguageUpdateRequest;
import com.gastroblue.model.request.TrackerUsersByIdsRequest;
import com.gastroblue.model.response.tracker.TrackerUser;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.service.UserDefinitionService;
import com.gastroblue.service.UserProductService;
import com.gastroblue.util.TestSession;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class TenantChecksTest {

  private final UserDefinitionService userService = mock(UserDefinitionService.class);
  private final UserProductService userProductService = mock(UserProductService.class);
  private final CompanyGroupService companyGroupService = mock(CompanyGroupService.class);
  private final CompanyService companyService = mock(CompanyService.class);
  private final ILookupService lookupService = mock(ILookupService.class, RETURNS_MOCKS);

  private final UserDefinitionFacade userFacade =
      new UserDefinitionFacade(
          userService,
          companyGroupService,
          companyService,
          mock(PasswordEncoder.class),
          lookupService,
          mock(IMailService.class),
          userProductService);

  private final TrackerFacade trackerFacade =
      new TrackerFacade(
          userService,
          userProductService,
          companyService,
          companyGroupService,
          lookupService,
          mock(ExpoPushClient.class));

  @AfterEach
  void logout() {
    TestSession.logout();
  }

  @Test
  void managerCannotToggleAUserOfAnotherGroup() {
    TestSession.login(ApplicationRole.GROUP_MANAGER, "g1", List.of());
    when(userService.findById("u2")).thenReturn(user("u2", "g2"));

    assertThatThrownBy(() -> userFacade.toggleUser("u2")).isInstanceOf(AccessDeniedException.class);
    verify(userProductService, never()).save(any());
  }

  @Test
  void managerCannotChangeTheLanguageOfAUserOfAnotherGroup() {
    TestSession.login(ApplicationRole.GROUP_MANAGER, "g1", List.of());
    when(userService.findById("u2")).thenReturn(user("u2", "g2"));

    assertThatThrownBy(() -> userFacade.updateLanguage("u2", null))
        .isInstanceOf(AccessDeniedException.class);
    verify(userService, never()).updateUser(any());
  }

  @Test
  void managerCannotReadTheCompanyContextOfAUserOfAnotherGroup() {
    TestSession.login(ApplicationRole.GROUP_MANAGER, "g1", List.of());
    when(userService.findById("u2")).thenReturn(user("u2", "g2"));

    assertThatThrownBy(() -> userFacade.findUserCompanyContext("u2"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void usersByIdsLeavesOutOtherGroupsWhenAGroupIsGiven() {
    TestSession.login(ApplicationRole.APP_CLIENT, null, List.of());
    when(userProductService.findByUserIdInAndProduct(any(), any()))
        .thenReturn(List.of(product("u1"), product("u2")));
    when(userService.findAllByIdIn(any())).thenReturn(List.of(user("u1", "g1"), user("u2", "g2")));

    List<TrackerUser> scoped =
        trackerFacade
            .findUsersByIds(new TrackerUsersByIdsRequest(List.of("u1", "u2"), "g1"))
            .getUsers();
    List<TrackerUser> all =
        trackerFacade
            .findUsersByIds(new TrackerUsersByIdsRequest(List.of("u1", "u2"), null))
            .getUsers();

    assertThat(scoped).extracting(TrackerUser::getUserId).containsExactly("u1");
    assertThat(all).extracting(TrackerUser::getUserId).containsExactly("u1", "u2");
  }

  @Test
  void ownLanguageIsAlwaysAllowed() {
    TestSession.login(ApplicationRole.STAFF, "g1", List.of("c1"));
    UserEntity self = user("caller", "g1");
    when(userService.findById("caller")).thenReturn(self);
    when(userProductService.findByUserIdAndProduct(any(), any())).thenReturn(Optional.empty());

    userFacade.updateLanguage("caller", new LanguageUpdateRequest(Language.TR));

    verify(userService).updateUser(self);
  }

  private static UserEntity user(String id, String companyGroupId) {
    UserEntity user =
        UserEntity.builder().companyGroupId(companyGroupId).language(Language.TR).build();
    user.setId(id);
    return user;
  }

  private static UserProductEntity product(String userId) {
    return UserProductEntity.builder()
        .userId(userId)
        .product(ApplicationProduct.TRACKER)
        .applicationRole(ApplicationRole.STAFF)
        .departments("ALL")
        .active(true)
        .build();
  }
}
