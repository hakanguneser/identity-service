package com.gastroblue.util;

import com.gastroblue.commons.helper.exception.type.BusinessException;
import com.gastroblue.commons.helper.mail.model.dto.MailAttachment;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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

  // GastroBlue Tracker store pages (same app id on both stores:
  // com.gastroblueco.thermometertracking).
  private static final String TRACKER_APP_STORE_URL =
      "https://apps.apple.com/tr/app/gastroblue-tracker/id6759391154";
  private static final String TRACKER_PLAY_STORE_URL =
      "https://play.google.com/store/apps/details?id=com.gastroblueco.thermometertracking";

  // Official store badges (Google Play: Turkish PNG; App Store: Apple's badge rendered to PNG,
  // since most mail clients do not show SVG).
  private static final String APP_STORE_BADGE_ID = "app-store-badge";
  private static final String APP_STORE_BADGE_PATH = "mail/app-store-badge.png";
  private static final String GOOGLE_PLAY_BADGE_ID = "google-play-badge";
  private static final String GOOGLE_PLAY_BADGE_PATH = "mail/google-play-badge.png";
  private static final String PNG = "image/png";

  private static final Map<String, byte[]> CACHE = new ConcurrentHashMap<>();

  /** Store download links are shown for Tracker users only. */
  public static boolean hasStoreLinks(ApplicationProduct product) {
    return product == ApplicationProduct.TRACKER;
  }

  public static String appStoreUrl(ApplicationProduct product) {
    return hasStoreLinks(product) ? TRACKER_APP_STORE_URL : null;
  }

  public static String playStoreUrl(ApplicationProduct product) {
    return hasStoreLinks(product) ? TRACKER_PLAY_STORE_URL : null;
  }

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

  /**
   * Inline images of the mail: the Tracker logo and, with the store links, the official App Store
   * and Google Play badges (templates use cid:app-store-badge and cid:google-play-badge).
   */
  public static List<MailAttachment> attachments(ApplicationProduct product) {
    List<MailAttachment> attachments = new ArrayList<>();
    if (hasLogo(product)) {
      attachments.add(MailAttachment.inline(CONTENT_ID, "tracker-logo.png", bytes(PATH), PNG));
    }
    if (hasStoreLinks(product)) {
      attachments.add(
          MailAttachment.inline(
              APP_STORE_BADGE_ID, "app-store-badge.png", bytes(APP_STORE_BADGE_PATH), PNG));
      attachments.add(
          MailAttachment.inline(
              GOOGLE_PLAY_BADGE_ID, "google-play-badge.png", bytes(GOOGLE_PLAY_BADGE_PATH), PNG));
    }
    return attachments;
  }

  private static byte[] bytes(String path) {
    return CACHE.computeIfAbsent(
        path,
        p -> {
          try (InputStream in = new ClassPathResource(p).getInputStream()) {
            return in.readAllBytes();
          } catch (IOException e) {
            throw new BusinessException(
                ErrorCode.MAIL_LOGO_NOT_FOUND,
                "Mail image could not be loaded: " + p + " " + e.getMessage());
          }
        });
  }
}
