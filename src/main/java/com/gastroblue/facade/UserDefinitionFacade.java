package com.gastroblue.facade;

import static com.gastroblue.model.enums.ApplicationRole.*;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.ValidationException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.CompanyContextResponse;
import com.gastroblue.model.response.CompanyDefinitionResponse;
import com.gastroblue.model.response.CompanyGroupDefinitionResponse;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.service.impl.UserDefinitionService;
import com.gastroblue.util.PasswordGenerator;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
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

  public UserDefinitionResponse findUserById(String userId) {
    UserEntity userEntity = userService.findById(userId);
    return UserMapper.toResponse(userEntity, enumFacade);
  }

  public UserDefinitionResponse updateUser(final String userId, final UserUpdateRequest request) {
    UserEntity existingEntity = userService.findById(userId);
    UserEntity entityTobeUpdated = UserMapper.updateEntity(existingEntity, request);
    UserEntity updatedEntity = userService.updateUser(entityTobeUpdated);
    return UserMapper.toResponse(updatedEntity, enumFacade);
  }

  public List<UserDefinitionResponse> findAccessibleUsers(boolean includeAll) {
    // TODO : burada department eklemek gerekecek
    Set<ApplicationRole> targetRoles;
    SessionUser user = IJwtService.findSessionUserOrThrow();

    if (includeAll) {
      targetRoles =
          switch (user.getApplicationRole()) {
            case ADMIN -> Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
            case GROUP_MANAGER -> Set.of(ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
            case ZONE_MANAGER -> Set.of(COMPANY_MANAGER, SUPERVISOR, STAFF);
            case COMPANY_MANAGER -> Set.of(SUPERVISOR, STAFF);
            case SUPERVISOR -> Set.of(STAFF);
            default -> Set.of();
          };
    } else {
      targetRoles =
          switch (user.getApplicationRole()) {
            case ADMIN -> Set.of(GROUP_MANAGER);
            case GROUP_MANAGER -> Set.of(COMPANY_MANAGER, ZONE_MANAGER);
            case ZONE_MANAGER -> Set.of(COMPANY_MANAGER);
            case COMPANY_MANAGER -> Set.of(SUPERVISOR);
            case SUPERVISOR -> Set.of(STAFF);
            default -> Set.of();
          };
    }
    return userService.findAccessibleUser(targetRoles).stream()
        .map(u -> UserMapper.toResponse(u, enumFacade))
        .toList();
  }

  public UserDefinitionResponse saveUser(UserSaveRequest request) {
    checkRegisteredUserRole(request);
    CompanyGroupEntity companyGroup = getRegistrationCompanyGroup(request);
    CompanyEntity company = getRegistrationCompany(request);
    String otp = PasswordGenerator.generate();
    UserEntity entityToBeSaved =
        UserMapper.toEntity(
            companyGroup.getId(), company.getId(), request, passwordEncoder.encode(otp));
    UserEntity savedEntity = userService.save(entityToBeSaved);
    // TODO : notifyNewPassword(savedEntity, generatedPassword); buradaki stratejiyi
    // konusmamiz
    // lazim
    return UserMapper.toResponse(savedEntity, enumFacade);
  }

  private void checkRegisteredUserRole(UserSaveRequest request) {
    boolean isAuthorized;
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser == null) {
      if (!adminRegistrationEnabled) {
        throw new AccessDeniedException(ErrorCode.ADMINISTRATOR_REGISTRATION_DISABLED);
      }
      isAuthorized =
          request.departments().contains(Department.ALL)
              && request.applicationRole().isAdministrator();
    } else {
      isAuthorized = sessionUser.getApplicationRole().isSupervisorAndAbove();
    }

    if (!isAuthorized) {
      throw new AccessDeniedException(ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION);
    }
  }

  private CompanyEntity getRegistrationCompany(UserSaveRequest request) {

    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser == null || request.applicationRole().isZoneManagerAndAbove()) {
      return new CompanyEntity();
    }

    if (sessionUser.getApplicationRole().isCompanyManagerAndAbove()) {
      return companyService.findByIdOrThrow(request.companyId());
    }

    return companyService.findByIdOrThrow(sessionUser.companyIds().get(0));
  }

  private CompanyGroupEntity getRegistrationCompanyGroup(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser == null || request.applicationRole().isAdministrator()) {
      return new CompanyGroupEntity();
    }
    if (request.applicationRole().isGroupManagerOrZoneManager()) {
      if (request.companyGroupId() == null) {
        throw new ValidationException(
            ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION,
            String.format(
                "Requested user %s has GroupManager or ZoneManager role, but no companyGroupId is assigned. ApplicationRole: %s, companyGroupId is null",
                request.username(), request.applicationRole()));
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
    return UserMapper.toResponse(toggledEntity, enumFacade);
  }

  public void sendOtp(final String userId) {
    UserEntity userEntity = userService.findById(userId);
    // TODO : check session user otp icin gelinen userin amiri mi ? daha once otp
    // gonderilmis mi ?
    // 120 saniye sayaci ?
    String generatedPassword = PasswordGenerator.generate();
    userEntity.setPassword(passwordEncoder.encode(generatedPassword));
    userEntity.setPasswordChangeRequired(true);
    userEntity.setPasswordExpiresAt(LocalDateTime.now().plusMinutes(15));
    userService.updateUser(userEntity);
    // notifyNewPassword(generatedPassword, request.getEmail()); // TODO : kisi
    // forgat password
    // yapamaz, bir ustu bunu yapabilir ona mail atacaz
  }

  public void changePassword(final String userId, final PasswordChangeRequest request) {
    UserEntity userEntity = userService.findById(userId);

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

  public List<ResolvedEnum> findAllApplicationRoles() {
    return enumFacade.getDropdownValues(ApplicationRole.class);
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
}
