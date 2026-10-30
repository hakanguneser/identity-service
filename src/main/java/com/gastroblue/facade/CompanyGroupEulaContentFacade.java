package com.gastroblue.facade;

import com.gastroblue.mapper.CompanyGroupEulaContentMapper;
import com.gastroblue.model.entity.CompanyGroupEulaContentEntity;
import com.gastroblue.model.request.CompanyGroupEulaContentSaveRequest;
import com.gastroblue.model.request.CompanyGroupEulaContentUpdateRequest;
import com.gastroblue.model.response.CompanyGroupEulaContentResponse;
import com.gastroblue.model.response.CompanyGroupEulaContentsResponse;
import com.gastroblue.service.CompanyGroupEulaContentService;
import com.gastroblue.util.TenantAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyGroupEulaContentFacade {

  private final CompanyGroupEulaContentService eulaContentService;

  public CompanyGroupEulaContentResponse create(
      String companyGroupId, CompanyGroupEulaContentSaveRequest request) {
    TenantAccess.requireGroupManagement(companyGroupId);
    return CompanyGroupEulaContentMapper.toResponse(
        eulaContentService.create(companyGroupId, request));
  }

  public CompanyGroupEulaContentResponse update(
      String companyGroupId, String id, CompanyGroupEulaContentUpdateRequest request) {
    TenantAccess.requireGroupManagement(companyGroupId);
    return CompanyGroupEulaContentMapper.toResponse(
        eulaContentService.update(companyGroupId, id, request));
  }

  public CompanyGroupEulaContentResponse findById(String companyGroupId, String id) {
    TenantAccess.requireCompanyGroup(companyGroupId);
    CompanyGroupEulaContentEntity entity =
        eulaContentService.findByCompanyGroupIdAndId(companyGroupId, id);
    return CompanyGroupEulaContentMapper.toResponse(entity);
  }

  public CompanyGroupEulaContentsResponse findAllByCompanyGroupId(String companyGroupId) {
    TenantAccess.requireCompanyGroup(companyGroupId);
    return CompanyGroupEulaContentsResponse.builder()
        .eulaContents(
            eulaContentService.findAllByCompanyGroupId(companyGroupId).stream()
                .map(CompanyGroupEulaContentMapper::toSummary)
                .toList())
        .build();
  }

  public void delete(String companyGroupId, String id) {
    TenantAccess.requireGroupManagement(companyGroupId);
    eulaContentService.delete(companyGroupId, id);
  }
}
