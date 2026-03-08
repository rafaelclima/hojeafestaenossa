package com.rafaellima.hojeafestaenossa.shared.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // Mantém 2 threads sempre ativas
        executor.setMaxPoolSize(10); // Pode subir até 10 threads em picos de acesso
        executor.setQueueCapacity(500); // Aguenta até 500 uploads na fila de espera
        executor.setThreadNamePrefix("AsyncUpload-");
        executor.initialize();
        return executor;
    }
}
