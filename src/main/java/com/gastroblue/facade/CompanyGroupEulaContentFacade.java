package com.gastroblue.facade;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupEulaContentMapper;
import com.gastroblue.model.entity.CompanyGroupEulaContentEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.CompanyGroupEulaContentSaveRequest;
import com.gastroblue.model.request.CompanyGroupEulaContentUpdateRequest;
import com.gastroblue.model.response.CompanyGroupEulaContentResponse;
import com.gastroblue.service.impl.CompanyGroupEulaContentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyGroupEulaContentFacade {

  private final CompanyGroupEulaContentService eulaContentService;

  public CompanyGroupEulaContentResponse create(
      String companyGroupId, CompanyGroupEulaContentSaveRequest request) {
    return CompanyGroupEulaContentMapper.toResponse(
        eulaContentService.create(companyGroupId, request));
  }

  public CompanyGroupEulaContentResponse update(
      String companyGroupId, String id, CompanyGroupEulaContentUpdateRequest request) {
    return CompanyGroupEulaContentMapper.toResponse(
        eulaContentService.update(companyGroupId, id, request));
  }

  public CompanyGroupEulaContentResponse findById(String companyGroupId, String id) {
    CompanyGroupEulaContentEntity entity = eulaContentService.findByIdOrThrow(id);
    if (!entity.getCompanyGroupId().equals(companyGroupId)) {
      throw new IllegalDefinitionException(
          ErrorCode.EULA_CONTENT_NOT_FOUND,
          "EULA Content does not belong to the specified Company Group");
    }
    return CompanyGroupEulaContentMapper.toResponse(entity);
  }

  public List<CompanyGroupEulaContentResponse> findAllByCompanyGroupId(String companyGroupId) {
    return eulaContentService.findAllByCompanyGroupId(companyGroupId).stream()
        .map(CompanyGroupEulaContentMapper::toResponse)
        .toList();
  }

  public void delete(String companyGroupId, String id) {
    eulaContentService.delete(companyGroupId, id);
  }
}
