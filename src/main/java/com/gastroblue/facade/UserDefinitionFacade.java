package com.gastroblue.facade;

import static com.gastroblue.model.enums.ApplicationRole.*;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.ValidationException;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.*;
import com.gastroblue.model.request.PasswordChangeRequest;
import com.gastroblue.model.request.UserSaveRequest;
import com.gastroblue.model.request.UserUpdateRequest;
import com.gastroblue.model.response.BatchUserDefinitionResponse;
import com.gastroblue.model.response.UserDefinitionResponse;
import com.gastroblue.model.shared.EnumDisplay;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.ApplicationPropertyService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.service.impl.UserDefinitionService;
import com.gastroblue.util.PasswordGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

  @Value("${admin.registration.enabled}")
  private boolean adminRegistrationEnabled;

  private final UserDefinitionService userService;
  private final CompanyGroupService companyGroupService;
  private final CompanyService companyService;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationPropertyService appPropertyService;

  public UserDefinitionResponse findUserById(String userId) {
    UserEntity userEntity = userService.findById(userId);
    return UserMapper.toResponse(userEntity);
  }

  public UserDefinitionResponse updateUser(final String userId, final UserUpdateRequest request) {
    UserEntity existingEntity = userService.findById(userId);
    UserEntity entityTobeUpdated = UserMapper.updateEntity(existingEntity, request);
    UserEntity updatedEntity = userService.updateUser(entityTobeUpdated);
    return UserMapper.toResponse(updatedEntity);
  }

  public List<UserDefinitionResponse> findAccessibleUsers(boolean includeAll) {
    // TODO : burada department eklemek gerekecek
    Set<ApplicationRole> targetRoles;
    SessionUser user = IJwtService.findSessionUserOrThrow();

    if (includeAll) {
      targetRoles =
          switch (user.applicationRole()) {
            case ADMIN -> Set.of(GROUP_MANAGER, ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
            case GROUP_MANAGER -> Set.of(ZONE_MANAGER, COMPANY_MANAGER, SUPERVISOR, STAFF);
            case ZONE_MANAGER -> Set.of(COMPANY_MANAGER, SUPERVISOR, STAFF);
            case COMPANY_MANAGER -> Set.of(SUPERVISOR, STAFF);
            case SUPERVISOR -> Set.of(STAFF);
            default -> Set.of();
          };
    } else {
      targetRoles =
          switch (user.applicationRole()) {
            case ADMIN -> Set.of(GROUP_MANAGER);
            case GROUP_MANAGER -> Set.of(COMPANY_MANAGER, ZONE_MANAGER);
            case ZONE_MANAGER -> Set.of(COMPANY_MANAGER);
            case COMPANY_MANAGER -> Set.of(SUPERVISOR);
            case SUPERVISOR -> Set.of(STAFF);
            default -> Set.of();
          };
    }
    return userService.findAccessibleUser(targetRoles).stream()
        .map(UserMapper::toResponse)
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
    return UserMapper.toResponse(savedEntity);
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
      isAuthorized = sessionUser.applicationRole().isSupervisorAndAbove();
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

    if (sessionUser.applicationRole().isCompanyManagerAndAbove()) {
      return companyService.findOrThrow(request.companyId());
    }

    return companyService.findOrThrow(sessionUser.companyId());
  }

  private CompanyGroupEntity getRegistrationCompanyGroup(UserSaveRequest request) {
    SessionUser sessionUser = IJwtService.findSessionUser();
    if (sessionUser == null || request.applicationRole().isAdministrator()) {
      return new CompanyGroupEntity();
    }
    if (request.applicationRole().isGroupManagerOrZoneManager()) {
      if (request.companyGroupId() == null) {
        throw new ValidationException(ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION);
      }
      return companyGroupService.findById(request.companyGroupId());
    }
    if (sessionUser.companyGroupId() == null) {
      throw new ValidationException(ErrorCode.USER_NOT_ALLOWED_FOR_REGISTRATION);
    }
    return companyGroupService.findById(sessionUser.companyGroupId());
  }

  public UserDefinitionResponse toggleUser(String userId) {
    UserEntity toggledEntity = userService.toggleUser(userId);
    return UserMapper.toResponse(toggledEntity);
  }

  public void sendOtp(final String userId) {
    UserEntity userEntity = userService.findById(userId);
    // TODO : check session user otp icin gelinen userin amiri mi ? daha once otp
    // gonderilmis mi ?
    // 120 saniye sayaci ?
    String generatedPassword = PasswordGenerator.generate();
    userEntity.setPassword(passwordEncoder.encode(generatedPassword));
    userEntity.setPasswordChangeRequired(true);
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

    userService.updateUser(userEntity);
  }

  public List<EnumDisplay> findAllApplicationRoles() {
    SessionUser sessionUser = IJwtService.findSessionUser();
    ApplicationRole role = sessionUser != null ? sessionUser.applicationRole() : null;

    if (role == null) {
      return Collections.emptyList();
    }

    return appPropertyService.getDropdownItems(ApplicationRole.class).stream()
        .filter(
            item -> {
              try {
                ApplicationRole candidate = ApplicationRole.valueOf(item.getKey());
                return candidate.isVisibleFor(role);
              } catch (IllegalArgumentException e) {
                return false;
              }
            })
        .toList();
  }

  public List<EnumDisplay> findAllDepartments() {
    return appPropertyService.getDropdownItems(Department.class);
  }

  public List<EnumDisplay> findAllZones() {
    return appPropertyService.getDropdownItems(Zone.class);
  }

  public List<EnumDisplay> findAllGenders() {
    return appPropertyService.getDropdownItems(Gender.class);
  }

  public BatchUserDefinitionResponse saveUsersBatch(List<UserSaveRequest> items) {
    List<UserDefinitionResponse> createdUsers = new ArrayList<>();
    for (UserSaveRequest request : items) {
      UserDefinitionResponse created = saveUser(request);
      createdUsers.add(created);
    }
    return new BatchUserDefinitionResponse(createdUsers);
  }
}
