package com.jirapat.personalfinance.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class FileUploadProperties {

    private String directory;
    private long maxFileSize;
    private int maxFilesPerReference;
    private Set<String> allowedContentTypes;
}

