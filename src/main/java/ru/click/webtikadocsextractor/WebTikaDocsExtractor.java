package ru.click.webtikadocsextractor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@Slf4j
public class WebTikaDocsExtractor {

    public static void main(String[] args) {
        SpringApplication.run(WebTikaDocsExtractor.class, args);
    }
}
