package com.gastroblue.model.response.tracker;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackerCompanyUsersResponse {
  private List<TrackerUser> users;
}
