package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupEulaContentMapper;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyGroupEulaContentEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.CompanyGroupEulaContentSaveRequest;
import com.gastroblue.model.request.CompanyGroupEulaContentUpdateRequest;
import com.gastroblue.repository.CompanyGroupEulaContentRepository;
import com.gastroblue.service.IJwtService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyGroupEulaContentService {

  private final CompanyGroupEulaContentRepository eulaContentRepository;
  private final CompanyGroupService companyGroupService;

  @Transactional
  public CompanyGroupEulaContentEntity create(
      String companyGroupId, CompanyGroupEulaContentSaveRequest request) {
    // Validate company group exists
    companyGroupService.findByIdOrThrow(companyGroupId);

    CompanyGroupEulaContentEntity entity =
        CompanyGroupEulaContentMapper.toEntity(companyGroupId, request);
    return eulaContentRepository.save(entity);
  }

  @Transactional
  public CompanyGroupEulaContentEntity update(
      String companyGroupId, String id, CompanyGroupEulaContentUpdateRequest request) {
    // Validate company group exists (optional check for consistency)
    companyGroupService.findByIdOrThrow(companyGroupId);

    CompanyGroupEulaContentEntity entity = findByIdOrThrow(id);

    if (!entity.getCompanyGroupId().equals(companyGroupId)) {
      throw new IllegalDefinitionException(
          ErrorCode.EULA_CONTENT_NOT_FOUND,
          "EULA Content does not belong to the specified Company Group");
    }

    if (request.eulaVersion() != null) {
      entity.setEulaVersion(request.eulaVersion());
    }
    if (request.language() != null) {
      entity.setLanguage(request.language());
    }
    if (request.content() != null) {
      entity.setContent(request.content());
    }
    if (request.startDate() != null) {
      entity.setStartDate(request.startDate());
    }
    // End date can be set to null, so we always set it if present in request?
    // Records handle nulls. If field is null in request, do we mean "no change" or
    // "set to null"?
    // Typically for update APIs, null means no change.
    // If we want to clear it, we might need a specific flag or convention.
    // However, usually end date is updated to close a version.
    if (request.endDate() != null) {
      entity.setEndDate(request.endDate());
    }

    return eulaContentRepository.save(entity);
  }

  public CompanyGroupEulaContentEntity findByIdOrThrow(String id) {
    return eulaContentRepository
        .findById(id)
        .orElseThrow(
            () -> {
              log.debug("EULA Content not found with id: {}", id);
              return new IllegalDefinitionException(
                  ErrorCode.EULA_CONTENT_NOT_FOUND, "EULA Content not found id: " + id);
            });
  }

  public List<CompanyGroupEulaContentEntity> findAllByCompanyGroupId(String companyGroupId) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    return eulaContentRepository.findAllByCompanyGroupId(companyGroupId);
  }

  @Transactional
  public void delete(String companyGroupId, String id) {
    companyGroupService.findByIdOrThrow(companyGroupId);
    CompanyGroupEulaContentEntity entity = findByIdOrThrow(id);

    if (!entity.getCompanyGroupId().equals(companyGroupId)) {
      throw new IllegalDefinitionException(
          ErrorCode.EULA_CONTENT_NOT_FOUND,
          "EULA Content does not belong to the specified Company Group");
    }
    eulaContentRepository.delete(entity);
  }

  public String getActiveEulaContent(final String companyGroupId) {
    SessionUser sessionUser = IJwtService.findSessionUserOrThrow();
    return eulaContentRepository
        .findActiveContent(
            companyGroupId,
            sessionUser.getApplicationProduct(),
            sessionUser.getLanguage(),
            LocalDate.now())
        .orElseThrow()
        .getContent();
  }
}
