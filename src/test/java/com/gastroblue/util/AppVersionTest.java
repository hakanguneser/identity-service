package com.gastroblue.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gastroblue.commons.helper.exception.type.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AppVersionTest {

  @Test
  void partsAreComparedAsNumbers() {
    assertThat(AppVersion.parse("1.9.0").isOlderThan(AppVersion.parse("1.10.0"))).isTrue();
    assertThat(AppVersion.parse("1.10.0").isOlderThan(AppVersion.parse("1.9.0"))).isFalse();
  }

  @Test
  void missingPartsCountAsZero() {
    assertThat(AppVersion.parse("1.4")).isEqualTo(AppVersion.parse("1.4.0"));
    assertThat(AppVersion.parse("2")).isEqualTo(AppVersion.parse("2.0.0.0"));
  }

  @Test
  void buildSuffixIsIgnored() {
    assertThat(AppVersion.parse("1.4.2+15")).isEqualTo(AppVersion.parse("1.4.2"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "abc", "1..2", "1.2.3.4.5", "v1.2", "1.2-beta"})
  void anInvalidVersionIsRejected(String value) {
    assertThatThrownBy(() -> AppVersion.parse(value)).isInstanceOf(BusinessException.class);
  }
}
