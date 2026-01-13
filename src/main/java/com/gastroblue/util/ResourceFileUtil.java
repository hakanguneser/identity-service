package com.gastroblue.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Component
@Slf4j
public class ResourceFileUtil {

  public String copyResourceToTemp(String resourcePath) {
    try {
      ClassPathResource resource = new ClassPathResource(resourcePath);

      String filename = resource.getFilename();
      if (filename == null) {
        throw new IllegalArgumentException("Resource filename alınamadı: " + resourcePath);
      }
      String tempDir = System.getProperty("java.io.tmpdir");
      File tempFile = new File(tempDir, filename);

      if (!tempFile.exists()) {
        try (InputStream is = resource.getInputStream();
            OutputStream os = new FileOutputStream(tempFile)) {
          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
          }
        }
        tempFile.setReadable(true, false);
        log.debug(
            "Resource '{}' tmp dizine kopyalandı: {}", resourcePath, tempFile.getAbsolutePath());
      } else {
        log.debug(
            "Resource '{}' zaten tmp dizinde mevcut: {}", resourcePath, tempFile.getAbsolutePath());
      }

      return tempFile.toURI().toString();

    } catch (Exception e) {
      log.error(
          "Resource '{}' tmp dizine kopyalanırken hata oluştu: {}", resourcePath, e.getMessage());
      return null;
    }
  }
}
