package com.gastroblue.model.base;

import io.gastroblue.commons.shared.enums.ApplicationProduct;
import io.gastroblue.commons.shared.enums.ApplicationRole;
import io.gastroblue.commons.shared.enums.Language;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record SessionUser(
    String userId,
    String applicationProduct,
    String applicationRole,
    List<String> departments,
    String companyGroupId,
    List<String> companyIds,
    String language,
    String username,
    Date issuedAt,
    Date expiresAt) {

  public Collection<? extends GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + applicationRole));
  }

  public Language getLanguage() {
    return Language.fromString(language);
  }

  public ApplicationRole getApplicationRole() {
    return ApplicationRole.fromString(applicationRole);
  }

  public ApplicationProduct getApplicationProduct() {
    return ApplicationProduct.fromString(applicationProduct);
  }

  public String getCompanyId() {
    if (companyIds != null && companyIds.size() == 1) {
      return companyIds.getFirst();
    }
    return null;
  }
}
