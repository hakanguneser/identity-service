package com.gastroblue.service.impl;

import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.repository.CompanyGroupRepository;
import com.gastroblue.service.IJwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.gastroblue.model.enums.DefinitionType.COMPANY_GROUP;
import static com.gastroblue.util.DelimitedStringUtil.join;


@Service
@RequiredArgsConstructor
public class CompanyGroupService {

  private final CompanyGroupRepository companyGroupRepository;

  public CompanyGroupEntity save(final CompanyGroupSaveRequest companyGroupRequest) {
    CompanyGroupEntity entityToBeSave = CompanyGroupMapper.toEntity(companyGroupRequest);
    return companyGroupRepository.save(entityToBeSave);
  }

  public CompanyGroupEntity update(String companyGroupId, CompanyGroupUpdateRequest request) {
    CompanyGroupEntity entityToBeUpdate =
        companyGroupRepository
            .findById(companyGroupId)
            .orElseThrow(() -> new IllegalDefinitionException(COMPANY_GROUP));
    entityToBeUpdate.setName(request.name());
    entityToBeUpdate.setGroupCode(request.groupCode());
    entityToBeUpdate.setGroupMail(join(request.groupMails()));
    entityToBeUpdate.setLogoUrl(request.logoUrl());
    return companyGroupRepository.save(entityToBeUpdate);
  }

  public List<CompanyGroupEntity> findAll() {
    return companyGroupRepository.findAll().stream().toList();
  }

  public CompanyGroupEntity findById(final String companyGroupId) {
    return companyGroupId == null
        ? null
        : companyGroupRepository
            .findById(companyGroupId)
            .orElseThrow(() -> new IllegalDefinitionException(COMPANY_GROUP));
  }

  public List<CompanyGroupEntity> findMyCompanyGroups() {
    SessionUser user = IJwtService.findSessionUserOrThrow();
    return switch (user.applicationRole()) {
      case ADMIN -> findAll();
      case GROUP_MANAGER, COMPANY_MANAGER, SUPERVISOR -> List.of(findById(user.companyGroupId()));
      default -> null;
    };
  }

  public CompanyGroup findByBaseId(String companyGroupId) {
    return companyGroupRepository
        .findById(companyGroupId)
        .map(CompanyGroupMapper::toBase)
        .orElseThrow(() -> new IllegalDefinitionException(COMPANY_GROUP));
  }
}
