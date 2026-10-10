CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(128) NOT NULL UNIQUE,
    nick_name VARCHAR(100) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reminder_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    feed_interval_minutes INT NOT NULL DEFAULT 180,
    diaper_interval_minutes INT NOT NULL DEFAULT 240,
    reminder_enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminder_config_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS baby_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    baby_name VARCHAR(50) NOT NULL DEFAULT '宝宝',
    record_type VARCHAR(20) NOT NULL,
    record_time DATETIME NOT NULL,
    feed_type VARCHAR(20) DEFAULT NULL,
    milk_amount DECIMAL(5,1) DEFAULT NULL,
    diaper_status VARCHAR(20) DEFAULT NULL,
    diaper_note VARCHAR(255) DEFAULT NULL,
    sleep_duration INT DEFAULT NULL,
    temperature DECIMAL(4,2) DEFAULT NULL,
    note TEXT DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_baby_record_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_baby_records_user_time ON baby_records(user_id, record_time);
CREATE INDEX idx_baby_records_type ON baby_records(record_type);
