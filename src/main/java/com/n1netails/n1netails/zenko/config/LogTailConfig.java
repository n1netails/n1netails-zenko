package com.n1netails.n1netails.zenko.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "n1netails.zenko.logtail")
public class LogTailConfig {

    private List<String> files;
    private List<String> keywords;
    private String alertEndpoint;
    private String alertToken;

}
