-- =====================================================================
-- Global EULA: a text without a company group applies to every group that has no text of its own.
-- findActiveContent already prefers the group's text and falls back to company_group_id IS NULL;
-- V1 made the column NOT NULL, so a global text could not be stored.
-- The foreign key stays: NULL is not checked against company_groups.
-- =====================================================================

ALTER TABLE company_group_eula_content ALTER COLUMN company_group_id DROP NOT NULL;

-- NULLS NOT DISTINCT: two global texts with the same product, language and version count as
-- duplicates (by default PostgreSQL treats every NULL as different and would allow them).
DROP INDEX ux_eula_content_group_product_language_version;
CREATE UNIQUE INDEX ux_eula_content_group_product_language_version
    ON company_group_eula_content (company_group_id, product, "language", eula_version) NULLS NOT DISTINCT;
