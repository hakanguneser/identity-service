package com.gastroblue.facade;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.response.UserEligibilityResponse;
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

  public UserEligibilityResponse trackerAddUser() {
    try {
      SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
      UserEntity userEntity = userDefinitionService.findUserByUserName(sessionUser.username());
      ApplicationRole applicationRole = sessionUser.getApplicationRole();
      if (applicationRole == null || !applicationRole.isSupervisorAndAbove()) {
        return new UserEligibilityResponse(
            false, "User Must Be Supervisor or Above"); // TODO: Language support
      }
      if (userEntity.getEmail() == null || userEntity.getEmail().isBlank()) {
        return new UserEligibilityResponse(false, "User Must have Email"); // TODO: Language support
      }

    } catch (Exception e) {
      return new UserEligibilityResponse(false, "Unexpected Error"); // TODO: Language support
    }
    return new UserEligibilityResponse(true, "User is eligible to add tracker user");
  }
}
