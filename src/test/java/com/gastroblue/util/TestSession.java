package com.gastroblue.util;

import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

/** Puts a session user into the security context for facade and util tests. */
public final class TestSession {

  private TestSession() {}

  public static SessionUser login(
      ApplicationRole role, String companyGroupId, List<String> companyIds) {
    SessionUser user =
        new SessionUser(
            "caller",
            "TRACKER",
            role.name(),
            List.of("ALL"),
            companyGroupId,
            companyIds,
            "TR",
            "caller",
            0,
            null,
            null);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    return user;
  }

  public static void logout() {
    SecurityContextHolder.clearContext();
  }
}
