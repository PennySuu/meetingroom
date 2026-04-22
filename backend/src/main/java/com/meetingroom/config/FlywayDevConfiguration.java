package com.meetingroom.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Flyway 在检测到「失败迁移」时会在 {@link Flyway#migrate()} 内置校验阶段直接抛错，
 * {@code repair-on-migrate} 属性未必先于该校验生效。开发环境显式先 {@link Flyway#repair()} 再迁移。
 */
@Configuration
@Profile("dev")
public class FlywayDevConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
