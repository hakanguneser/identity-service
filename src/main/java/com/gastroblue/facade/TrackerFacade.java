package com.gastroblue.facade;

import com.gastroblue.exception.AccessDeniedException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.TrackerMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyEntity;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.entity.UserEntity;
import com.gastroblue.model.entity.UserProductEntity;
import com.gastroblue.model.enums.ApplicationProduct;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.response.tracker.TrackerCompanyContextResponse;
import com.gastroblue.model.response.tracker.TrackerCompanyUsersResponse;
import com.gastroblue.model.response.tracker.TrackerUser;
import com.gastroblue.service.IJwtService;
import com.gastroblue.service.impl.CompanyGroupService;
import com.gastroblue.service.impl.CompanyService;
import com.gastroblue.service.impl.UserDefinitionService;
import com.gastroblue.service.impl.UserProductService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackerFacade {

  private final UserDefinitionService userDefinitionService;
  private final UserProductService userProductService;
  private final CompanyService companyService;
  private final CompanyGroupService companyGroupService;
  private final EnumConfigurationFacade enumConfigurationFacade;

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
                  return new IllegalDefinitionException(
                      ErrorCode.COMPANY_NOT_FOUND, "Company not found: " + companyCode);
                });
    if (!company.getCompanyGroupId().equals(group.getId())) {
      throw new IllegalDefinitionException(
          ErrorCode.COMPANY_NOT_FOUND,
          "Company not found in group: " + companyGroupCode + " / " + companyCode);
    }
    return TrackerMapper.toCompanyContextResponse(group, company, enumConfigurationFacade);
  }

  private static void requireTrackerProduct() {
    SessionUser session = IJwtService.findSessionUserOrThrow();
    if (session.getApplicationProduct() != ApplicationProduct.TRACKER) {
      throw new AccessDeniedException(
          ErrorCode.ACCESS_DENIED, "Tracker API requires TRACKER product context");
    }
  }
}
