-- =====================================================================
-- Mobile app version check (GET /api/v1/app-versions/check, called before login).
-- One row per product and platform:
--   min_supported_version: an older app must update (REQUIRED).
--   latest_version:        an older app is offered the update (OPTIONAL).
-- Raise min_supported_version only after the store rollout reaches 100%; a user who cannot see
-- the new version in the store yet would otherwise be locked out.
-- No seed data: rows are inserted by hand.
-- =====================================================================

CREATE TABLE application_versions (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    product               varchar(36)      NOT NULL,
    platform              varchar(10)      NOT NULL,
    latest_version        varchar(20)      NOT NULL,
    min_supported_version varchar(20)      NOT NULL,
    store_url             varchar(500)     NOT NULL,
    is_active             bool             NOT NULL,
    CONSTRAINT pk_application_versions PRIMARY KEY (id),
    -- Same format AppVersion parses; a typo here would otherwise fail every check at runtime.
    CONSTRAINT ck_application_versions_latest CHECK (latest_version ~ '^[0-9]+(\.[0-9]+){0,3}$'),
    CONSTRAINT ck_application_versions_min CHECK (min_supported_version ~ '^[0-9]+(\.[0-9]+){0,3}$')
);

-- findByProductAndPlatformAndActiveTrue (every app start); one row per product and platform.
CREATE UNIQUE INDEX ux_application_versions_product_platform ON application_versions (product, platform);
