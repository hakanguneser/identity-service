package com.gastroblue.model.base;

import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.commons.shared.enums.ApplicationRole;
import com.gastroblue.commons.shared.enums.Language;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
    Date expiresAt)
    implements UserDetails {

  @Override
  public String getUsername() {
    return userId;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + applicationRole));
  }

  public Collection<? extends GrantedAuthority> authorities() {
    return getAuthorities();
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

  @Override
  public boolean isAccountNonExpired() {
    return expiresAt == null || expiresAt.after(new Date());
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isAccountNonExpired();
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public String getCompanyId() {
    if (companyIds != null && companyIds.size() == 1) {
      return companyIds.getFirst();
    }
    return null;
  }
}
