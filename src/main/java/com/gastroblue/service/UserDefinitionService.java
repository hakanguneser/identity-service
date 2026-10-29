package com.gastroblue.service;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDefinitionService {

  private static final int MAX_LOGIN_ATTEMPT_COUNT = 5;
  private static final int LOGIN_LOCK_DURATION_MINUTES = 15;

  private final UserRepository userRepository;

  public UserEntity updateUser(UserEntity userEntity) {
    return userRepository.save(userEntity);
  }

  public UserEntity save(final UserEntity entityToBeSaved) {
    try {
      return userRepository.save(entityToBeSaved);
    } catch (DataIntegrityViolationException e) {
      throw new NotFoundException(
          ErrorCode.USER_ALREADY_EXISTS,
          String.format("User already exists (userId=%s)", entityToBeSaved.getId()));
    }
  }

  public UserEntity findById(final String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new NotFoundException(
                    ErrorCode.USER_NOT_FOUND, String.format("User not found (userId=%s)", userId)));
  }

  public UserEntity findUserByUserName(final String username) {
    return userRepository
        .findByUsername(username.toLowerCase(Locale.ENGLISH))
        .orElseThrow(
            () ->
                new NotFoundException(
                    ErrorCode.USER_NOT_FOUND,
                    String.format("User not found (username=%s)", username)));
  }

  public List<UserEntity> findActiveByCompanyGroupIdAndCompanyIdAndProduct(
      String companyGroupId, String companyId, ApplicationProduct product) {
    return userRepository.findActiveByCompanyGroupIdAndCompanyIdAndProduct(
        companyGroupId, companyId, product);
  }

  /** Returns persisted users for the given ids (missing ids are omitted). */
  public List<UserEntity> findAllByIdIn(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return List.of();
    }
    return userRepository.findAllById(userIds);
  }

  public List<UserEntity> findAccessibleUser(
      Set<ApplicationRole> applicationRoles, ApplicationProduct product) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();

    List<String> normalizedCompanyIds =
        (sessionUser.companyIds() == null || sessionUser.companyIds().isEmpty())
            ? null
            : sessionUser.companyIds();

    Set<ApplicationRole> normalizedRoles =
        (applicationRoles == null || applicationRoles.isEmpty()) ? null : applicationRoles;

    return userRepository
        .findAccessibleUsers(
            sessionUser.companyGroupId(), product, normalizedCompanyIds, normalizedRoles)
        .stream()
        .toList();
  }

  @Transactional
  public void updatePasswordCheckAfterLogin(String username) {
    userRepository.updatePasswordCheckAfterLogin(username, LocalDateTime.now());
  }

  @Transactional
  public void incrementLoginAttempts(String username) {
    userRepository
        .findByUsername(username.toLowerCase(Locale.ENGLISH))
        .ifPresent(
            user -> {
              int newCount = user.getLoginAttemptCount() + 1;
              if (newCount >= MAX_LOGIN_ATTEMPT_COUNT) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOGIN_LOCK_DURATION_MINUTES));
                user.setLoginAttemptCount(0);
              } else {
                user.setLoginAttemptCount(newCount);
              }
              userRepository.save(user);
            });
  }
}
