package com.gastroblue.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gastroblue.commons.helper.exception.type.BusinessException;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.helper.mail.service.IMailService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.commons.shared.enums.Language;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.service.UserDefinitionService;
import com.gastroblue.service.UserProductService;
import com.gastroblue.util.TestSession;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

class DefaultDepartmentTest {

  private final UserDefinitionService userService = mock(UserDefinitionService.class);
  private final UserProductService userProductService = mock(UserProductService.class);
  private final CompanyGroupService companyGroupService = mock(CompanyGroupService.class);
  private final CompanyService companyService = mock(CompanyService.class);

  private final UserDefinitionFacade facade =
      new UserDefinitionFacade(
          userService,
          companyGroupService,
          companyService,
          mock(PasswordEncoder.class),
          mock(ILookupService.class, RETURNS_MOCKS),
          mock(IMailService.class),
          userProductService);

  @BeforeEach
  void setUp() {
    TestSession.login(ApplicationRole.ADMIN, null, List.of());
    UserEntity manager = user("caller");
    manager.setEmail("admin@demo.com");
    when(userService.findUserByUserName(any())).thenReturn(manager);
    CompanyGroupEntity group = CompanyGroupEntity.builder().name("Demo Group").build();
    group.setId("g1");
    when(companyGroupService.findByIdOrThrow("g1")).thenReturn(group);
    CompanyEntity company = CompanyEntity.builder().companyName("Demo").build();
    company.setId("c1");
    when(companyService.findByIdOrThrow("c1")).thenReturn(company);
    when(userService.save(any())).thenAnswer(i -> withId(i.getArgument(0)));
    when(userProductService.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  @AfterEach
  void logout() {
    TestSession.logout();
  }

  @Test
  void trackerUserWithoutDepartmentsGetsManagement() {
    facade.saveUser(request(null, ApplicationProduct.TRACKER));

    ArgumentCaptor<UserProductEntity> saved = ArgumentCaptor.forClass(UserProductEntity.class);
    verify(userProductService).save(saved.capture());
    assertThat(saved.getValue().getDepartments()).isEqualTo("MANAGEMENT");
  }

  @Test
  void givenDepartmentsAreKept() {
    facade.saveUser(request(List.of("KITCHEN"), ApplicationProduct.TRACKER));

    ArgumentCaptor<UserProductEntity> saved = ArgumentCaptor.forClass(UserProductEntity.class);
    verify(userProductService).save(saved.capture());
    assertThat(saved.getValue().getDepartments()).isEqualTo("KITCHEN");
  }

  @Test
  void otherProductsStillRequireDepartmentsAndNothingIsSaved() {
    assertThatThrownBy(() -> facade.saveUser(request(List.of(), ApplicationProduct.CHECK)))
        .isInstanceOfSatisfying(
            BusinessException.class,
            e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.DEPARTMENT_REQUIRED.code()));
    verify(userService, never()).save(any());
  }

  private static UserSaveRequest request(List<String> departments, ApplicationProduct product) {
    return new UserSaveRequest(
        "demo.manager",
        "g1",
        "c1",
        ApplicationRole.COMPANY_MANAGER,
        departments,
        "Demo",
        "Manager",
        null,
        "demo.manager@demo.com",
        null,
        null,
        product);
  }

  private static UserEntity user(String id) {
    UserEntity user =
        UserEntity.builder().language(Language.TR).name("Ad").surname("Soyad").build();
    user.setId(id);
    return user;
  }

  private static UserEntity withId(UserEntity entity) {
    entity.setId("new-user");
    return entity;
  }
}
