package com.br.infnet.userservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserMetrics {
    private final Counter usersCreated;
    private final Counter usersDeleted;
    private final Counter usersBanned;
    private final Counter usersSuspended;
    private final Counter usersBackFromSuspension;

    public UserMetrics(MeterRegistry meterRegistry) {
        this.usersCreated = Counter.builder("users.created.total")
                .description("Total de usuários criados")
                .tag("service", "user-service")
                .register(meterRegistry);

        this.usersDeleted = Counter.builder("users.deleted.total")
                .description("Total de usuários deletados")
                .tag("service", "user-service")
                .register(meterRegistry);

        this.usersBanned = Counter.builder("users.banned.total")
                .description("Total de usuários banidos")
                .tag("service", "user-service")
                .register(meterRegistry);

        this.usersSuspended = Counter.builder("users.suspended.total")
                .description("Total de usuários suspensos")
                .tag("service", "user-service")
                .register(meterRegistry);

        this.usersBackFromSuspension = Counter.builder("users.restored.total")
                .description("Total de usuários que voltaram da suspensão")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    public void incrementUsersCreated() {
        usersCreated.increment();
    }

    public void incrementUsersDeleted() {
        usersDeleted.increment();
    }

    public void incrementUsersBanned() {
        usersBanned.increment();
    }

    public void incrementUsersSuspended() {
        usersSuspended.increment();
    }

    public void incrementUsersBackFromSuspension() {
        usersBackFromSuspension.increment();
    }
}