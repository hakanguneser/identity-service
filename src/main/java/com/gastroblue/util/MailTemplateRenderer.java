package com.gastroblue.util;

import com.gastroblue.model.enums.MailTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;

/**
 * Renders HTML email templates stored under {@code resources/templates/mail/}.
 *
 * <p>Supports two syntaxes:
 *
 * <ul>
 *   <li><b>Variable</b>: {@code {{key}}} – replaced with the string value of the parameter.
 *   <li><b>Conditional block</b>: {@code {{#key}}...{{/key}}} – the block is included only when the
 *       parameter value is {@link Boolean#TRUE}; otherwise the entire block is removed. Missing
 *       keys default to {@code false} (block hidden).
 * </ul>
 *
 * <p>To switch to a more powerful engine (e.g. Thymeleaf or FreeMarker) in the future, replace only
 * this class – the rest of the mail infrastructure is unaffected.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MailTemplateRenderer {

  private static final String TEMPLATE_BASE_PATH = "templates/mail/";
  private static final String TEMPLATE_EXTENSION = ".html";

  /**
   * Loads the template file, resolves conditional blocks, then substitutes {@code {{key}}}
   * placeholders with values from {@code params}.
   *
   * @param template the template to render
   * @param params substitution map; boolean values control conditional blocks
   * @return rendered HTML string
   * @throws MailTemplateException if the template file cannot be read
   */
  public static String render(MailTemplate template, Map<String, Object> params) {
    String path = TEMPLATE_BASE_PATH + template.getTemplateName() + TEMPLATE_EXTENSION;
    String content = loadTemplate(path);
    content = resolveConditionals(content, params);
    return substitute(content, params);
  }

  // ── Private helpers ──────────────────────────────────────────────────────────

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

  /**
   * Resolves {@code {{#key}}...{{/key}}} conditional blocks.
   *
   * <ul>
   *   <li>If the param value is {@link Boolean#TRUE} → the block content is kept (tags removed).
   *   <li>Otherwise → the entire block including its content is removed.
   * </ul>
   *
   * <p>Blocks are resolved before variable substitution so that variables inside a removed block
   * are never evaluated.
   */
  private static String resolveConditionals(String content, Map<String, Object> params) {
    // Matches {{#key}} ... {{/key}} across multiple lines (DOTALL)
    Pattern pattern = Pattern.compile("\\{\\{#(\\w+)\\}\\}(.*?)\\{\\{/(\\1)\\}\\}", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String key = matcher.group(1);
      String blockContent = matcher.group(2);
      boolean show = Boolean.TRUE.equals(params != null ? params.get(key) : null);
      matcher.appendReplacement(sb, show ? Matcher.quoteReplacement(blockContent) : "");
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /** Substitutes {@code {{key}}} placeholders with their string values. */
  private static String substitute(String template, Map<String, Object> params) {
    if (params == null || params.isEmpty()) {
      return template;
    }
    String result = template;
    for (Map.Entry<String, Object> entry : params.entrySet()) {
      // Skip boolean keys – they were handled by resolveConditionals
      if (entry.getValue() instanceof Boolean) continue;
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
