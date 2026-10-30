package com.gastroblue.model.enums;

import com.gastroblue.commons.helper.mail.model.base.BaseMailTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MailTemplate implements BaseMailTemplate {
  INITIAL_PASSWORD("initial-password", "GastroBlue hesabınız oluşturuldu"),
  RESET_PASSWORD("reset-password", "GastroBlue şifreniz yenilendi");

  private final String templateName;
  private final String subject;
}
