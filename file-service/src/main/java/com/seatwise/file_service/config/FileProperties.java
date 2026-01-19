package com.seatwise.file_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileProperties {
    private String allowedTypes;
    private int maxSizeMb = 50;
    private String categories;

    public List<String> getAllowedTypesList() {
        return Arrays.asList(allowedTypes.split(","));
    }

    public List<String> getCategoriesList() {
        return Arrays.asList(categories.split(","));
    }
}