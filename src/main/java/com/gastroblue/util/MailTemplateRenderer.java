package com.gastroblue.util;

import com.gastroblue.model.enums.MailParameters;
import com.gastroblue.model.enums.MailTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailTemplateRenderer {

  private static final String TEMPLATE_BASE_PATH = "templates/mail/";
  private static final String TEMPLATE_EXTENSION = ".html";

  /**
   * Loads the template file and substitutes {@code {{key}}} placeholders with values from {@code
   * params}.
   *
   * @param templa e the template to render
   * @param params substitution map; keys must match placeholders without braces
   * @return rendered HTML string
   * @throws MailTemplateException if the template file cannot be read
   */
  public static String render(MailTemplate template, Map<MailParameters, Object> params) {
    String path = TEMPLATE_BASE_PATH + template.getTemplateName() + TEMPLATE_EXTENSION;
    String content = loadTemplate(path);
    return substitute(content, params);
  }

  private static String loadTemplate(String path) {
    try {
      ClassPathResource resource = new ClassPathResource(path);
      try (InputStream is = resource.getInputStream()) {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
      }
    } catch (IOException e) {
      throw new MailTemplateException("Failed to load mail template from path: " + path, e);
    }
  }

  private static String substitute(String template, Map<MailParameters, Object> params) {
    if (template == null || template.isBlank()) {
      return template;
    }
    String result = template;

    // 1. Handle Mustache-style conditional blocks: {{#key}}...{{/key}}
    if (params != null) {
      for (Map.Entry<MailParameters, Object> entry : params.entrySet()) {
        String key = entry.getKey().getKey();
        Object val = entry.getValue();
        boolean isActive = (val instanceof Boolean b && b);

        String blockRegex = "\\{\\{#" + key + "\\}\\}(.*?)\\{\\{/" + key + "\\}\\}";
        java.util.regex.Pattern pattern =
            java.util.regex.Pattern.compile(blockRegex, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(result);

        if (isActive) {
          result = matcher.replaceAll("$1");
        } else {
          result = matcher.replaceAll("");
        }
      }
    }

    // 2. Clear any remaining conditional blocks for keys not present in params
    String remainingBlocksRegex = "\\{\\{#.*?\\}\\}(.*?)\\{\\{/.*?\\}\\}";
    java.util.regex.Pattern p =
        java.util.regex.Pattern.compile(remainingBlocksRegex, java.util.regex.Pattern.DOTALL);
    result = p.matcher(result).replaceAll("");

    // 3. Handle simple variable substitutions: {{key}}
    if (params != null) {
      for (Map.Entry<MailParameters, Object> entry : params.entrySet()) {
        String placeholder = "{{" + entry.getKey().getKey() + "}}";
        String value = entry.getValue() != null ? entry.getValue().toString() : "";
        result = result.replace(placeholder, value);
      }
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
