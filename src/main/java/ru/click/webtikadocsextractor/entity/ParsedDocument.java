package ru.click.webtikadocsextractor.entity;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;


public class ParsedDocument {

    private final Map<String, String> metadata = new HashMap<>();
    @Getter
    @Setter
    private String content = Strings.EMPTY;

    private String getMetadataByKey(String key) {
        return metadata.get(key);
    }

    private void setMetadata(String key, String value) {
        metadata.put(key, value);
    }

}
