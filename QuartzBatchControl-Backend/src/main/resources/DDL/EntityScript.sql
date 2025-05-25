CREATE TABLE batch_job_meta (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                job_name VARCHAR(255) NOT NULL,
                                meta_name VARCHAR(255) NOT NULL,
                                job_description VARCHAR(500),
                                job_parameters TEXT,
                                created_by VARCHAR(100) NOT NULL,
                                created_at DATETIME NOT NULL,
                                updated_by VARCHAR(100) NOT NULL,
                                updated_at DATETIME NOT NULL
);

CREATE TABLE batch_job_execution_log (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               job_execution_id BIGINT,
                               job_name VARCHAR(255) NOT NULL,
                               meta_id BIGINT NOT NULL,
                               start_time DATETIME NOT NULL,
                               end_time DATETIME DEFAULT NULL,
                               status VARCHAR(255) NOT NULL,
                               exit_code VARCHAR(255) DEFAULT NULL,
                               exit_message VARCHAR(255) DEFAULT NULL,
                               job_parameters TEXT DEFAULT NULL
) ENGINE=InnoDB;

CREATE TABLE quartz_job_meta (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         job_name VARCHAR(255) NOT NULL,
                                         job_group VARCHAR(255) NOT NULL,
                                         job_type VARCHAR(50) NOT NULL,        -- ENUM (QuartzJobType)
                                         event_type VARCHAR(50) NOT NULL,      -- ENUM (QuartzJobEventType)
                                         cron_expression VARCHAR(255) DEFAULT NULL,
                                         batch_meta_id BIGINT DEFAULT NULL,
                                         created_by VARCHAR(100) NOT NULL,
                                         created_at DATETIME NOT NULL,
                                         updated_by VARCHAR(100) NOT NULL,
                                         updated_at DATETIME NOT NULL
) ENGINE=InnoDB;

CREATE TABLE quartz_job_execution_log (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          job_name VARCHAR(190) NOT NULL,
                                          job_group VARCHAR(190) NOT NULL,
                                          start_time DATETIME NOT NULL,
                                          end_time DATETIME NOT NULL,
                                          status VARCHAR(50) NOT NULL,         -- 예: SUCCESS / FAIL
                                          message TEXT
);

