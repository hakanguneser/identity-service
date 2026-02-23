package com.gastroblue.mail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Renders HTML email templates stored under {@code resources/templates/mail/}.
 *
 * <p>Template variables use the {@code {{key}}} syntax. Example:
 *
 * <pre>
 * Hello {{username}}, welcome to {{appName}}!
 * </pre>
 *
 * <p>To switch to a more powerful engine (e.g. Thymeleaf or FreeMarker) in the future, replace only
 * this class – the rest of the mail infrastructure is unaffected.
 */
@Component
public class MailTemplateRenderer {

  private static final String TEMPLATE_BASE_PATH = "templates/mail/";
  private static final String TEMPLATE_EXTENSION = ".html";

  /**
   * Loads the template file and substitutes {@code {{key}}} placeholders with values from {@code
   * params}.
   *
   * @param template the template to render
   * @param params substitution map; keys must match placeholders without braces
   * @return rendered HTML string
   * @throws MailTemplateException if the template file cannot be read
   */
  public String render(MailTemplate template, Map<String, Object> params) {
    String path = TEMPLATE_BASE_PATH + template.getTemplateName() + TEMPLATE_EXTENSION;
    String content = loadTemplate(path);
    return substitute(content, params);
  }

  private String loadTemplate(String path) {
    try {
      ClassPathResource resource = new ClassPathResource(path);
      try (InputStream is = resource.getInputStream()) {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new MailTemplateException("Failed to load mail template from path: " + path, e);
    }
  }

  private String substitute(String template, Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return template;
    }
    String result = template;
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      String placeholder = "{{" + entry.getKey() + "}}";
      String value = entry.getValue() != null ? entry.getValue().toString() : "";
      result = result.replace(placeholder, value);
    }
    return result;
  }

  /** Thrown when a mail template file cannot be loaded or parsed. */
  public static class MailTemplateException extends RuntimeException {
    public MailTemplateException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
