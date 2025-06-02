-- Assuming the com.cas.login schema and app_user table already exist.
-- If not, you would need to create them first:
-- CREATE SCHEMA IF NOT EXISTS "com.cas.login";
-- CREATE TABLE IF NOT EXISTS "com.cas.login".app_user (
--     id BIGINT PRIMARY KEY,
--     -- other user columns
-- );

-- Table for Packing Lists
CREATE TABLE packing_lists (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES "com.cas.login".app_user(id)
        ON DELETE CASCADE
);

-- Table for Categories within a Packing List
CREATE TABLE packing_list_categories (
    id BIGSERIAL PRIMARY KEY,
    packing_list_id BIGINT NOT NULL,
    title TEXT NOT NULL,
    "order" INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_packing_list
        FOREIGN KEY(packing_list_id)
        REFERENCES packing_lists(id)
        ON DELETE CASCADE
);

-- Table for Items within a Category
CREATE TABLE packing_list_items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    is_checked BOOLEAN NOT NULL DEFAULT FALSE,
    "order" INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category
        FOREIGN KEY(category_id)
        REFERENCES packing_list_categories(id)
        ON DELETE CASCADE
);

-- Optional: Indexes for foreign keys can improve performance
CREATE INDEX IF NOT EXISTS idx_packing_lists_user_id ON packing_lists(user_id);
CREATE INDEX IF NOT EXISTS idx_packing_list_categories_packing_list_id ON packing_list_categories(packing_list_id);
CREATE INDEX IF NOT EXISTS idx_packing_list_items_category_id ON packing_list_items(category_id);

-- Optional: Triggers to update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_packing_lists_updated_at
BEFORE UPDATE ON packing_lists
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_packing_list_categories_updated_at
BEFORE UPDATE ON packing_list_categories
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_packing_list_items_updated_at
BEFORE UPDATE ON packing_list_items
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
