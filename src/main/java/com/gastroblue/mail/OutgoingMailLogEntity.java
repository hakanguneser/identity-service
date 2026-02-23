package com.gastroblue.mail;

import com.gastroblue.model.entity.base.Auditable;
import com.gastroblue.model.enums.MailStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Persisted record of every email attempt.
 *
 * <p>Intentionally does NOT extend {@code Auditable} – auditing would require an active security
 * context, which is not available during async background processing.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "OUTGOING_MAIL_LOG")
public class OutgoingMailLogEntity extends Auditable {

  /** JSON-serialised list of primary recipients. */
  @Column(name = "TO_ADDRESSES", nullable = false, columnDefinition = "TEXT")
  private String toAddresses;

  /** JSON-serialised list of CC recipients (nullable). */
  @Column(name = "CC_ADDRESSES", columnDefinition = "TEXT")
  private String ccAddresses;

  /** JSON-serialised list of BCC recipients (nullable). */
  @Column(name = "BCC_ADDRESSES", columnDefinition = "TEXT")
  private String bccAddresses;

  @Column(name = "TEMPLATE_NAME", nullable = false, length = 100)
  private String templateName;

  /** JSON-serialised map of template parameters (nullable). */
  @Column(name = "TEMPLATE_PARAMS", columnDefinition = "TEXT")
  private String templateParams;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS", nullable = false, length = 20)
  private MailStatus status;

  @Column(name = "ERROR_MESSAGE", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "SENT_AT")
  private LocalDateTime sentAt;
}
