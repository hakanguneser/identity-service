package com.gastroblue.model.entity.base;

import static com.gastroblue.service.IJwtService.ANONYMOUS_USER;

import com.gastroblue.model.base.SessionUser;
import com.gastroblue.service.IJwtService;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;

public class AuditorAwareImpl implements AuditorAware<String> {
  @Override
  public @NonNull Optional<String> getCurrentAuditor() {
    return Optional.ofNullable(IJwtService.findSessionUser())
        .map(SessionUser::username)
        .filter(username -> !username.isBlank())
        .or(() -> Optional.of(ANONYMOUS_USER));
  }
}
