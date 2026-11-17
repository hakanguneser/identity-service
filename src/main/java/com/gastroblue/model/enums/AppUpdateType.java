package com.gastroblue.model.enums;

/** What the app does after the version check. */
public enum AppUpdateType {
  /** The app is up to date, or no version is defined for it. */
  NONE,
  /** A newer version exists; the user may skip it. */
  OPTIONAL,
  /** The app is below the minimum supported version and must be updated from the store. */
  REQUIRED
}
