package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.ProductRole;
import com.gastroblue.repository.UserRepository;
import com.gastroblue.service.IJwtService;
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

  private final UserRepository userRepository;
  private final UserProductService userProductService;

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
                    ErrorCode.USER_NOT_FOUND,
                    String.format("User not found (userId=%s)", userId)));
  }

  public UserEntity findUserByUserName(final String username) {
    return userRepository
        .findByUsername(username.toLowerCase(Locale.ENGLISH))
        .orElseThrow(
            () ->
                new IllegalDefinitionException(
                    ErrorCode.USER_NOT_FOUND,
                    String.format("User not found (username=%s)", username)));
  }

  public List<UserEntity> findAccessibleUser(
      Set<ProductRole> productRoles, ApplicationProduct product) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();

    List<String> normalizedCompanyIds =
        (sessionUser.companyIds() == null || sessionUser.companyIds().isEmpty())
            ? null
            : sessionUser.companyIds();

    Set<ProductRole> normalizedRoles =
        (productRoles == null || productRoles.isEmpty()) ? null : productRoles;

    return userRepository
        .findAccessibleUsers(
            sessionUser.companyGroupId(), product, normalizedCompanyIds, normalizedRoles)
        .stream()
        .toList();
  }

  public UserEntity toggleUser(String userId) {
    UserEntity entityToBeUpdated = findById(userId);
    entityToBeUpdated.setActive(!entityToBeUpdated.isActive());
    return userRepository.save(entityToBeUpdated);
  }

  public void signEula(String username, ApplicationProduct product) {
    UserEntity user = findUserByUserName(username);
    userProductService.updateEulaAcceptedAt(user.getId(), product);
  }

  @Transactional
  public void updateLoginStats(String username, String userId, ApplicationProduct product) {
    userRepository.updatePasswordCheckAfterLogin(username, LocalDateTime.now());
    if (product != null && userId != null) {
      userProductService.updateLastSuccessLogin(userId, product);
    }
  }
}
