package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.core.env.Environment;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LoggingConfigUpdater {
    private final Environment env;

    public LoggingConfigUpdater(Environment env) {
        this.env = env;
    }

    @EventListener
    public void handleRefresh(EnvironmentChangeEvent event) {
        // Check if logging properties changed
        if (event.getKeys().stream().anyMatch(key -> key.startsWith("logging."))) {
            try {
                // Load a template and replace placeholders with values from env
                String updatedConfig = generateUpdatedLogbackConfig(env);
                // Write the updated config to a file that Logback scans
                Files.write(Paths.get("config/logback-dynamic.xml"), updatedConfig.getBytes());
            } catch (IOException e) {
                // Handle error writing file
                e.printStackTrace();
            }
        }
    }

    private String generateUpdatedLogbackConfig(Environment env) {
        String logFile = env.getProperty("logging.file.name", "logs/app.log");
        String rootLevel = env.getProperty("logging.level.root", "INFO");
        // Build a new configuration string. In a real scenario, you might use a template engine.
        return "<configuration scan=\"true\" scanPeriod=\"30 seconds\">\n" +
            "    <springProperty scope=\"context\" name=\"logFile\" source=\"logging.file.name\" defaultValue=\"" + logFile + "\"/>\n" +
            "    <springProperty scope=\"context\" name=\"rootLevel\" source=\"logging.level.root\" defaultValue=\"" + rootLevel + "\"/>\n" +
            "    <appender name=\"CONSOLE\" class=\"ch.qos.logback.core.ConsoleAppender\">\n" +
            "        <encoder>\n" +
            "            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>\n" +
            "        </encoder>\n" +
            "    </appender>\n" +
            "    <appender name=\"FILE\" class=\"ch.qos.logback.core.rolling.RollingFileAppender\">\n" +
            "        <file>${logFile}</file>\n" +
            "        <rollingPolicy class=\"ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy\">\n" +
            "            <fileNamePattern>${logFile}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>\n" +
            "            <maxFileSize>10MB</maxFileSize>\n" +
            "        </rollingPolicy>\n" +
            "        <encoder>\n" +
            "            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level - %msg%n</pattern>\n" +
            "        </encoder>\n" +
            "    </appender>\n" +
            "    <root level=\"${rootLevel}\">\n" +
            "        <appender-ref ref=\"CONSOLE\" />\n" +
            "        <appender-ref ref=\"FILE\" />\n" +
            "    </root>\n" +
            "</configuration>";
    }
}
