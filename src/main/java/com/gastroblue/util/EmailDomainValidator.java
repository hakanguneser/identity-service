package com.gastroblue.util;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.model.enums.ErrorCode;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailDomainValidator {

  public static void validateAllowedDomains(
      List<String> allowedDomains, List<String> mailAddresses) {

    if (allowedDomains == null || allowedDomains.isEmpty()) {
      if (mailAddresses == null || mailAddresses.isEmpty()) {
        return;
      }
      throw new IllegalDefinitionException(
          ErrorCode.INVALID_MAIL_DOMAINS, "At least one domain must be specified");
    }

    Set<String> normalizedAllowedDomains =
        allowedDomains.stream().filter(Objects::nonNull).collect(Collectors.toSet());

    List<String> invalidMails =
        mailAddresses.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(mail -> !isDomainAllowed(mail, normalizedAllowedDomains))
            .toList();

    if (!invalidMails.isEmpty()) {
      throw new IllegalDefinitionException(
          ErrorCode.INVALID_MAIL_DOMAINS, DelimitedStringUtil.join(invalidMails));
    }
  }

  private static boolean isDomainAllowed(String mail, Set<String> allowedDomains) {
    int atIndex = mail.lastIndexOf('@');
    if (atIndex < 0 || atIndex == mail.length() - 1) {
      return false;
    }

    String domain = mail.substring(atIndex + 1).toLowerCase();
    return allowedDomains.contains(domain);
  }
}
