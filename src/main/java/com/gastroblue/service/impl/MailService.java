package com.gastroblue.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gastroblue.model.entity.OutgoingMailLogEntity;
import com.gastroblue.model.enums.MailParameters;
import com.gastroblue.model.enums.MailStatus;
import com.gastroblue.model.enums.MailTemplate;
import com.gastroblue.model.properties.MailProperties;
import com.gastroblue.repository.OutgoingMailLogRepository;
import com.gastroblue.service.IMailService;
import com.gastroblue.util.DelimitedStringUtil;
import com.gastroblue.util.MailTemplateRenderer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Async implementation of {@link IMailService}.
 *
 * <p>All calls are dispatched to the dedicated {@code mailTaskExecutor} thread pool. The caller is
 * never blocked and never receives exceptions from this method.
 *
 * <p>Processing order:
 *
 * <ol>
 *   <li>Insert PENDING log row.
 *   <li>If mail is disabled → mark SKIPPED, return.
 *   <li>Render template.
 *   <li>Send via SMTP, measuring duration.
 *   <li>Update log → SUCCESS or FAILED.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService implements IMailService {

  private final MailProperties mailProperties;
  private final JavaMailSender mailSender;
  private final OutgoingMailLogRepository outgoingMailLogRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @Async("mailTaskExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendMail(
      List<String> to,
      List<String> cc,
      List<String> bcc,
      MailTemplate template,
      Map<MailParameters, Object> params) {

    log.debug(
        "mail.send requested [template={}, to.count={}]",
        template.name(),
        to != null ? to.size() : 0);
    if (to == null || to.isEmpty()) {
      log.info("mail.skipped [reason=empty-to-address, template={}]", template.name());
      return;
    }
    OutgoingMailLogEntity mailLog = buildPendingLog(to, cc, bcc, template, params);
    outgoingMailLogRepository.save(mailLog);

    if (!mailProperties.isEnabled()) {
      log.info("mail.skipped [reason=mail-disabled, template={}]", template.name());
      updateLog(mailLog, MailStatus.SKIPPED, null);
      return;
    }
    try {
      String body = MailTemplateRenderer.render(template, params);
      doSend(to, cc, bcc, template, body);

      log.info("mail.sent [template={}, to.count={}]", template.name(), to.size());
      updateLog(mailLog, MailStatus.SUCCESS, null);

    } catch (Exception ex) {
      log.error("mail.failed [template={}, error={}]", template.name(), ex.getMessage());
      updateLog(mailLog, MailStatus.FAILED, truncate(ex.getMessage(), 2000));
    }
  }

  private void doSend(
      List<String> to, List<String> cc, List<String> bcc, MailTemplate template, String body)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setFrom(mailProperties.getFrom());
    helper.setTo(to.toArray(new String[0]));

    if (cc != null && !cc.isEmpty()) {
      helper.setCc(cc.toArray(new String[0]));
    }
    if (bcc != null && !bcc.isEmpty()) {
      helper.setBcc(bcc.toArray(new String[0]));
    }

    helper.setSubject(resolveSubject(template));
    helper.setText(body, true);

    mailSender.send(message);
  }

  private String resolveSubject(MailTemplate template) {
    return switch (template) {
      case INITIAL_PASSWORD -> "GastroBlue – Hesabınız Oluşturuldu";
    };
  }

  private OutgoingMailLogEntity buildPendingLog(
      List<String> to,
      List<String> cc,
      List<String> bcc,
      MailTemplate template,
      Map<MailParameters, Object> params) {
    return OutgoingMailLogEntity.builder()
        .toAddresses(DelimitedStringUtil.join(to))
        .ccAddresses(DelimitedStringUtil.join(cc))
        .bccAddresses(DelimitedStringUtil.join(bcc))
        .templateName(template.getTemplateName())
        .templateParams(toJson(params))
        .status(MailStatus.PENDING)
        .build();
  }

  private void updateLog(OutgoingMailLogEntity entity, MailStatus status, String errorMessage) {
    entity.setStatus(status);
    entity.setErrorMessage(errorMessage);
    if (status == MailStatus.SUCCESS) {
      entity.setSentAt(LocalDateTime.now());
    }
    outgoingMailLogRepository.save(entity);
  }

  private String toJson(Object value) {
    if (value == null) return null;
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      log.warn("mail.log serialisation failed for value type={}", value.getClass().getSimpleName());
      return value.toString();
    }
  }

  private String truncate(String text, int maxLength) {
    if (text == null) return null;
    return text.length() <= maxLength ? text : text.substring(0, maxLength);
  }
}
