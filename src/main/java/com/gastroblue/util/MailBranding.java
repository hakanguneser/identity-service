package com.gastroblue.util;

import com.gastroblue.commons.helper.exception.type.BusinessException;
import com.gastroblue.commons.helper.mail.model.dto.MailAttachment;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

/**
 * Mail header per product. Tracker mails carry the Tracker logo as an inline image (templates
 * reference it as {@code <img src="cid:tracker-logo">}, same as tracker-service); the other
 * products have no logo yet and show the product name as text.
 */
@UtilityClass
public class MailBranding {

  private static final String CONTENT_ID = "tracker-logo";
  private static final String PATH = "mail/tracker-logo.png";

  private static volatile byte[] cachedBytes;

  public static boolean hasLogo(ApplicationProduct product) {
    return product == ApplicationProduct.TRACKER;
  }

  /** Text shown in the header when the product has no logo; null when the logo is shown. */
  public static String headerText(ApplicationProduct product) {
    if (hasLogo(product)) {
      return null;
    }
    if (product == null) {
      return "GastroBlue";
    }
    return switch (product) {
      case CHECK -> "GastroBlue Check APP";
      case FORMFLOW -> "GastroBlue Form Flow";
      default -> "GastroBlue";
    };
  }

  public static List<MailAttachment> attachments(ApplicationProduct product) {
    if (!hasLogo(product)) {
      return List.of();
    }
    return List.of(MailAttachment.inline(CONTENT_ID, "tracker-logo.png", bytes(), "image/png"));
  }

  private static byte[] bytes() {
    byte[] bytes = cachedBytes;
    if (bytes == null) {
      try (InputStream in = new ClassPathResource(PATH).getInputStream()) {
        bytes = in.readAllBytes();
        cachedBytes = bytes;
      } catch (IOException e) {
        throw new BusinessException(
            ErrorCode.MAIL_LOGO_NOT_FOUND, "Mail logo could not be loaded: " + e.getMessage());
      }
    }
    return bytes;
  }
}
