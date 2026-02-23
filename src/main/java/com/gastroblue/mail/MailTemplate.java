package com.gastroblue.mail;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum representing all supported email templates.
 *
 * <p>To add a new template:
 *
 * <ol>
 *   <li>Add a new constant here with the template file name (without extension).
 *   <li>Create the corresponding {@code .html} file in {@code resources/templates/mail/}.
 * </ol>
 */
@Getter
@RequiredArgsConstructor
public enum MailTemplate {
  WELCOME("welcome");

  /** File name (without extension) relative to {@code resources/templates/mail/}. */
  private final String templateName;
}
