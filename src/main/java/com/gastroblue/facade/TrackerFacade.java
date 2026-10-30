package com.gastroblue.facade;

import com.gastroblue.client.ExpoPushClient;
import com.gastroblue.client.ExpoPushResult;
import com.gastroblue.client.ExpoPushTokens;
import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.lookup.service.ILookupService;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.mapper.TrackerMapper;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.enums.PushNotificationError;
import com.gastroblue.model.enums.PushNotificationResultType;
import com.gastroblue.model.request.PushNotificationRequest;
import com.gastroblue.model.request.TrackerUsersByIdsRequest;
import com.gastroblue.model.response.tracker.PushNotificationErrorDetail;
import com.gastroblue.model.response.tracker.PushNotificationResponse;
import com.gastroblue.model.response.tracker.PushNotificationResult;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyUsersResponse;
import com.gastroblue.model.response.tracker.TrackerUser;
import com.gastroblue.service.CompanyGroupService;
import com.gastroblue.service.CompanyService;
import com.gastroblue.service.UserDefinitionService;
import com.gastroblue.service.UserProductService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackerFacade {

  private final UserDefinitionService userDefinitionService;
  private final UserProductService userProductService;
  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final ILookupService lookupService;
  private final ExpoPushClient expoPushClient;

  public TrackerCompanyUsersResponse findCompanyUsers(String companyGroupId, String companyId) {
    requireTrackerProduct();
    companyService.findByCompanyGroupIdAndId(companyGroupId, companyId);
    ApplicationProduct product = IJwtService.findSessionUserOrThrow().getApplicationProduct();
    List<UserEntity> users =
        userDefinitionService.findActiveByCompanyGroupIdAndCompanyIdAndProduct(
            companyGroupId, companyId, product);
    List<String> userIds = users.stream().map(UserEntity::getId).toList();
    Map<String, UserProductEntity> userProductMap =
        userIds.isEmpty()
            ? Map.of()
            : userProductService.findByUserIdInAndProduct(userIds, product).stream()
                .collect(Collectors.toMap(UserProductEntity::getUserId, up -> up));
    List<TrackerUser> items =
        users.stream()
            .map(u -> TrackerMapper.toUser(u, userProductMap.get(u.getId()), lookupService))
            .toList();
    return TrackerCompanyUsersResponse.builder().users(items).build();
  }

  public TrackerCompanyUsersResponse findUsersByIds(TrackerUsersByIdsRequest request) {
    requireTrackerProduct();
    List<String> ids = request.userIdList();
    List<String> orderedDistinct = new ArrayList<>(new LinkedHashSet<>(ids));
    if (orderedDistinct.isEmpty()) {
      return TrackerCompanyUsersResponse.builder().users(List.of()).build();
    }
    ApplicationProduct product = ApplicationProduct.TRACKER;
    Map<String, UserProductEntity> userProductMap =
        userProductService.findByUserIdInAndProduct(orderedDistinct, product).stream()
            .collect(Collectors.toMap(UserProductEntity::getUserId, up -> up, (a, b) -> a));
    Map<String, UserEntity> userMap =
        userDefinitionService.findAllByIdIn(orderedDistinct).stream()
            .collect(Collectors.toMap(UserEntity::getId, u -> u));
    List<TrackerUser> users =
        orderedDistinct.stream()
            .map(
                id -> {
                  UserEntity user = userMap.get(id);
                  UserProductEntity userProduct = userProductMap.get(id);
                  if (user == null || userProduct == null) {
                    return null;
                  }
                  if (StringUtils.hasText(request.companyGroupId())
                      && !request.companyGroupId().equals(user.getCompanyGroupId())) {
                    return null;
                  }
                  return TrackerMapper.toUser(user, userProduct, lookupService);
                })
            .filter(Objects::nonNull)
            .toList();
    return TrackerCompanyUsersResponse.builder().users(users).build();
  }

  public TrackerCompanyContextResponse findCompanyContextByCodes(
      String companyGroupCode, String companyCode) {
    requireTrackerProduct();
    CompanyGroupEntity group = companyGroupService.findByGroupCode(companyGroupCode);
    CompanyEntity company =
        companyService
            .findByCompanyCode(companyCode)
            .orElseThrow(
                () -> {
                  log.debug("Company not found with code: {}", companyCode);
                  return new NotFoundException(
                      ErrorCode.COMPANY_NOT_FOUND, "Company not found: " + companyCode);
                });
    if (!company.getCompanyGroupId().equals(group.getId())) {
      throw new NotFoundException(
          ErrorCode.COMPANY_NOT_FOUND,
          "Company not found in group: " + companyGroupCode + " / " + companyCode);
    }
    return TrackerMapper.toCompanyContextResponse(group, company, lookupService);
  }

  /**
   * Sends the notification synchronously and returns one result per requested user. Users without a
   * TRACKER push token get {@code NoTokenFound}; token level Expo errors are mapped back to the
   * users that own the token.
   */
  public PushNotificationResponse sendPushNotifications(PushNotificationRequest request) {
    // requireTrackerProduct();
    List<String> userIds = new ArrayList<>(new LinkedHashSet<>(request.userIdList()));

    Map<String, String> tokenByUserId =
        userProductService.findByUserIdInAndProduct(userIds, ApplicationProduct.TRACKER).stream()
            .filter(up -> StringUtils.hasText(up.getPushToken()))
            .collect(
                Collectors.toMap(
                    UserProductEntity::getUserId,
                    up -> ExpoPushTokens.normalize(up.getPushToken()),
                    (first, second) -> first));

    Map<String, ExpoPushResult> resultByToken =
        expoPushClient
            .sendNotifications(
                tokenByUserId.values().stream().distinct().toList(),
                request.title(),
                request.body())
            .stream()
            .collect(Collectors.toMap(ExpoPushResult::token, r -> r, (first, second) -> first));

    List<PushNotificationResult> results =
        userIds.stream()
            .map(userId -> toPushResult(userId, tokenByUserId.get(userId), resultByToken))
            .toList();
    int successCount =
        (int) results.stream().filter(r -> r.result() == PushNotificationResultType.OK).count();

    log.info(
        "Tracker push sent: recipients={}, success={}, failure={}",
        results.size(),
        successCount,
        results.size() - successCount);

    return PushNotificationResponse.builder()
        .recipientCount(results.size())
        .successCount(successCount)
        .failureCount(results.size() - successCount)
        .results(results)
        .build();
  }

  private static PushNotificationResult toPushResult(
      String userId, String token, Map<String, ExpoPushResult> resultByToken) {
    if (token == null) {
      return failedResult(userId, PushNotificationError.NO_TOKEN_FOUND.getCode());
    }
    ExpoPushResult expoResult = resultByToken.get(token);
    if (expoResult == null) {
      return failedResult(userId, PushNotificationError.UNKNOWN.getCode());
    }
    if (expoResult.ok()) {
      return PushNotificationResult.builder()
          .userId(userId)
          .result(PushNotificationResultType.OK)
          .build();
    }
    return failedResult(userId, expoResult.errorCode());
  }

  /** Keeps Expo's own error code even when it is not one of the known codes. */
  private static PushNotificationResult failedResult(String userId, String errorCode) {
    return PushNotificationResult.builder()
        .userId(userId)
        .result(PushNotificationResultType.NOK)
        .error(
            PushNotificationErrorDetail.builder()
                .expoError(errorCode)
                .errorDescription(PushNotificationError.fromExpoCode(errorCode).getDescription())
                .build())
        .build();
  }

  private static void requireTrackerProduct() {
    SessionUser session = IJwtService.findSessionUserOrThrow();
    if (session.getApplicationProduct() != ApplicationProduct.TRACKER) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Tracker API requires TRACKER product context");
    }
  }
}
