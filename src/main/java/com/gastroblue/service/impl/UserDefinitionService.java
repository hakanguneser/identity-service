package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.UserMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.base.User;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.repository.UserRepository;
import com.gastroblue.service.IJwtService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDefinitionService {

  private final UserRepository userRepository;

  public UserEntity updateUser(UserEntity userEntity) {
    return userRepository.save(userEntity);
  }

  public UserEntity save(final UserEntity entityToBeSaved) {
    try {
      return userRepository.save(entityToBeSaved);
    } catch (DataIntegrityViolationException e) {
      throw new IllegalDefinitionException(
          ErrorCode.USER_ALREADY_EXISTS,
          String.format("User already exists (userId=%s)", entityToBeSaved.getId()));
    }
  }

  public UserEntity findById(final String userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    ErrorCode.USER_NOT_FOUND, String.format("User not found (userId=%s)", userId)));
  }

  public UserEntity findUserEntityByUserName(final String username) {
    return userRepository
        .findByUsername(username.toLowerCase(Locale.ENGLISH))
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    ErrorCode.USER_NOT_FOUND,
                    String.format("User not found (username=%s)", username)));
  }

  public List<User> findUserByCompanyId(final String companyId) {
    return userRepository.findByCompanyId(companyId).stream().map(UserMapper::toBase).toList();
  }

  public List<UserEntity> findAccessibleUser(Set<ApplicationRole> applicationRole) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    return userRepository
        .findByCompanyGroupIdAndZoneIdAndCompanyIdAndApplicationRoleIn(
            sessionUser.companyGroupId(),
            sessionUser.zone(),
            sessionUser.companyId(),
            applicationRole)
        .stream()
        .toList();
  }

  public UserEntity toggleUser(String userId) {
    UserEntity entityToBeUpdated = findById(userId);
    entityToBeUpdated.setActive(!entityToBeUpdated.isActive());
    return userRepository.save(entityToBeUpdated);
  }

  public void signAgreement(String userId) {
    UserEntity entityToBeUpdated = findById(userId);
    entityToBeUpdated.setTermsAcceptanceRequired(false);
    entityToBeUpdated.setTermsAcceptedDate(LocalDateTime.now());
    userRepository.save(entityToBeUpdated);
  }
}
