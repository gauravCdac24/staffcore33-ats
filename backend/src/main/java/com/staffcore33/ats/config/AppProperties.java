package com.staffcore33.ats.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private File file = new File();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expirationMs = 86400000;
    }

    @Getter
    @Setter
    public static class Cors {
        private String allowedOrigins = "http://localhost:3000";
    }

    @Getter
    @Setter
    public static class File {
        private String uploadDir = "./uploads";
    }
}
