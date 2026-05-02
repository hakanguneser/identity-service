package com.gastroblue.facade;

import com.gastroblue.client.TrackerPushNotificationDispatcher;
import com.gastroblue.commons.helper.exception.type.AccessDeniedException;
import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.jwt.model.dto.SessionUser;
import com.gastroblue.commons.helper.jwt.service.IJwtService;
import com.gastroblue.commons.shared.enums.ApplicationProduct;
import com.gastroblue.mapper.TrackerMapper;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.PushNotificationRequest;
import com.gastroblue.model.request.TrackerUsersByIdsRequest;
import com.gastroblue.model.response.tracker.PushNotificationAcceptedResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyUsersResponse;
import com.gastroblue.model.response.tracker.TrackerUser;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.service.impl.UserDefinitionService;
import com.gastroblue.service.impl.UserProductService;
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
  private final EnumConfigurationFacade enumConfigurationFacade;
  private final TrackerPushNotificationDispatcher trackerPushNotificationDispatcher;

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
            .map(
                u ->
                    TrackerMapper.toUser(u, userProductMap.get(u.getId()), enumConfigurationFacade))
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
                  return TrackerMapper.toUser(user, userProduct, enumConfigurationFacade);
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
    return TrackerMapper.toCompanyContextResponse(group, company, enumConfigurationFacade);
  }

  public PushNotificationAcceptedResponse enqueuePushNotifications(
      PushNotificationRequest request) {
    requireTrackerProduct();
    List<UserProductEntity> userProducts =
        userProductService.findByUserIdInAndProduct(
            request.userIdList(), ApplicationProduct.TRACKER);
    List<String> tokens =
        userProducts.stream()
            .map(UserProductEntity::getPushToken)
            .filter(StringUtils::hasText)
            .map(String::trim)
            .distinct()
            .toList();
    if (!tokens.isEmpty()) {
      trackerPushNotificationDispatcher.dispatch(tokens, request.title(), request.body());
    }
    return PushNotificationAcceptedResponse.builder().recipientCount(tokens.size()).build();
  }

  private static void requireTrackerProduct() {
    SessionUser session = IJwtService.findSessionUserOrThrow();
    if (session.getApplicationProduct() != ApplicationProduct.TRACKER) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Tracker API requires TRACKER product context");
    }
  }
}
