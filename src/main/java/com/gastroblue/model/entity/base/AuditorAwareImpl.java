package com.gastroblue.model.entity.base;

import com.gastro.formflow.services.model.base.organization.SessionUser;
import com.gastro.formflow.services.service.IJwtService;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

import static com.gastro.formflow.services.service.IJwtService.ANONYMOUS_USER;

public class AuditorAwareImpl implements AuditorAware<String> {
  @Override
  public @NonNull Optional<String> getCurrentAuditor() {
    return Optional.ofNullable(IJwtService.findSessionUser())
        .map(SessionUser::getSessionUsername)
        .filter(username -> !username.isBlank())
        .or(() -> Optional.of(ANONYMOUS_USER));
  }
}
