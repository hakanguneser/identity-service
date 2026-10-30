package com.gastroblue.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmailDomainValidatorTest {

  @Test
  void anyMailIsAcceptedWhenNoDomainIsDefined() {
    List<String> mails = List.of("a@gmail.com", "b@hotel.com.tr");

    assertThatCode(() -> EmailDomainValidator.validateAllowedDomains(null, mails))
        .doesNotThrowAnyException();
    assertThatCode(() -> EmailDomainValidator.validateAllowedDomains(List.of(), mails))
        .doesNotThrowAnyException();
    assertThatCode(
            () -> EmailDomainValidator.validateAllowedDomains(Arrays.asList(" ", null), mails))
        .doesNotThrowAnyException();
    assertThat(EmailDomainValidator.isDomainAllowed("a@gmail.com", "")).isTrue();
    assertThat(EmailDomainValidator.isDomainAllowed("a@gmail.com", (String) null)).isTrue();
  }

  @Test
  void definedDomainsStillRestrict() {
    List<String> domains = List.of("hotel.com.tr");

    assertThatCode(
            () -> EmailDomainValidator.validateAllowedDomains(domains, List.of("b@hotel.com.tr")))
        .doesNotThrowAnyException();
    assertThatThrownBy(
            () -> EmailDomainValidator.validateAllowedDomains(domains, List.of("a@gmail.com")))
        .isInstanceOf(NotFoundException.class);
    assertThat(EmailDomainValidator.isDomainAllowed("a@gmail.com", "hotel.com.tr")).isFalse();
  }

  @Test
  void noMailsAreValidAgainstDefinedDomains() {
    assertThatCode(() -> EmailDomainValidator.validateAllowedDomains(List.of("hotel.com.tr"), null))
        .doesNotThrowAnyException();
  }
}
