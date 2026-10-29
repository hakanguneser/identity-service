package com.gastroblue.model.response;

import com.gastroblue.commons.helper.lookup.model.dto.BaseLookupModel;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DropdownResponse {
  private List<BaseLookupModel> items;
}
