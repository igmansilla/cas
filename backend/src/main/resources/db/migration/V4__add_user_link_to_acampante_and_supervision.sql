-- Add user_id column to acampantes table to link to a user account
-- Make it nullable as per @JoinColumn(nullable = true)
-- Add unique constraint as per @JoinColumn(unique = true)
ALTER TABLE acampantes
ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Add foreign key constraint if it doesn't exist.
-- Need to check constraint name conventions if any are established.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_acampante_user' AND conrelid = 'acampantes'::regclass
    ) THEN
        ALTER TABLE acampantes
        ADD CONSTRAINT fk_acampante_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL; -- Matches orphanRemoval and allows null
    END IF;
END$$;

-- Add unique constraint for user_id if it doesn't exist.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uq_acampante_user_id' AND conrelid = 'acampantes'::regclass AND contype = 'u'
    ) THEN
        ALTER TABLE acampantes
        ADD CONSTRAINT uq_acampante_user_id UNIQUE (user_id);
    END IF;
END$$;


-- Create user_supervision table for ManyToMany relationship between users (dirigente <-> acampante)
-- This table might have been created by Hibernate ddl-auto=update already if User entity was processed.
-- Using IF NOT EXISTS to be safe.
CREATE TABLE IF NOT EXISTS user_supervision (
    dirigente_id BIGINT NOT NULL,
    acampante_id BIGINT NOT NULL,
    PRIMARY KEY (dirigente_id, acampante_id),
    CONSTRAINT fk_supervision_dirigente
        FOREIGN KEY (dirigente_id)
        REFERENCES users(id)
        ON DELETE CASCADE, -- If a user (dirigente) is deleted, their supervision links are removed
    CONSTRAINT fk_supervision_acampante
        FOREIGN KEY (acampante_id)
        REFERENCES users(id)
        ON DELETE CASCADE  -- If a user (acampante) is deleted, their supervision links are removed
);

-- Note: Roles (ROLE_ACAMPANTE, ROLE_DIRIGENTE) are expected to be handled by data.sql
-- or a repeatable Flyway migration if preferred for seed/reference data.
-- For this exercise, relying on data.sql as previously created.
