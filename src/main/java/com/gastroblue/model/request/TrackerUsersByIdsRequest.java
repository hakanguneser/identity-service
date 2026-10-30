package com.gastroblue.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @param companyGroupId when set, users of other company groups are left out of the result. The
 *     tracker sends it whenever the request runs for a user session; system calls (alarm
 *     notifications) leave it empty.
 */
public record TrackerUsersByIdsRequest(
    @NotEmpty(message = "{validation.tracker.userIds.notEmpty}") List<@NotBlank String> userIdList,
    String companyGroupId) {}
