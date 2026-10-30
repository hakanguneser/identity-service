package com.gastroblue.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.gastroblue.commons.helper.mail.util.MailTemplateLoader;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.model.enums.MailParameters;
import com.gastroblue.model.enums.MailTemplate;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PasswordMailTemplateTest {

  private final MailTemplateLoader loader = new MailTemplateLoader();

  @ParameterizedTest
  @EnumSource(MailTemplate.class)
  void trackerMailShowsTheLogo(MailTemplate template) {
    String html = render(template, ApplicationProduct.TRACKER, true);

    assertThat(html).contains("cid:tracker-logo").doesNotContain("GastroBlue Check APP");
    assertThat(html).contains("apps.apple.com/tr/app/gastroblue-tracker/id6759391154");
    assertThat(html)
        .contains("play.google.com/store/apps/details?id=com.gastroblueco.thermometertracking");
    assertCommon(html);
  }

  @ParameterizedTest
  @EnumSource(MailTemplate.class)
  void otherProductsShowTheProductNameInstead(MailTemplate template) {
    String html = render(template, ApplicationProduct.CHECK, false);

    assertThat(html).doesNotContain("cid:tracker-logo").contains("GastroBlue Check APP");
    assertThat(html).doesNotContain("App Store", "Google Play");
    assertThat(html).doesNotContain("Merhaba Ayşe Yönetici");
    assertCommon(html);
  }

  @ParameterizedTest
  @EnumSource(MailTemplate.class)
  void managerNoteOnlyWhenTheUserHasNoMail(MailTemplate template) {
    assertThat(render(template, ApplicationProduct.TRACKER, true))
        .contains("Merhaba Ayşe Yönetici");
  }

  private static void assertCommon(String html) {
    assertThat(html)
        .contains("Merhaba Ali Veli", "ali.veli", "K7PX2MQA", "Mutfak, Bar", "www.gastroblue.com")
        .doesNotContain("{{", "}}", "gastrobluecheckapp", "wixstatic");
  }

  private String render(MailTemplate template, ApplicationProduct product, boolean managerNote) {
    Map<MailParameters, Object> params = new EnumMap<>(MailParameters.class);
    params.put(MailParameters.FULL_NAME, "Ali Veli");
    params.put(MailParameters.USERNAME, "ali.veli");
    params.put(MailParameters.TEMPORARY_PASSWORD, "K7PX2MQA");
    params.put(MailParameters.ACTIVATE_MANAGER_NOTE, managerNote);
    params.put(MailParameters.MANAGER_FULL_NAME, "Ayşe Yönetici");
    params.put(MailParameters.APPLICATION_ROLE, "Şirket Yöneticisi");
    params.put(MailParameters.DEPARTMENT, "Mutfak, Bar");
    params.put(MailParameters.COMPANY_NAME, "Demo Company");
    params.put(MailParameters.COMPANY_GROUP_NAME, "Demo Group");
    params.put(MailParameters.TRACKER_LOGO, MailBranding.hasLogo(product));
    params.put(MailParameters.HEADER_TEXT, MailBranding.headerText(product));
    params.put(MailParameters.STORE_LINKS, MailBranding.hasStoreLinks(product));
    params.put(MailParameters.APP_STORE_URL, MailBranding.appStoreUrl(product));
    params.put(MailParameters.PLAY_STORE_URL, MailBranding.playStoreUrl(product));
    return loader.loadAndRender(template.getTemplateName(), params);
  }
}
