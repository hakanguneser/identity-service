package com.gastroblue.model.response;

import com.gastroblue.model.enums.Language;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CompanyGroupEulaContentResponse {
  private String id;
  private String companyGroupId;
  private String eulaVersion;
  private Language language;
  private String content;
  private LocalDateTime startDate;
  private LocalDateTime endDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
