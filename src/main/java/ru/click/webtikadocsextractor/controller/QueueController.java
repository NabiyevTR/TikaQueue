package ru.click.webtikadocsextractor.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.click.webtikadocsextractor.constants.QueueStatus;
import ru.click.webtikadocsextractor.entity.QueueResponse;
import ru.click.webtikadocsextractor.service.QueueService;

@RestController
@Slf4j
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    @GetMapping(value = "/extract/{fileDestEncoded}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QueueResponse putTaskToQueue(@PathVariable String fileDestEncoded) {

        QueueResponse queueResponse;

        try {
            log.info("Request: /{}", fileDestEncoded);
            queueResponse = queueService.createTask(fileDestEncoded);
            log.info("Response: {}", queueResponse);
        } catch (Exception e) {
            queueResponse = handleException(e, Strings.EMPTY);
            log.error("Response: {}", queueResponse);
        }
        return  queueResponse;
    }

    @GetMapping(value = "/status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public QueueResponse getStatus(@PathVariable String id) {

        QueueResponse queueResponse;

        try {
            log.info("Request: /status/{id}", id);
            queueResponse = queueService.getStatus(id);
            log.info("Response: {}", queueResponse);
        } catch (Exception e) {
            queueResponse = handleException(e, id);
            log.error("Response: {}", queueResponse);
        }
        return queueResponse;
    }

    private QueueResponse handleException(Exception e, String id) {
        log.error(e.getMessage(), e);
        QueueResponse queueResponse = new QueueResponse();
        queueResponse.setUUID(id);
        queueResponse.setStatus(QueueStatus.ERROR.getStatus());
        queueResponse.setMessage(e.getMessage());
        return queueResponse;
    }
}
