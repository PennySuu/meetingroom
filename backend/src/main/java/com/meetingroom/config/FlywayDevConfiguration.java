package com.meetingroom.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 开发环境下容忍失败迁移与半成品表：先 repair+migrate，仍失败则 clean+migrate。
 * 仅 dev 生效，避免影响生产数据。
 */
@Configuration
@Profile("dev")
public class FlywayDevConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            try {
                flyway.migrate();
            } catch (RuntimeException ex) {
                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                if (msg.contains("already exists")
                        || msg.contains("failed migration")
                        || msg.contains("Validate failed")) {
                    flyway.clean();
                    flyway.migrate();
                    return;
                }
                throw ex;
            }
        };
    }
}