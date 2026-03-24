package com.gastroblue.facade;

import com.gastroblue.exception.ValidationException;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityFacade {

  private final UserDefinitionService userDefinitionService;

  public void addUser() {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    UserEntity userEntity = userDefinitionService.findUserByUserName(sessionUser.username());
    ApplicationRole applicationRole = sessionUser.getApplicationRole();
    if (applicationRole == null || !applicationRole.isSupervisorAndAbove()) {
      throw new ValidationException(ErrorCode.INSUFFICIENT_ROLE, "User is not a supervisor");
    }
    if (userEntity.getEmail() == null || userEntity.getEmail().isBlank()) {
      throw new ValidationException(ErrorCode.USER_EMAIL_NOT_FOUND, "User email is not found");
    }
  }
}
