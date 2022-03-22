package ru.click.webtikadocsextractor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.click.webtikadocsextractor.constants.QueueStatus;
import ru.click.webtikadocsextractor.entity.QueueEntity;
import ru.click.webtikadocsextractor.entity.QueueResponse;
import ru.click.webtikadocsextractor.repository.QueueRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final TikaAnalysis tikaAnalysis;
    private final TesseractAnalysis tesseractAnalysis;

    private final QueueRepository repository;
    private final FileService fileService;

    private static final int nThreads = 4;
    private final ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

    @Value("${input}")
    private String inputPath;

    @Value("${enableOCR:false}")
    private boolean enableOCR;

    @Value("${tika.check-task-interval.ms}")
    private int tikaCheckTaskInterval;

    @Value("${tika.extraction-max-time.ms}")
    private int tikaExtractionMaxTime;

    @Value("${tesseract.check-task-interval.ms}")
    private int tessCheckTaskInterval;

    @Value("${tesseract.extraction-max-time.ms}")
    private int tessExtractionMaxTime;

    @Value("${queue.check-interval.ms}")
    private int queueCheckInterval;

    @Value("${queue.finished.max-time.ms}")
    private int queueFinishedMaxTime;

    @Value("${queue.processing.max-time.ms}")
    private int queueProcessingMaxTime;


    @PostConstruct
    private void init() {

        // Checking and executing Tika tasks
        executorService.submit((Runnable) () -> {
            while (true) {

                QueueEntity queueEntity = getTaskForProcessing(false);


                if (queueEntity != null) {
                    log.debug("Tika: Start task: {}", queueEntity);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<?> future = executor.submit(() -> docAnalysisWithTika(queueEntity));

                    try {
                        future.get(tikaExtractionMaxTime, TimeUnit.MILLISECONDS);
                    } catch (ExecutionException | TimeoutException | InterruptedException e) {
                        handleException(queueEntity, e);
                    }

                } else {
                    log.debug("Tika: No documents to extract.");
                    try {
                        Thread.sleep(tikaCheckTaskInterval);
                    } catch (InterruptedException e) {
                        // Ignore interrupted exception
                    }
                }
            }
        });

        // Checking and executing Tess tasks
        executorService.submit(() -> {
            while (true) {

                QueueEntity queueEntity = getTaskForProcessing(true);


                if (queueEntity != null) {
                    log.debug("Tess: Start task: {}", queueEntity);
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Future<?> future = executor.submit(() -> docAnalysisWithTess(queueEntity));

                    try {
                        future.get(tessExtractionMaxTime, TimeUnit.MILLISECONDS);
                    } catch (ExecutionException | TimeoutException | InterruptedException e) {
                        handleException(queueEntity, e);
                    }

                } else {
                    log.debug("Tess: No documents to extract.");
                    try {
                        Thread.sleep(tessCheckTaskInterval);
                    } catch (InterruptedException e) {
                        // Ignore interrupted exception
                    }
                }
            }
        });

        // Deleting finished tasks
        executorService.submit(() -> {
            while (true) {
                removeFinishedTasksAndFiles();
                try {
                    Thread.sleep(queueCheckInterval);
                } catch (InterruptedException e) {
                    // Ignore interrupted exception
                }
            }
        });

        // Deleting sleeping tasks
        executorService.submit(() -> {
            while (true) {
                removeHangedUpTasksAndFiles();
                try {
                    Thread.sleep(queueCheckInterval);
                } catch (InterruptedException e) {
                    // Ignore interrupted exception
                }
            }

        });

    }

    @PreDestroy
    private void destroy() {
        executorService.shutdownNow();
    }

    private void docAnalysisWithTika(QueueEntity queueEntity) {

        try {
            String inRelFilePath = queueEntity.getRelFileDestination();
            String outRelFilePath = queueEntity.getId().toString();

            boolean result = tikaAnalysis.extractContentToFile(inRelFilePath, outRelFilePath);

            if (!result) {
                log.debug("Tika: Empty document content");
            }

            if (!result && enableOCR) {
                queueEntity.setStatus(QueueStatus.WAITING);
                queueEntity.setOcr(true);
            } else {
                queueEntity.setStatus(QueueStatus.PROCESSED);
                queueEntity.setFinishTime(new Date());
            }
            repository.save(queueEntity);

        } catch (Exception e) {
            handleException(queueEntity, e);
        }
    }

    private void docAnalysisWithTess(QueueEntity queueEntity) {
        try {
            String inRelFilePath = queueEntity.getRelFileDestination();
            String outRelFilePath = queueEntity.getId().toString();

            tesseractAnalysis.extractContentToFile(inRelFilePath, outRelFilePath);
            queueEntity.setStatus(QueueStatus.PROCESSED);
            queueEntity.setFinishTime(new Date());
            repository.save(queueEntity);

        } catch (Exception e) {
            handleException(queueEntity, e);
        }
    }

    private void handleException(QueueEntity queueEntity, Exception e) {
        queueEntity.setStatus(QueueStatus.ERROR);
        queueEntity.setErrorMessage(e.getMessage());
        queueEntity.setFinishTime(new Date());
        repository.save(queueEntity);
    }


    public QueueResponse getStatus(String id) {

        QueueResponse response = new QueueResponse();
        response.setUUID(id);

        Optional<QueueEntity> queueEntity = repository.findById(UUID.fromString(id));
        if (queueEntity.isPresent()) {
            response.setStatus(queueEntity.get().getStatus().getStatus());
            response.setMessage("");
        } else {
            response.setStatus(QueueStatus.ERROR.getStatus());
            response.setMessage("No task with such Id...");
        }
        return response;
    }

    public QueueResponse createTask(String fileDestEncoded) throws NoSuchFileException {

        String fileDest = new String(
                Base64.getDecoder().decode(fileDestEncoded.getBytes(StandardCharsets.UTF_8))
        );

        QueueResponse queueResponse = new QueueResponse();

        // Check if file exists
        Path path = Paths.get(inputPath, fileDest);
        if (!Files.exists(path)) {
            queueResponse.setMessage("No file: " + fileDest);
            queueResponse.setStatus(QueueStatus.ERROR.getStatus());
            return queueResponse;
        }

        // Put task to database
        QueueEntity queueEntity = new QueueEntity();
        queueEntity.setStatus(QueueStatus.WAITING);
        queueEntity.setCreateTime(new Date());
        queueEntity.setRelFileDestination(fileDest);
        queueEntity = repository.save(queueEntity);

        queueResponse.setStatus(queueEntity.getStatus().toString());
        queueResponse.setUUID(queueEntity.getId().toString());

        return queueResponse;
    }

    private QueueEntity getTaskForProcessing(boolean ocr) {
        Optional<QueueEntity> optionalQueueEntity =
                repository.findFirstByStatusAndOcrOrderByCreateTimeAsc(QueueStatus.WAITING, ocr);
        if (optionalQueueEntity.isPresent()) {
            QueueEntity queueEntity = optionalQueueEntity.get();
            if (queueEntity.getStartTime() == null) queueEntity.setStartTime(new Date());
            queueEntity.setStatus(QueueStatus.PROCESSING);
            return repository.save(queueEntity);
        } else {
            return null;
        }
    }



    private void removeFinishedTasksAndFiles() {

        Date date = new Date(System.currentTimeMillis() - queueFinishedMaxTime);

        Optional<List<QueueEntity>> optionalList =
                repository.findQueueEntityByStatusAndFinishTimeIsNotNullAndFinishTimeIsBefore(QueueStatus.PROCESSED, date);

        if (optionalList.isEmpty()) {
            log.debug("Nothing to delete");
            return;
        }
        List<QueueEntity> queue = optionalList.get();
        if (queue.isEmpty()) {
            log.debug("Nothing to delete");
            return;
        }

        queue.forEach(entity -> {
            try {
                fileService.delete(entity.getRelFileDestination());
                repository.delete(entity);
                log.debug("Deleting entity: {}", entity);
            } catch (Exception e) {
                log.debug("Cannot delete: {}", entity);
            }
        });
    }

    private void removeHangedUpTasksAndFiles() {

        Date date = new Date(System.currentTimeMillis() - queueProcessingMaxTime);

        Optional<List<QueueEntity>> optionalList =
                repository.findQueueEntityByStatusAndStartTimeIsNotNullAndStartTimeIsBefore(QueueStatus.PROCESSED, date);

        if (optionalList.isEmpty()) {
            log.debug("Nothing to delete");
            return;
        }
        List<QueueEntity> queue = optionalList.get();
        if (queue.isEmpty()) {
            log.debug("Nothing to delete");
            return;
        }

        queue.forEach(entity -> {
            try {
                fileService.delete(entity.getRelFileDestination());
                repository.delete(entity);
                log.debug("Deleting entity: {}", entity);
            } catch (Exception e) {
                log.debug("Cannot delete: {}", entity);
            }
        });
    }


}
