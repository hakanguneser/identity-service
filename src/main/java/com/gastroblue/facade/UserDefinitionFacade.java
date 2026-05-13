package com.gastroblue.facade;

import static com.gastroblue.commons.shared.enums.ApplicationRole.*;
import static com.gastroblue.model.enums.ErrorCode.INVALID_USERNAME_OR_PASSWORD;
import static com.gastroblue.model.enums.MailParameters.*;
import static com.gastroblue.model.enums.MailTemplate.INITIAL_PASSWORD;
import static com.gastroblue.model.enums.MailTemplate.RESET_PASSWORD;

import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.exception.type.BusinessException;
import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
import com.gastroblue.commons.helper.lookup.model.dto.LookupQuery;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.helper.mail.model.dto.Receivers;
import com.gastroblue.commons.helper.mail.service.IMailService;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.commons.shared.util.DelimitedStringUtil;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.Lookups;
import com.gastroblue.model.enums.MailParameters;
import com.gastroblue.model.enums.MailTemplate;
import com.gastroblue.model.request.LanguageUpdateRequest;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.AccessibleUsersResponse;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.DropdownResponse;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.service.UserDefinitionService;
import com.gastroblue.service.UserProductService;
import com.gastroblue.util.EmailDomainValidator;
import com.gastroblue.util.PasswordGenerator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserDefinitionFacade {

  private static final String DEPARTMENT_ALL = "ALL";

  private final UserDefinitionService userService;
  private final CompanyGroupService companyGroupService;
  private final CompanyService companyService;
  private final PasswordEncoder passwordEncoder;
  private final ILookupService lookupService;
  private final IMailService mailService;
  private final UserProductService userProductService;

  public UserDefinitionResponse findUserById(String userId) {
    UserEntity userEntity = userService.findById(userId);
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    if (!sessionUser.getApplicationRole().isAdministrator()
        && !Objects.equals(userEntity.getCompanyGroupId(), sessionUser.companyGroupId())) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED, "User not accessible");
    }
    UserProductEntity userProduct = resolveUserProduct(userId);
    return UserMapper.toResponse(userEntity, userProduct, lookupService);
  }

  @Transactional
  public UserDefinitionResponse updateUser(final String userId, final UserUpdateRequest request) {
    UserEntity existingEntity = userService.findById(userId);
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();

    if (!sessionUser.getApplicationRole().isAdministrator()
        && !Objects.equals(existingEntity.getCompanyGroupId(), sessionUser.companyGroupId())) {
      throw new AccessDeniedException(ErrorCode.ACCESS_DENIED, "User not accessible");
    }

    UserProductEntity userProduct = resolveUserProduct(userId);

    // Self-update is exempt from role hierarchy check
    if (!sessionUser.getApplicationRole().isAdministrator()
        && !Objects.equals(userId, sessionUser.userId())
        && userProduct != null
        && !getManageableRoles(sessionUser.getApplicationRole())
            .contains(userProduct.getApplicationRole())) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Insufficient role to update this user");
    }

    if (userProduct == null || !userProduct.getApplicationRole().isAdministrator()) {
      if (request.mail() != null && !request.mail().isBlank()) {
        if (existingEntity.getCompanyGroupId() != null) {
          CompanyGroup companyGroup =
              companyGroupService.findCompanyByIdOrThrow(existingEntity.getCompanyGroupId());
          EmailDomainValidator.validateAllowedDomains(
              DelimitedStringUtil.split(companyGroup.getMailDomains()), List.of(request.mail()));
        }
      }
    }

    UserEntity entityTobeUpdated = UserMapper.updateEntity(existingEntity, request);
    UserEntity updatedEntity = userService.updateUser(entityTobeUpdated);

    if (userProduct != null && request.departments() != null) {
      List<String> depts =
          request.departments().contains(DEPARTMENT_ALL)
              ? List.of(DEPARTMENT_ALL)
              : request.departments().stream().distinct().toList();
      userProduct.setDepartments(DelimitedStringUtil.join(depts));
      userProduct = userProductService.save(userProduct);
    }

    return UserMapper.toResponse(updatedEntity, userProduct, lookupService);
  }

  public AccessibleUsersResponse findAccessibleUsers(boolean includeAll) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    ApplicationProduct product = sessionUser.getApplicationProduct();
    ApplicationRole sessionRole = sessionUser.getApplicationRole();

    Set<ApplicationRole> targetRoles;
    if (includeAll) {
      targetRoles =
          switch (sessionRole) {
            case ADMIN -> Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
            case GROUP_MANAGER -> Set.of(ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
            case ZONE_MANAGER -> Set.of(COMPANY_MANAGER, SUPERVISOR, STAFF);
            case COMPANY_MANAGER -> Set.of(SUPERVISOR, STAFF);
            case SUPERVISOR -> Set.of(STAFF);
            default -> Set.of();
          };
    } else {
      targetRoles =
          switch (sessionRole) {
            case ADMIN -> Set.of(GROUP_MANAGER);
            case GROUP_MANAGER -> Set.of(COMPANY_MANAGER, ZONE_MANAGER);
            case ZONE_MANAGER -> Set.of(COMPANY_MANAGER);
            case COMPANY_MANAGER -> Set.of(SUPERVISOR);
            case SUPERVISOR -> Set.of(STAFF);
            default -> Set.of();
          };
    }

    List<UserEntity> users = userService.findAccessibleUser(targetRoles, product);
    List<String> userIds = users.stream().map(UserEntity::getId).toList();
    Map<String, UserProductEntity> userProductMap =
        product != null
            ? userProductService.findByUserIdInAndProduct(userIds, product).stream()
                .collect(Collectors.toMap(UserProductEntity::getUserId, up -> up))
            : Map.of();

    List<String> sessionDepartments = sessionUser.departments();
    return AccessibleUsersResponse.builder()
        .users(
            users.stream()
                .map(u -> UserMapper.toResponse(u, userProductMap.get(u.getId()), lookupService))
                .filter(
                    user -> {
                      if (sessionDepartments == null
                          || sessionDepartments.contains(DEPARTMENT_ALL)) {
                        return true;
                      }
                      return user.getDepartmentsList().stream()
                          .anyMatch(sessionDepartments::contains);
                    })
                .toList())
        .build();
  }

  @Transactional
  public UserDefinitionResponse saveUser(UserSaveRequest request) {
    UserEntity managerUser = checkRegisteredUserRole(request);
    CompanyGroupEntity companyGroup = getRegistrationCompanyGroup(request);
    CompanyEntity company = getRegistrationCompany(request);
    if (request.email() != null && !request.email().isBlank()) {
      List<String> mailDomains = DelimitedStringUtil.split(companyGroup.getMailDomains());
      if (!mailDomains.isEmpty()) {
        EmailDomainValidator.validateAllowedDomains(mailDomains, List.of(request.email()));
      }
    }
    String generatedPassword = PasswordGenerator.generate();
    UserEntity entityToBeSaved =
        UserMapper.toEntity(
            companyGroup.getId(),
            company.getId(),
            request,
            passwordEncoder.encode(generatedPassword));
    UserEntity savedUserEntity = userService.save(entityToBeSaved);

    ApplicationProduct product = getApplicationProduct(request.product());

    UserProductEntity savedUserProduct = null;
    if (product != null && request.applicationRole() != null) {
      UserProductEntity userProduct =
          UserProductEntity.builder()
              .userId(savedUserEntity.getId())
              .product(product)
              .applicationRole(request.applicationRole())
              .departments(DelimitedStringUtil.join(getDepartments(request)))
              .active(true)
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
    return UserMapper.toResponse(savedUserEntity, savedUserProduct, lookupService);
  }

  private static ApplicationProduct getApplicationProduct(ApplicationProduct requestedProduct) {

    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    ApplicationProduct product = sessionUser.getApplicationProduct();
    if (sessionUser.getApplicationRole().isAdministrator()) {
      if (requestedProduct == null) {
        throw new BusinessException(
            ErrorCode.PRODUCT_NOT_ALLOWED_FOR_REGISTRATION,
            String.format(
                "Requested user %s has Admin role, but no product is assigned. ApplicationRole: %s, companyGroupId is null",
                sessionUser.username(), sessionUser.applicationRole()));
      }
      product = requestedProduct;
    }
    return product;
  }

  private List<String> getDepartments(UserSaveRequest request) {
    List<String> departments = Optional.ofNullable(request.departments()).orElse(List.of());
    if (departments.contains(DEPARTMENT_ALL)) {
      return List.of(DEPARTMENT_ALL);
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

    Receivers receivers = new Receivers();
    Map<MailParameters, Object> mailParams = new EnumMap<>(MailParameters.class);

    boolean activateManagerNote = false;
    UserDefinitionResponse createdUser =
        UserMapper.toResponse(createdUserEntity, userProduct, lookupService);
    if (createdUserEntity.getEmail() == null || createdUserEntity.getEmail().isBlank()) {
      receivers.setTo(Collections.singletonList(managerUserEntity.getEmail()));
      activateManagerNote = true;
    } else {
      receivers.setTo(List.of(createdUserEntity.getEmail()));
      receivers.setCc(List.of(managerUserEntity.getEmail()));
    }

    mailParams.put(FULL_NAME, createdUserEntity.getFullName());
    mailParams.put(USERNAME, createdUserEntity.getUsername());
    mailParams.put(TEMPORARY_PASSWORD, generatedPassword);
    mailParams.put(ACTIVATE_MANAGER_NOTE, activateManagerNote);
    mailParams.put(MANAGER_FULL_NAME, managerUserEntity.getFullName());
    if (createdUser.getApplicationRole() != null) {
      mailParams.put(APPLICATION_ROLE, createdUser.getApplicationRole().getDisplay());
    }

    mailParams.put(
        DEPARTMENT,
        createdUser.getDepartments().stream()
            .filter(Objects::nonNull)
            .map(BaseLookupModel::getDisplay)
            .toList());
    if (createdUser.getZone() != null) {
      mailParams.put(ZONE, createdUser.getZone().getDisplay());
    }
    mailParams.put(COMPANY_NAME, companyName);
    mailParams.put(COMPANY_GROUP_NAME, companyGroupName);
    mailService.sendMail(mailTemplate, receivers, mailParams);
  }

  private UserEntity checkRegisteredUserRole(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    UserEntity sessionUserEntity = userService.findUserByUserName(sessionUser.username());

    ApplicationRole sessionRole = sessionUser.getApplicationRole();
    boolean isAuthorized = sessionRole != null && sessionRole.isSupervisorAndAbove();

    if (!isAuthorized) {
      throw new AccessDeniedException(ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION);
    }
    if (sessionUserEntity.getEmail() == null || sessionUserEntity.getEmail().isBlank()) {
      throw new BusinessException(
          ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION, "User email is required");
    }
    return sessionUserEntity;
  }

  private CompanyEntity getRegistrationCompany(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUser();

    if (request.applicationRole() != null && request.applicationRole().isZoneManagerAndAbove()) {
      return new CompanyEntity();
    }

    if (sessionUser == null
        || (sessionUser.getApplicationRole() != null
            && sessionUser.getApplicationRole().isCompanyManagerAndAbove())) {
      if (request.companyId() == null || request.companyId().isBlank()) {
        throw new BusinessException(
            ErrorCode.COMPANY_NOT_FOUND, "companyId is required for this registration role");
      }
      return companyService.findByIdOrThrow(request.companyId());
    }

    return companyService.findByIdOrThrow(sessionUser.getCompanyId());
  }

  private CompanyGroupEntity getRegistrationCompanyGroup(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUser();

    // 1. Session ADMIN ise
    if (sessionUser == null || sessionUser.getApplicationRole().isAdministrator()) {
      // Request'te de ADMIN tanımlıysa → boş entity dön (admin kaydı)
      if (request.applicationRole() != null && request.applicationRole().isAdministrator()) {
        return new CompanyGroupEntity();
      }
      // Request'te companyGroupId varsa → oradan çek
      if (request.companyGroupId() != null) {
        return companyGroupService.findByIdOrThrow(request.companyGroupId());
      }
      throw new BusinessException(
          ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION,
          "No companyGroupId provided for administrator registration");
    }

    // 2. Session ADMIN değilse → direkt session'dan çek
    if (sessionUser.companyGroupId() == null || sessionUser.companyGroupId().isEmpty()) {
      throw new BusinessException(
          ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION,
          String.format("Requested user %s has no companyGroupId", sessionUser.username()));
    }
    return companyGroupService.findByIdOrThrow(sessionUser.companyGroupId());
  }

  public UserDefinitionResponse toggleUser(String userId) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    ApplicationProduct product = sessionUser.getApplicationProduct();

    UserProductEntity userProduct =
        userProductService
            .findByUserIdAndProduct(userId, product)
            .orElseThrow(() -> new AccessDeniedException(ErrorCode.ACCESS_DENIED));
    if (!getManageableRoles(sessionUser.getApplicationRole())
        .contains(userProduct.getApplicationRole())) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Insufficient role to toggle this user");
    }
    userProduct.setActive(!userProduct.isActive());
    UserProductEntity updatedProduct = userProductService.save(userProduct);

    UserEntity userEntity = userService.findById(userId);
    return UserMapper.toResponse(userEntity, updatedProduct, lookupService);
  }

  @Transactional
  public void sendOtp(final String userId) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    UserEntity managerUser = userService.findUserByUserName(sessionUser.username());
    UserEntity userEntity = userService.findById(userId);
    userProductService
        .findByUserIdAndProduct(userId, sessionUser.getApplicationProduct())
        .ifPresent(
            targetProduct -> {
              if (!getManageableRoles(sessionUser.getApplicationRole())
                  .contains(targetProduct.getApplicationRole())) {
                throw new AccessDeniedException(
                    ErrorCode.ACCESS_DENIED, "Insufficient role to reset this user's password");
              }
            });
    String generatedPassword = PasswordGenerator.generate();
    userEntity.setPassword(passwordEncoder.encode(generatedPassword));
    userEntity.setPasswordChangeRequired(true);
    userEntity.setPasswordExpiresAt(LocalDateTime.now().plusMinutes(15));
    userEntity.setPasswordVersion(userEntity.getPasswordVersion() + 1);
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

  @Transactional
  public void changePassword(final PasswordChangeRequest request) {
    UserEntity userEntity =
        userService.findUserByUserName(IJwtService.findSessionUserOrThrow().username());

    if (!passwordEncoder.matches(request.oldPassword(), userEntity.getPassword())) {
      throw new AccessDeniedException(INVALID_USERNAME_OR_PASSWORD);
    }

    userEntity.setPassword(passwordEncoder.encode(request.newPassword()));
    if (userEntity.isPasswordChangeRequired()) {
      userEntity.setPasswordChangeRequired(false);
    }
    userEntity.setPasswordExpiresAt(LocalDateTime.now().plusMonths(12));
    userEntity.setPasswordVersion(userEntity.getPasswordVersion() + 1);

    userService.updateUser(userEntity);
  }

  public DropdownResponse findAllApplicationRoles() {
    return DropdownResponse.builder().items(lookupService.findAssignableRoles()).build();
  }

  public DropdownResponse findAllDepartments() {
    return DropdownResponse.builder()
        .items(lookupService.findAll(LookupQuery.of().lookup(Lookups.DEPARTMENT)))
        .build();
  }

  public DropdownResponse findAllZones() {
    return DropdownResponse.builder()
        .items(lookupService.findAll(LookupQuery.of().lookup(Lookups.ZONE)))
        .build();
  }

  public DropdownResponse findAllGenders() {
    return DropdownResponse.builder()
        .items(lookupService.findAll(LookupQuery.of().lookup(Lookups.GENDER)))
        .build();
  }

  public DropdownResponse findAvailableCompanies() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AtomicInteger index = new AtomicInteger(0);
    return DropdownResponse.builder()
        .items(
            companyService.findByCompanyGroupId(sessionUser.companyGroupId()).stream()
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
                        new BaseLookupModel(
                            company.getId(),
                            company.getCompanyCode() + " - " + company.getCompanyName(),
                            index.getAndIncrement()))
                .toList())
        .build();
  }

  public DropdownResponse findAvailableCompanyGroups() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    AtomicInteger index = new AtomicInteger(0);
    return DropdownResponse.builder()
        .items(
            companyGroupService.findAll().stream()
                .filter(
                    companyGroup ->
                        sessionUser.companyGroupId() == null
                            || Objects.equals(sessionUser.companyGroupId(), companyGroup.getId()))
                .sorted(
                    Comparator.comparing(
                        c -> c.getGroupCode().toLowerCase() + " - " + c.getName().toLowerCase()))
                .map(
                    companyGroup ->
                        new BaseLookupModel(
                            companyGroup.getId(),
                            companyGroup.getGroupCode() + " - " + companyGroup.getName(),
                            index.getAndIncrement()))
                .toList())
        .build();
  }

  public CompanyContextResponse findUserCompanyContext(String userId) {
    UserEntity user = userService.findById(userId);
    CompanyGroupDefinitionResponse companyGroup = null;
    CompanyDefinitionResponse company = null;
    if (user.getCompanyGroupId() != null) {
      CompanyGroupEntity companyGroupEntity =
          companyGroupService.findByIdOrThrow(user.getCompanyGroupId());
      companyGroup = CompanyGroupMapper.toResponse(companyGroupEntity);
    }
    if (user.getCompanyId() != null) {
      CompanyEntity companyEntity = companyService.findByIdOrThrow(user.getCompanyId());
      company = CompanyGroupMapper.toResponse(companyEntity, lookupService);
    }

    return CompanyContextResponse.builder().companyGroup(companyGroup).company(company).build();
  }

  public void updateLanguage(String userId, LanguageUpdateRequest request) {
    UserEntity userEntity = userService.findById(userId);
    userEntity.setLanguage(request.language());
    userService.updateUser(userEntity);
  }

  private static Set<ApplicationRole> getManageableRoles(ApplicationRole callerRole) {
    if (callerRole == null) return Set.of();
    return switch (callerRole) {
      case ADMIN -> Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
      case GROUP_MANAGER -> Set.of(ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
      case ZONE_MANAGER -> Set.of(COMPANY_MANAGER, SUPERVISOR, STAFF);
      case COMPANY_MANAGER -> Set.of(SUPERVISOR, STAFF);
      case SUPERVISOR -> Set.of(STAFF);
      default -> Set.of();
    };
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
