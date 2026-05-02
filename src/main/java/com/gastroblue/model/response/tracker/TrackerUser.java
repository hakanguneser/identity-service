package com.gastroblue.model.response.tracker;

import com.gastroblue.commons.shared.model.DisplayableLookupValue;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerUser {
  private String userId;
  private String companyId;
  private String companyGroupId;
  private String username;
  private String email;
  private Boolean isActive;
  private String name;
  private String surname;
  private String phone;
  private DisplayableLookupValue language;
  private DisplayableLookupValue gender;
  private DisplayableLookupValue zone;
  private DisplayableLookupValue applicationRole;
  private List<DisplayableLookupValue> departments;
}
