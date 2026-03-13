package com.gastroblue.facade;

import static com.gastroblue.model.enums.MailParameters.*;
import static com.gastroblue.model.enums.MailTemplate.INITIAL_PASSWORD;
import static com.gastroblue.model.enums.MailTemplate.RESET_PASSWORD;
import static com.gastroblue.model.enums.ProductRole.*;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.ValidationException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.LanguageUpdateRequest;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.IMailService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.service.impl.UserDefinitionService;
import com.gastroblue.service.impl.UserProductService;
import com.gastroblue.util.DelimitedStringUtil;
import com.gastroblue.util.PasswordGenerator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDefinitionFacade {

  @Value("${app.admin.registration.enabled}")
  private boolean adminRegistrationEnabled;

  private final UserDefinitionService userService;
  private final CompanyGroupService companyGroupService;
  private final CompanyService companyService;
  private final PasswordEncoder passwordEncoder;
  private final EnumConfigurationFacade enumFacade;
  private final IMailService mailService;
  private final UserProductService userProductService;

  public UserDefinitionResponse findUserById(String userId) {
    UserEntity userEntity = userService.findById(userId);
    UserProductEntity userProduct = resolveUserProduct(userId);
    return UserMapper.toResponse(userEntity, userProduct, enumFacade);
  }

  public UserDefinitionResponse updateUser(final String userId, final UserUpdateRequest request) {
    UserEntity existingEntity = userService.findById(userId);
    UserEntity entityTobeUpdated = UserMapper.updateEntity(existingEntity, request);
    UserEntity updatedEntity = userService.updateUser(entityTobeUpdated);
    UserProductEntity userProduct = resolveUserProduct(userId);
    return UserMapper.toResponse(updatedEntity, userProduct, enumFacade);
  }

  public List<UserDefinitionResponse> findAccessibleUsers(boolean includeAll) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    ApplicationProduct product = sessionUser.getApplicationProduct();

    Set<ProductRole> targetRoles;
    if (sessionUser.isAdmin()) {
      targetRoles =
          includeAll
              ? Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF)
              : Set.of(GROUP_MANAGER);
    } else {
      ProductRole productRole = sessionUser.getProductRole();
      if (productRole == null) return List.of();
      if (includeAll) {
        targetRoles =
            switch (productRole) {
              case GROUP_MANAGER -> Set.of(ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
              case ZONE_MANAGER -> Set.of(COMPANY_MANAGER, SUPERVISOR, STAFF);
              case COMPANY_MANAGER -> Set.of(SUPERVISOR, STAFF);
              case SUPERVISOR -> Set.of(STAFF);
              default -> Set.of();
            };
      } else {
        targetRoles =
            switch (productRole) {
              case GROUP_MANAGER -> Set.of(COMPANY_MANAGER, ZONE_MANAGER);
              case ZONE_MANAGER -> Set.of(COMPANY_MANAGER);
              case COMPANY_MANAGER -> Set.of(SUPERVISOR);
              case SUPERVISOR -> Set.of(STAFF);
              default -> Set.of();
            };
      }
    }

    List<UserEntity> users = userService.findAccessibleUser(targetRoles, product);
    List<String> userIds = users.stream().map(UserEntity::getId).toList();
    Map<String, UserProductEntity> userProductMap =
        product != null
            ? userProductService.findByUserIdInAndProduct(userIds, product).stream()
                .collect(Collectors.toMap(UserProductEntity::getUserId, up -> up))
            : Map.of();

    List<Department> sessionDepartments = sessionUser.getDepartments();
    return users.stream()
        .map(u -> UserMapper.toResponse(u, userProductMap.get(u.getId()), enumFacade))
        .filter(
            user -> {
              if (sessionDepartments == null || sessionDepartments.contains(Department.ALL)) {
                return true;
              }
              return user.getDepartmentsList().stream().anyMatch(sessionDepartments::contains);
            })
        .toList();
  }

  public UserDefinitionResponse saveUser(UserSaveRequest request) {
    UserEntity managerUser = checkRegisteredUserRole(request);
    CompanyGroupEntity companyGroup = getRegistrationCompanyGroup(request);
    CompanyEntity company = getRegistrationCompany(request);
    String generatedPassword = PasswordGenerator.generate();
    UserEntity entityToBeSaved =
        UserMapper.toEntity(
            companyGroup.getId(),
            company.getId(),
            request,
            passwordEncoder.encode(generatedPassword));
    UserEntity savedUserEntity = userService.save(entityToBeSaved);

    UserProductEntity savedUserProduct = null;
    if (request.product() != null && request.productRole() != null) {
      UserProductEntity userProduct =
          UserProductEntity.builder()
              .userId(savedUserEntity.getId())
              .product(request.product())
              .productRole(request.productRole())
              .departments(DelimitedStringUtil.join(getDepartments(request)))
              .build();
      savedUserProduct = userProductService.save(userProduct);
    }

    notifyNewPassword(
        INITIAL_PASSWORD,
        savedUserEntity,
        savedUserProduct,
        managerUser,
        generatedPassword,
        companyGroup.getName(),
        company.getCompanyName());
    return UserMapper.toResponse(savedUserEntity, savedUserProduct, enumFacade);
  }

  private List<Department> getDepartments(UserSaveRequest request) {
    List<Department> departments = Optional.ofNullable(request.departments()).orElse(List.of());
    if (departments.contains(Department.ALL)) {
      return List.of(Department.ALL);
    }
    return departments.stream().distinct().toList();
  }

  private void notifyNewPassword(
      MailTemplate mailTemplate,
      UserEntity createdUserEntity,
      UserProductEntity userProduct,
      UserEntity managerUserEntity,
      String generatedPassword,
      String companyGroupName,
      String companyName) {
    List<String> toAddress = new ArrayList<>();
    List<String> ccAddress = new ArrayList<>();
    List<String> bccAddress = new ArrayList<>();
    boolean activateManagerNote = false;
    UserDefinitionResponse createdUser =
        UserMapper.toResponse(createdUserEntity, userProduct, enumFacade);
    if (createdUserEntity.getEmail() == null || createdUserEntity.getEmail().isBlank()) {
      toAddress.add(managerUserEntity.getEmail());
      activateManagerNote = true;
    } else {
      toAddress.add(createdUserEntity.getEmail());
      ccAddress.add(managerUserEntity.getEmail());
    }
    Map<MailParameters, Object> mailParams = new HashMap<>();

    mailParams.put(FULL_NAME, createdUserEntity.getFullName());
    mailParams.put(USERNAME, createdUserEntity.getUsername());
    mailParams.put(TEMPORARY_PASSWORD, generatedPassword);
    mailParams.put(ACTIVATE_MANAGER_NOTE, activateManagerNote);
    mailParams.put(MANAGER_FULL_NAME, managerUserEntity.getFullName());
    if (createdUser.getProductRole() != null) {
      mailParams.put(APPLICATION_ROLE, createdUser.getProductRole().getDisplay());
    }
    mailParams.put(
        DEPARTMENT, createdUser.getDepartments().stream().map(ResolvedEnum::getDisplay).toList());
    if (createdUser.getZone() != null) {
      mailParams.put(ZONE, createdUser.getZone().getDisplay());
    }
    mailParams.put(COMPANY_NAME, companyName);
    mailParams.put(COMPANY_GROUP_NAME, companyGroupName);
    mailService.sendMail(toAddress, ccAddress, bccAddress, mailTemplate, mailParams);
  }

  private UserEntity checkRegisteredUserRole(UserSaveRequest request) {
    boolean isAuthorized;
    String username = IJwtService.findSessionUserOrThrow().username();
    UserEntity sessionUserEntity = userService.findUserByUserName(username);
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUserEntity == null) {
      if (!adminRegistrationEnabled) {
        throw new AccessDeniedException(ErrorCode.ADMINISTRATOR_REGISTRATION_DISABLED);
      }
      isAuthorized =
          request.departments().contains(Department.ALL)
              && request.systemRole() == SystemRole.ADMIN;
    } else {
      ProductRole productRole = sessionUser != null ? sessionUser.getProductRole() : null;
      isAuthorized =
          (sessionUser != null && sessionUser.isAdmin())
              || (productRole != null && productRole.isSupervisorAndAbove());
    }

    if (!isAuthorized) {
      throw new AccessDeniedException(ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION);
    }
    if (sessionUserEntity == null
        || sessionUserEntity.getEmail() == null
        || sessionUserEntity.getEmail().isBlank()) {
      throw new ValidationException(
          ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION, "User email is required");
    }
    return sessionUserEntity;
  }

  private CompanyEntity getRegistrationCompany(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser == null
        || (request.productRole() != null && request.productRole().isZoneManagerAndAbove())) {
      return new CompanyEntity();
    }

    ProductRole sessionProductRole = sessionUser.getProductRole();
    if (sessionProductRole != null && sessionProductRole.isCompanyManagerAndAbove()) {
      return companyService.findByIdOrThrow(request.companyId());
    }

    return companyService.findByIdOrThrow(sessionUser.getCompanyId());
  }

  private CompanyGroupEntity getRegistrationCompanyGroup(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser == null || request.systemRole() == SystemRole.ADMIN) {
      return new CompanyGroupEntity();
    }
    if (request.productRole() != null && request.productRole().isGroupManagerOrZoneManager()) {
      if (request.companyGroupId() == null) {
        throw new ValidationException(
            ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION,
            String.format(
                "Requested user %s has GroupManager or ZoneManager role, but no companyGroupId is assigned. ProductRole: %s, companyGroupId is null",
                request.username(), request.productRole()));
      }
      return companyGroupService.findByIdOrThrow(request.companyGroupId());
    }
    if (sessionUser.companyGroupId() == null) {
      throw new ValidationException(
          ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION,
          String.format("Requested user %s has no companyGroupId", sessionUser.username()));
    }
    return companyGroupService.findByIdOrThrow(sessionUser.companyGroupId());
  }

  public UserDefinitionResponse toggleUser(String userId) {
    UserEntity toggledEntity = userService.toggleUser(userId);
    UserProductEntity userProduct = resolveUserProduct(userId);
    return UserMapper.toResponse(toggledEntity, userProduct, enumFacade);
  }

  public void sendOtp(final String userId) {
    UserEntity managerUser =
        userService.findUserByUserName(IJwtService.findSessionUserOrThrow().username());
    UserEntity userEntity = userService.findById(userId);
    String generatedPassword = PasswordGenerator.generate();
    userEntity.setPassword(passwordEncoder.encode(generatedPassword));
    userEntity.setPasswordChangeRequired(true);
    userEntity.setPasswordExpiresAt(LocalDateTime.now().plusMinutes(15));
    userService.updateUser(userEntity);
    String companyGroupName = "";
    if (userEntity.getCompanyGroupId() != null) {
      companyGroupName =
          companyGroupService
              .findById(userEntity.getCompanyGroupId())
              .map(CompanyGroupEntity::getName)
              .orElse("");
    }
    String companyName = "";
    if (userEntity.getCompanyId() != null) {
      companyName =
          companyService
              .findById(userEntity.getCompanyId())
              .map(CompanyEntity::getCompanyName)
              .orElse("");
    }
    notifyNewPassword(
        RESET_PASSWORD,
        userEntity,
        null,
        managerUser,
        generatedPassword,
        companyGroupName,
        companyName);
  }

  public void changePassword(final PasswordChangeRequest request) {
    UserEntity userEntity =
        userService.findUserByUserName(IJwtService.findSessionUserOrThrow().username());

    if (userEntity == null
        || !passwordEncoder.matches(request.oldPassword(), userEntity.getPassword())) {
      throw new BadCredentialsException("Bad credentials");
    }

    userEntity.setPassword(passwordEncoder.encode(request.newPassword()));
    if (userEntity.isPasswordChangeRequired()) {
      userEntity.setPasswordChangeRequired(false);
    }
    userEntity.setPasswordExpiresAt(LocalDateTime.now().plusMonths(12));

    userService.updateUser(userEntity);
  }

  public List<ResolvedEnum> findAllProductRoles() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    if (sessionUser.isAdmin()) {
      return enumFacade.getDropdownValues(ProductRole.class);
    }
    ProductRole sessionProductRole = sessionUser.getProductRole();
    if (sessionProductRole == null) return List.of();
    return enumFacade.getDropdownValues(ProductRole.class).stream()
        .filter(
            resolved -> {
              ProductRole role = ProductRole.fromString(resolved.getKey());
              return role != null && role.getLevel() > sessionProductRole.getLevel();
            })
        .toList();
  }

  public List<ResolvedEnum> findAllDepartments() {
    return enumFacade.getDropdownValues(Department.class);
  }

  public List<ResolvedEnum> findAllZones() {
    return enumFacade.getDropdownValues(Zone.class);
  }

  public List<ResolvedEnum> findAllGenders() {
    return enumFacade.getDropdownValues(Gender.class);
  }

  public List<ResolvedEnum> findAvailableCompanies() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AtomicInteger index = new AtomicInteger(0);
    return companyService.findByCompanyGroupId(sessionUser.companyGroupId()).stream()
        .filter(CompanyEntity::isActive)
        .filter(
            company ->
                sessionUser.companyIds() == null
                    || sessionUser.companyIds().isEmpty()
                    || sessionUser.companyIds().contains(company.getId()))
        .sorted(
            Comparator.comparing(
                c -> (c.getCompanyCode() + " - " + c.getCompanyName()).toLowerCase()))
        .map(
            company ->
                new ResolvedEnum(
                    company.getId(),
                    company.getCompanyCode() + " - " + company.getCompanyName(),
                    index.getAndIncrement()))
        .toList();
  }

  public List<ResolvedEnum> findAvailableCompanyGroups() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AtomicInteger index = new AtomicInteger(0);
    return companyGroupService.findAll().stream()
        .filter(
            companyGroup ->
                sessionUser.companyGroupId() == null
                    || Objects.equals(sessionUser.companyGroupId(), companyGroup.getId()))
        .sorted(
            Comparator.comparing(
                c -> c.getGroupCode().toLowerCase() + " - " + c.getName().toLowerCase()))
        .map(
            companyGroup ->
                new ResolvedEnum(
                    companyGroup.getId(),
                    companyGroup.getGroupCode() + " - " + companyGroup.getName(),
                    index.getAndIncrement()))
        .toList();
  }

  public CompanyContextResponse findUserCompanyContext(String userId) {
    UserEntity user = userService.findById(userId);
    if (user == null) {
      return CompanyContextResponse.builder().build();
    }
    CompanyGroupDefinitionResponse companyGroup = null;
    CompanyDefinitionResponse company = null;
    if (user.getCompanyGroupId() != null) {
      CompanyGroupEntity companyGroupEntity =
          companyGroupService.findByIdOrThrow(user.getCompanyGroupId());
      companyGroup = CompanyGroupMapper.toResponse(companyGroupEntity);
    }
    if (user.getCompanyId() != null) {
      CompanyEntity companyEntity = companyService.findByIdOrThrow(user.getCompanyId());
      company = CompanyGroupMapper.toResponse(companyEntity, enumFacade);
    }

    return CompanyContextResponse.builder().companyGroup(companyGroup).company(company).build();
  }

  public void updateLanguage(String userId, LanguageUpdateRequest request) {
    UserEntity userEntity = userService.findById(userId);
    userEntity.setLanguage(request.language());
    userService.updateUser(userEntity);
  }

  private UserProductEntity resolveUserProduct(String userId) {
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser != null && sessionUser.getApplicationProduct() != null) {
      return userProductService
          .findByUserIdAndProduct(userId, sessionUser.getApplicationProduct())
          .orElse(null);
    }
    return null;
  }
}
