package com.gastroblue.model.base;

public interface ConfigurableEnum extends DefaultConfigurableEnum {

  @Override
  default boolean isDefault() {
    return false;
  }
}
