-- Créé le 08/03/2026 pour l'ajout de sources aux souvenirs
CREATE TABLE sources (
    source_id BIGINT AUTO_INCREMENT PRIMARY KEY,

    memory_id BIGINT NOT NULL,

    url VARCHAR(2048) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    title VARCHAR(512),

    credibility_score INT,
    status VARCHAR(20) NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_source_memory
        FOREIGN KEY (memory_id)
        REFERENCES memories(memory_id)
        ON DELETE CASCADE,
    
    CONSTRAINT uk_memory_url UNIQUE (memory_id, url(512))
);

CREATE INDEX idx_sources_memory_id ON sources(memory_id);
CREATE INDEX idx_sources_domain ON sources(domain);
CREATE INDEX idx_sources_status ON sources(status);