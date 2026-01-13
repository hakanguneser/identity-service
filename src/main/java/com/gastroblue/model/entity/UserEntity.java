package com.gastroblue.model.entity;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.ApplicationRole;
import com.gastroblue.model.enums.Gender;
import com.gastroblue.model.enums.Language;
import com.gastroblue.model.enums.Zone;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "USERS")
public class UserEntity extends Auditable implements UserDetails {
  @Column(name = "COMPANY_ID", length = 36)
  private String companyId;

  @Column(name = "COMPANY_GROUP_ID", length = 36)
  private String companyGroupId;

  @Column(name = "USERNAME", length = 100, unique = true)
  private String username;

  @Column(name = "PASSWORD", length = 500)
  private String password;

  @Column(name = "EMAIL", length = 500)
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "LANGUAGE", length = 5)
  private Language language;

  @Enumerated(EnumType.STRING)
  @Column(name = "APPLICATION_ROLE", length = 50)
  private ApplicationRole applicationRole;

  @Column(name = "DEPARTMENTS", length = 1000, nullable = false)
  private String departments;

  @Column(name = "IS_ACTIVE")
  private boolean active;

  @Column(name = "NAME", length = 100)
  private String name;

  @Column(name = "SURNAME", length = 100)
  private String surname;

  @Column(name = "PHONE", length = 10)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "GENDER", length = 10)
  private Gender gender;

  @Enumerated(EnumType.STRING)
  @Column(name = "ZONE", length = 10)
  private Zone zone;

  @Column(name = "PASSWORD_CHANGE_REQUIRED", nullable = false)
  private boolean passwordChangeRequired;

  @Column(name = "TERMS_ACCEPTANCE_REQUIRED", nullable = false)
  private boolean termsAcceptanceRequired;

  @Column(name = "TERMS_ACCEPTED_DATE")
  private LocalDateTime termsAcceptedDate;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(applicationRole.name()));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public String getFullName() {
    return name + " " + surname;
  }
}
