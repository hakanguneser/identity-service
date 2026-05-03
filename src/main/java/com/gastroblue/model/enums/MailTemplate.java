package com.gastroblue.model.enums;

import com.gastroblue.commons.helper.mail.model.base.BaseMailTemplate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum MailTemplate implements BaseMailTemplate {
  INITIAL_PASSWORD("initial-password", "GastroBlue – Hesabınız Oluşturuldu"),
  RESET_PASSWORD("reset-password", "GastroBlue – Şifreniz Yenilendi");

  private final String templateName;
  private final String subject;
}
