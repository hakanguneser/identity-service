package com.gastroblue.service;

import static com.gastroblue.commons.shared.util.DelimitedStringUtil.join;

import com.gastroblue.commons.helper.exception.type.NotFoundException;
import com.gastroblue.commons.helper.security.model.dto.SessionUser;
import com.gastroblue.commons.helper.security.service.IJwtService;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.repository.CompanyGroupRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyGroupService {

  private final CompanyGroupRepository companyGroupRepository;

  public CompanyGroupEntity save(CompanyGroupSaveRequest request) {
    CompanyGroupEntity entity = CompanyGroupMapper.toEntity(request);
    return companyGroupRepository.save(entity);
  }

  public CompanyGroupEntity update(String companyGroupId, CompanyGroupUpdateRequest request) {
    CompanyGroupEntity entityToBeUpdate =
        companyGroupRepository
            .findById(companyGroupId)
            .orElseThrow(
                () -> {
                  log.debug("Company Group not found for update with id: {}", companyGroupId);
                  return new NotFoundException(
                      ErrorCode.COMPANY_GROUP_NOT_FOUND, "Company Group not found");
                });
    entityToBeUpdate.setName(request.name());
    entityToBeUpdate.setGroupMail(join(request.groupMails()));
    entityToBeUpdate.setLogoUrl(request.logoUrl());
    entityToBeUpdate.setMailDomains(join(request.mailDomains()));
    return companyGroupRepository.save(entityToBeUpdate);
  }

  public List<CompanyGroupEntity> findAll() {
    return companyGroupRepository.findAll().stream().toList();
  }

  public CompanyGroupEntity findByIdOrThrow(final String companyGroupId) {
    if (companyGroupId == null) {
      throw new NotFoundException(
          ErrorCode.COMPANY_GROUP_NOT_FOUND, "Company group ID must not be null");
    }
    return companyGroupRepository
        .findById(companyGroupId)
        .orElseThrow(
            () -> {
              log.debug("Company Group not found with id: {}", companyGroupId);
              return new NotFoundException(
                  ErrorCode.COMPANY_GROUP_NOT_FOUND, "Company Group not found");
            });
  }

  public List<CompanyGroupEntity> findMyCompanyGroups() {
    SessionUser user = IJwtService.findSessionUserOrThrow();
    return switch (user.getApplicationRole()) {
      case ADMIN -> findAll();
      case GROUP_MANAGER, COMPANY_MANAGER, SUPERVISOR ->
          List.of(findByIdOrThrow(user.companyGroupId()));
      default -> List.of();
    };
  }

  public CompanyGroup findCompanyByIdOrThrow(String companyGroupId) {
    return companyGroupRepository
        .findById(companyGroupId)
        .map(CompanyGroupMapper::toBase)
        .orElseThrow(
            () -> {
              log.debug("Company Group not found with id: {}", companyGroupId);
              return new NotFoundException(
                  ErrorCode.COMPANY_GROUP_NOT_FOUND, "Company Group not found");
            });
  }

  public CompanyGroupEntity findByGroupCode(String groupCode) {
    return companyGroupRepository
        .findByGroupCode(groupCode)
        .orElseThrow(
            () -> {
              log.debug("Company Group not found with code: {}", groupCode);
              return new NotFoundException(
                  ErrorCode.COMPANY_GROUP_NOT_FOUND, "Group not found: " + groupCode);
            });
  }

  public Optional<CompanyGroupEntity> findById(String companyGroupId) {
    return companyGroupRepository.findById(companyGroupId);
  }
}
