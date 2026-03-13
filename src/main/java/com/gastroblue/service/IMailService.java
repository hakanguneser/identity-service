package com.gastroblue.service;

import com.gastroblue.model.enums.MailParameters;
import com.gastroblue.model.enums.MailTemplate;
import java.util.List;
import java.util.Map;

/**
 * Public contract for the mail infrastructure.
 *
 * <p>Business code must depend only on this interface – never on the async implementation directly.
 *
 * <p>All sends are:
 *
 * <ul>
 *   <li><b>Asynchronous</b> – the caller is not blocked.
 *   <li><b>Logged</b> – every attempt is persisted to the {@code MAIL_LOG} table.
 * </ul>
 */
public interface IMailService {

  /**
   * Schedules an email for async delivery.
   *
   * @param to list of primary recipient addresses (at least one required)
   * @param cc list of CC addresses (may be null or empty)
   * @param bcc list of BCC addresses (may be null or empty)
   * @param template the template to render
   * @param params template substitution parameters (e.g. {@code "username" -> "John"})
   */
  void sendMail(
      List<String> to,
      List<String> cc,
      List<String> bcc,
      MailTemplate template,
      Map<MailParameters, Object> params);
}
