package com.gastroblue.model.request;


import com.gastroblue.model.enums.Language;

public record ApplicationPropertySaveRequest(
        String propertyKey, Language language, String propertyValue, String description) {}
