package com.gastroblue.service.impl;

import static com.gastroblue.util.DelimitedStringUtil.join;

import com.gastroblue.exception.DefinitionNotFoundException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.repository.CompanyGroupRepository;
import com.gastroblue.service.IJwtService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            .orElseThrow(IllegalDefinitionException::new);
    entityToBeUpdate.setName(request.name());
    entityToBeUpdate.setGroupCode(request.groupCode());
    entityToBeUpdate.setGroupMail(join(request.groupMails()));
    entityToBeUpdate.setLogoUrl(request.logoUrl());
    return companyGroupRepository.save(entityToBeUpdate);
  }

  public List<CompanyGroupEntity> findAll() {
    return companyGroupRepository.findAll().stream().toList();
  }

  public CompanyGroupEntity findByIdOrThrow(final String companyGroupId) {
    return companyGroupId == null
        ? null
        : companyGroupRepository
            .findById(companyGroupId)
            .orElseThrow(IllegalDefinitionException::new);
  }

  public List<CompanyGroupEntity> findMyCompanyGroups() {
    SessionUser user = IJwtService.findSessionUserOrThrow();
    return switch (user.applicationRole()) {
      case ADMIN -> findAll();
      case GROUP_MANAGER, COMPANY_MANAGER, SUPERVISOR ->
          List.of(findByIdOrThrow(user.companyGroupId()));
      default -> null;
    };
  }

  public CompanyGroup findByBaseId(String companyGroupId) {
    return companyGroupRepository
        .findById(companyGroupId)
        .map(CompanyGroupMapper::toBase)
        .orElseThrow(IllegalDefinitionException::new);
  }

  public CompanyGroupEntity findByGroupCode(String groupCode) {
    return companyGroupRepository
        .findByGroupCode(groupCode)
        .orElseThrow(() -> new DefinitionNotFoundException("Group not found: " + groupCode));
  }
}
