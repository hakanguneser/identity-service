package com.gastroblue.model.base;

import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.Department;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.enums.ProductRole;
import com.gastroblue.model.enums.SystemRole;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record SessionUser(
    String applicationProduct,
    String systemRole,
    String productRole,
    List<String> departments,
    String companyGroupId,
    List<String> companyIds,
    String language,
    String username,
    Date issuedAt,
    Date expiresAt) {

  public Collection<? extends GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + systemRole));
  }

  public List<Department> getDepartments() {
    return departments.stream().map(Department::fromString).toList();
  }

  public Language getLanguage() {
    return Language.fromString(language);
  }

  public SystemRole getSystemRole() {
    return SystemRole.fromString(systemRole);
  }

  public ProductRole getProductRole() {
    return ProductRole.fromString(productRole);
  }

  public boolean isAdmin() {
    return SystemRole.ADMIN.name().equals(systemRole);
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
