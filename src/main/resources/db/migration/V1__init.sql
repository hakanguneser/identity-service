-- =====================================================================
-- identity-service :: initial schema (PostgreSQL 16)
-- Identity entities + gastroblue-commons tables (error_messages, outgoing_mail_log, lookup).
-- Column order: id, audit columns, version, then entity fields.
-- Enum columns are varchar without CHECK constraints, so a new enum value needs no migration.
--
-- Every index below serves a query in the code (repository named in the comment). No index
-- without a query: each one costs on every insert. Identity tables stay small (users in the
-- thousands), so a rare query without an index (user_products by push token, the product/role
-- subquery of findAccessibleUsers) is left to a scan instead of an index on every write.
--
-- Fresh install only: there is no seed data. The first ADMIN user is inserted by hand.
-- =====================================================================


-- ---------------------------------------------------------------------
-- company_groups
-- ---------------------------------------------------------------------
CREATE TABLE company_groups (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    "name"                varchar(100),
    group_code            varchar(25),
    group_mail            varchar(500),
    logo_url              varchar(100),
    mail_domains          varchar(255),
    CONSTRAINT pk_company_groups PRIMARY KEY (id)
);

-- CompanyGroupRepository.findByGroupCode (tracker context, login context); codes are unique.
CREATE UNIQUE INDEX ux_company_groups_group_code ON company_groups (group_code);


-- ---------------------------------------------------------------------
-- companies
-- ---------------------------------------------------------------------
CREATE TABLE companies (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    company_code          varchar(50),
    company_name          varchar(250),
    company_group_id      varchar(36)      NOT NULL,
    company_mail          varchar(500),
    country               varchar(50),
    city                  varchar(50),
    "zone"                varchar(50),
    segment_1             varchar(50),
    segment_2             varchar(50),
    segment_3             varchar(50),
    segment_4             varchar(50),
    segment_5             varchar(50),
    is_active             bool             NOT NULL,
    CONSTRAINT pk_companies PRIMARY KEY (id),
    CONSTRAINT fk_companies_company_group FOREIGN KEY (company_group_id) REFERENCES company_groups (id)
);

-- CompanyRepository.findByCompanyCode (tracker context by codes); codes are unique.
CREATE UNIQUE INDEX ux_companies_company_code ON companies (company_code);
-- findByCompanyGroupId (company lists, dropdowns) and findByCompanyGroupIdAndZone.
CREATE INDEX ix_companies_group_zone ON companies (company_group_id, "zone");


-- ---------------------------------------------------------------------
-- company_group_products
-- ---------------------------------------------------------------------
CREATE TABLE company_group_products (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    company_group_id      varchar(36)      NOT NULL,
    product               varchar(36)      NOT NULL,
    enabled               bool             NOT NULL,
    api_url               varchar(512),
    api_version           varchar(16),
    notes                 varchar(500),
    CONSTRAINT pk_company_group_products PRIMARY KEY (id),
    CONSTRAINT fk_company_group_products_group FOREIGN KEY (company_group_id) REFERENCES company_groups (id)
);

-- findByCompanyGroupIdAndProduct (every login) and findAllByCompanyGroupId (prefix).
CREATE UNIQUE INDEX ux_company_group_products_group_product ON company_group_products (company_group_id, product);


-- ---------------------------------------------------------------------
-- company_products (license per company and product)
-- ---------------------------------------------------------------------
CREATE TABLE company_products (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    company_id            varchar(36)      NOT NULL,
    product               varchar(36)      NOT NULL,
    enabled               bool             NOT NULL,
    license_expires_at    date,
    agreed_user_count     int4,
    CONSTRAINT pk_company_products PRIMARY KEY (id),
    CONSTRAINT fk_company_products_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

-- findByCompanyIdAndProduct (every login, license check) and findAllByCompanyId (prefix).
CREATE UNIQUE INDEX ux_company_products_company_product ON company_products (company_id, product);


-- ---------------------------------------------------------------------
-- company_group_eula_content
-- ---------------------------------------------------------------------
CREATE TABLE company_group_eula_content (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    company_group_id      varchar(36)      NOT NULL,
    product               varchar(36)      NOT NULL,
    eula_version          varchar(36)      NOT NULL,
    "language"            varchar(5)       NOT NULL,
    "content"             text             NOT NULL,
    start_date            date             NOT NULL,
    end_date              date,
    CONSTRAINT pk_company_group_eula_content PRIMARY KEY (id),
    CONSTRAINT fk_eula_content_company_group FOREIGN KEY (company_group_id) REFERENCES company_groups (id)
);

-- One text per group, product, language and version. Serves findAllByCompanyGroupId (prefix)
-- and findActiveContent (group, product, language; a group has only a few versions).
CREATE UNIQUE INDEX ux_eula_content_group_product_language_version
    ON company_group_eula_content (company_group_id, product, "language", eula_version);


-- ---------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id                        varchar(36)      NOT NULL,
    created_by                varchar(50)      NOT NULL,
    created_date              timestamp(6)     NOT NULL,
    last_modified_by          varchar(50),
    last_modified_date        timestamp(6),
    "version"                 int8             NOT NULL,
    company_id                varchar(36),
    company_group_id          varchar(36),
    username                  varchar(100),
    "password"                varchar(500),
    email                     varchar(500),
    "language"                varchar(5),
    is_active                 bool             NOT NULL,
    "name"                    varchar(100),
    surname                   varchar(100),
    phone                     varchar(10),
    gender                    varchar(10),
    "zone"                    varchar(10),
    password_change_required  bool             NOT NULL,
    password_expires_at       timestamp(6),
    password_version          int4             NOT NULL,
    login_attempt_count       int4             NOT NULL,
    locked_until              timestamp(6),
    CONSTRAINT pk_users PRIMARY KEY (id),
    -- ADMIN users have no group and no company, so both stay nullable.
    CONSTRAINT fk_users_company_group FOREIGN KEY (company_group_id) REFERENCES company_groups (id),
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

-- findByUsername (every login and every token check); usernames are unique.
CREATE UNIQUE INDEX ux_users_username ON users (username);
-- findAccessibleUsers (group, optional company list) and findActiveByCompanyGroupIdAndCompanyIdAndProduct.
CREATE INDEX ix_users_group_company ON users (company_group_id, company_id);
-- countActiveByCompanyIdAndProduct (license user limit on every login): active users of a company.
CREATE INDEX ix_users_company_active ON users (company_id) WHERE is_active;


-- ---------------------------------------------------------------------
-- user_products (role, departments and push token per user and product)
-- ---------------------------------------------------------------------
CREATE TABLE user_products (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    user_id               varchar(36)      NOT NULL,
    product               varchar(36)      NOT NULL,
    application_role      varchar(50)      NOT NULL,
    departments           varchar(1000)    NOT NULL,
    is_active             bool             NOT NULL,
    last_success_login    timestamp(6),
    push_token            varchar(1000),
    eula_accepted_at      timestamp(6),
    CONSTRAINT pk_user_products PRIMARY KEY (id),
    CONSTRAINT fk_user_products_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- One record per user and product. Serves findByUserIdAndProduct, findByUserIdInAndProduct,
-- the login updates and the subqueries joining on user_id.
CREATE UNIQUE INDEX ux_user_products_user_product ON user_products (user_id, product);


-- =====================================================================
-- gastroblue-commons tables
-- =====================================================================

-- ---------------------------------------------------------------------
-- error_messages
-- ---------------------------------------------------------------------
CREATE TABLE error_messages (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    error_code            varchar(500)     NOT NULL,
    "language"            varchar(5)       NOT NULL,
    message               varchar(1000)    NOT NULL,
    CONSTRAINT pk_error_messages PRIMARY KEY (id)
);

-- findByErrorCodeAndLanguage; one message per code and language.
CREATE UNIQUE INDEX ux_error_messages_code_language ON error_messages (error_code, "language");


-- ---------------------------------------------------------------------
-- outgoing_mail_log (write-only log; read only by the admin list, so no secondary index)
-- ---------------------------------------------------------------------
CREATE TABLE outgoing_mail_log (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    to_addresses          text             NOT NULL,
    cc_addresses          text,
    bcc_addresses         text,
    template_name         varchar(100)     NOT NULL,
    template_params       text,
    status                varchar(20)      NOT NULL,
    error_message         text,
    error_type            varchar(20),
    sent_at               timestamp(6),
    CONSTRAINT pk_outgoing_mail_log PRIMARY KEY (id)
);


-- ---------------------------------------------------------------------
-- lookup
-- Identity holds child lookups (CITY under COUNTRY), so the same key may appear under different
-- parents: the parent is part of the unique key.
-- ---------------------------------------------------------------------
CREATE TABLE lookup (
    id                    varchar(36)      NOT NULL,
    created_by            varchar(50)      NOT NULL,
    created_date          timestamp(6)     NOT NULL,
    last_modified_by      varchar(50),
    last_modified_date    timestamp(6),
    "version"             int8             NOT NULL,
    company_group_id      varchar(50)      NOT NULL,
    application_product   varchar(50)      NOT NULL,
    category              varchar(100)     NOT NULL,
    parent_lookup_key     varchar(100)     NOT NULL,
    "language"            varchar(10)      NOT NULL,
    lookup_key            varchar(100)     NOT NULL,
    display               varchar(500)     NOT NULL,
    display_order         int4,
    CONSTRAINT pk_lookup PRIMARY KEY (id)
);

-- One index for every LookupRepository query: findLookups uses the first five columns,
-- findLookupByKeyAndParent all six, findLookupByKey the category prefix (parent is always "*"
-- for lookups without a parent).
CREATE UNIQUE INDEX ux_lookup_key
    ON lookup (company_group_id, application_product, category, parent_lookup_key, "language", lookup_key);
