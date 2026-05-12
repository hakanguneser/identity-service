package com.gastroblue.model.entity;

import com.gastroblue.commons.helper.persistence.model.base.Auditable;
import com.gastroblue.commons.shared.enums.Language;
import com.gastroblue.model.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "USERS",
    uniqueConstraints = {@UniqueConstraint(name = "UK_USERS", columnNames = "USERNAME")},
    indexes = {
      @Index(name = "IDX_USERS_COMPANY_GROUP_ID", columnList = "COMPANY_GROUP_ID"),
      @Index(name = "IDX_USERS_COMPANY_ID_ACTIVE", columnList = "COMPANY_ID, IS_ACTIVE"),
      @Index(name = "IDX_USERS_PASSWORD_EXPIRES_AT", columnList = "PASSWORD_EXPIRES_AT")
    })
public class UserEntity extends Auditable implements UserDetails {

  @Column(name = "COMPANY_ID", length = 36)
  private String companyId;

  @Column(name = "COMPANY_GROUP_ID", length = 36)
  private String companyGroupId;

  @Column(name = "USERNAME", length = 100)
  private String username;

  @Column(name = "PASSWORD", length = 500)
  private String password;

  @Column(name = "EMAIL", length = 500)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "LANGUAGE", length = 5)
  private Language language;

  @Column(name = "IS_ACTIVE")
  private boolean active;

  @Column(name = "NAME", length = 100)
  private String name;

  @Column(name = "SURNAME", length = 100)
  private String surname;

  @Column(name = "PHONE", length = 10)
  private String phone;

  @Column(name = "GENDER", length = 10)
  private String gender;

  @Column(name = "ZONE", length = 10)
  private String zone;

  @Column(name = "PASSWORD_CHANGE_REQUIRED", nullable = false)
  private boolean passwordChangeRequired;

  @Column(name = "PASSWORD_EXPIRES_AT")
  private LocalDateTime passwordExpiresAt;

  @Column(name = "PASSWORD_VERSION", nullable = false)
  @Builder.Default
  private int passwordVersion = 1;

  @Column(name = "LOGIN_ATTEMPT_COUNT", nullable = false)
  @Builder.Default
  private int loginAttemptCount = 0;

  @Column(name = "LOCKED_UNTIL")
  private LocalDateTime lockedUntil;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_AUTHENTICATED"));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }

  public String getFullName() {
    return name + " " + surname;
  }
}
