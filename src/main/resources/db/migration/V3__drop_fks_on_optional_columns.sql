-- =====================================================================
-- Optional tenant columns carry no foreign key.
-- An ADMIN has no company group and no company; a GROUP_MANAGER (and ZONE_MANAGER) has a group but
-- no company; a global EULA has no group. These columns are filled by the application or by hand
-- (first ADMIN), so they are not tied to company_groups / companies.
-- The FKs on mandatory columns (companies, products, user_products) stay.
-- =====================================================================

ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_company_group;
ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_company;
ALTER TABLE company_group_eula_content DROP CONSTRAINT IF EXISTS fk_eula_content_company_group;

-- "No group / no company" is NULL, never an empty string: the code checks for null.
UPDATE users SET company_group_id = NULL WHERE company_group_id = '';
UPDATE users SET company_id = NULL WHERE company_id = '';
UPDATE company_group_eula_content SET company_group_id = NULL WHERE company_group_id = '';
