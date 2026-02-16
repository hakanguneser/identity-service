package com.gastroblue.service.impl;

import static com.gastroblue.util.DelimitedStringUtil.join;

import com.gastroblue.exception.DefinitionNotFoundException;
import com.gastroblue.exception.IllegalDefinitionException;
import com.gastroblue.mapper.CompanyGroupMapper;
import com.gastroblue.model.base.CompanyGroup;
import com.gastroblue.model.base.SessionUser;
import com.gastroblue.model.entity.CompanyGroupEntity;
import com.gastroblue.model.enums.ErrorCode;
import com.gastroblue.model.request.CompanyGroupSaveRequest;
import com.gastroblue.model.request.CompanyGroupUpdateRequest;
import com.gastroblue.repository.CompanyGroupRepository;
import com.gastroblue.service.IJwtService;
import com.gastroblue.util.DelimitedStringUtil;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyGroupService {

  private final CompanyGroupRepository companyGroupRepository;

  public static final String DEFAULT_COMPANY_GROUP_ID = "*";

  public CompanyGroupEntity save(final CompanyGroupSaveRequest companyGroupRequest) {
    CompanyGroupEntity entityToBeSave = CompanyGroupMapper.toEntity(companyGroupRequest);
    return companyGroupRepository.save(entityToBeSave);
  }

  public CompanyGroupEntity update(String companyGroupId, CompanyGroupUpdateRequest request) {
    CompanyGroupEntity entityToBeUpdate =
        companyGroupRepository
            .findById(companyGroupId)
            .orElseThrow(
                () -> {
                  log.debug("Company Group not found for update with id: {}", companyGroupId);
                  return new DefinitionNotFoundException(ErrorCode.COMPANY_GROUP_NOT_FOUND);
                });
    List<String> mailDomains =
        request.mailDomains().stream().map(String::trim).map(String::toLowerCase).toList();
    List<String> mailGroupMails =
        request.groupMails().stream().map(String::trim).map(String::toLowerCase).toList();
    entityToBeUpdate.setName(request.name());
    entityToBeUpdate.setGroupCode(request.groupCode());
    entityToBeUpdate.setGroupMail(join(mailGroupMails));
    entityToBeUpdate.setLogoUrl(request.logoUrl());
    if (request.thermometerTrackerEnabled() != null) {
      entityToBeUpdate.setThermometerTrackerEnabled(request.thermometerTrackerEnabled());
      entityToBeUpdate.setThermometerTrackerApiUrl(request.thermometerTrackerApiUrl());
      entityToBeUpdate.setThermometerTrackerApiVersion(request.thermometerTrackerApiVersion());
    }
    if (request.formflowEnabled() != null) {
      entityToBeUpdate.setFormflowEnabled(request.formflowEnabled());
      entityToBeUpdate.setFormflowApiUrl(request.formflowApiUrl());
      entityToBeUpdate.setFormflowApiVersion(request.formflowApiVersion());
    }
    entityToBeUpdate.setMailDomains(join(mailDomains));
    checkCompanyAvailableMailDomains(mailDomains, mailGroupMails);
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
            .orElseThrow(
                () -> {
                  log.debug("Company Group not found with id: {}", companyGroupId);
                  return new IllegalDefinitionException(
                      ErrorCode.COMPANY_GROUP_NOT_FOUND, "Company Group not found");
                });
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

  public CompanyGroup findById(String companyGroupId) {
    return companyGroupRepository
        .findById(companyGroupId)
        .map(CompanyGroupMapper::toBase)
        .orElseThrow(
            () -> {
              log.debug("Company Group not found with id: {}", companyGroupId);
              return new IllegalDefinitionException(
                  ErrorCode.COMPANY_GROUP_NOT_FOUND, "Company Group not found");
            });
  }

  public CompanyGroupEntity findByGroupCode(String groupCode) {
    return companyGroupRepository
        .findByGroupCode(groupCode)
        .orElseThrow(
            () -> {
              log.debug("Company Group not found with code: {}", groupCode);
              return new IllegalDefinitionException(
                  ErrorCode.COMPANY_GROUP_NOT_FOUND, "Group not found: " + groupCode);
            });
  }

  public void checkCompanyAvailableMailDomains(
      List<String> allowedDomains, List<String> mailAddresses) {

    if (allowedDomains == null || allowedDomains.isEmpty()) {
      throw new IllegalDefinitionException(
          ErrorCode.INVALID_MAIL_DOMAINS, "At least one domain must be specified");
    }

    Set<String> normalizedAllowedDomains =
        allowedDomains.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

    List<String> invalidMails =
        mailAddresses.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(mail -> !isDomainAllowed(mail, normalizedAllowedDomains))
            .toList();

    if (!invalidMails.isEmpty()) {
      throw new IllegalDefinitionException(
          ErrorCode.INVALID_MAIL_DOMAINS, DelimitedStringUtil.join(invalidMails));
    }
  }

  private boolean isDomainAllowed(String mail, Set<String> allowedDomains) {
    int atIndex = mail.lastIndexOf('@');
    if (atIndex < 0 || atIndex == mail.length() - 1) {
      return false;
    }

    String domain = mail.substring(atIndex + 1).toLowerCase();
    return allowedDomains.contains(domain);
  }
}
