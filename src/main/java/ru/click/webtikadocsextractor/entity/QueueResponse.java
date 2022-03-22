package ru.click.webtikadocsextractor.entity;

import lombok.Data;
import org.apache.logging.log4j.util.Strings;

@Data
public class QueueResponse {
    private String UUID = Strings.EMPTY;
    private String status = Strings.EMPTY;
    private String message = Strings.EMPTY;
}
