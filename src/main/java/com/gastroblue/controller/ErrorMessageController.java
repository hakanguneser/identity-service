package com.gastroblue.controller;

import com.gastroblue.facade.ErrorMessageFacade;
import com.gastroblue.model.request.ErrorMessageSaveRequest;
import com.gastroblue.model.request.ErrorMessageUpdateRequest;
import com.gastroblue.model.response.ErrorMessageResponse;
import com.gastroblue.model.shared.ResolvedEnum;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configuration/errors")
@RequiredArgsConstructor
public class ErrorMessageController {

  private final ErrorMessageFacade facade;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ErrorMessageResponse create(@RequestBody @Valid ErrorMessageSaveRequest request) {
    return facade.create(request);
  }

  @PutMapping("/{id}")
  public ErrorMessageResponse update(
      @PathVariable String id, @RequestBody @Valid ErrorMessageUpdateRequest request) {
    return facade.update(id, request);
  }

  @GetMapping("/{id}")
  public ErrorMessageResponse findById(@PathVariable String id) {
    return facade.findById(id);
  }

  @GetMapping
  public List<ErrorMessageResponse> findAll() {
    return facade.findAll();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable String id) {
    facade.delete(id);
  }

  @GetMapping("dropdown/languages")
  public ResponseEntity<List<ResolvedEnum>> findAllLanguages() {
    return ResponseEntity.ok(facade.getLanguages()); // TODO: Language support
  }
}
