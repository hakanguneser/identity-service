package com.gastroblue.facade;

import com.gastroblue.model.entity.ErrorMessageEntity;
import com.gastroblue.model.request.ErrorMessageSaveRequest;
import com.gastroblue.model.request.ErrorMessageUpdateRequest;
import com.gastroblue.model.response.ErrorMessageResponse;
import com.gastroblue.service.impl.ErrorMessageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ErrorMessageFacade {

  private final ErrorMessageService service;

  public ErrorMessageResponse create(ErrorMessageSaveRequest request) {
    ErrorMessageEntity entity =
        ErrorMessageEntity.builder()
            .errorCode(request.errorCode())
            .language(request.language())
            .message(request.message())
            .build();
    return toResponse(service.save(entity));
  }

  public ErrorMessageResponse update(String id, ErrorMessageUpdateRequest request) {
    ErrorMessageEntity entity = service.findById(id);
    if (request.message() != null) {
      entity.setMessage(request.message());
    }
    return toResponse(service.save(entity));
  }

  public ErrorMessageResponse findById(String id) {
    return toResponse(service.findById(id));
  }

  public List<ErrorMessageResponse> findAll() {
    return service.findAll().stream().map(this::toResponse).toList();
  }

  public void delete(String id) {
    service.delete(id);
  }

  private ErrorMessageResponse toResponse(ErrorMessageEntity entity) {
    return ErrorMessageResponse.builder()
        .id(entity.getId())
        .errorCode(entity.getErrorCode())
        .language(entity.getLanguage())
        .message(entity.getMessage())
        .build();
  }
}
