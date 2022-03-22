package ru.click.webtikadocsextractor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileService {

    @Value("${input:}")
    private String inputFolder;

    @Value("${output:}")
    private String outputFolder;

    public List<InputStream> getFilesInputStreams() {
        List<Path> paths = getPaths();
        return paths.stream()
                .map(p -> {
                    try {
                        return new BufferedInputStream(new FileInputStream(p.toString()));
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Path> getPaths() {
        try (Stream<Path> paths = Files.walk(Paths.get(inputFolder))) {
            return paths
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public InputStream getFilesInputStream(String fileRelPath) throws FileNotFoundException {
        return new BufferedInputStream(new FileInputStream(getFile(fileRelPath)));
    }

    public File getFile(String fileRelPath) throws FileNotFoundException {
        Path filePath = Paths.get(inputFolder, fileRelPath);
        if (!Files.exists(filePath)) throw new FileNotFoundException("No such file: "+ fileRelPath);
        return filePath.toFile();
    }

    public void savetoFile(String relFilePath, String content) throws IOException {
        Path filePath = Paths.get(outputFolder, relFilePath);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }

    public void delete(String relFileDestination) throws IOException {
        Path filePath = Paths.get(outputFolder, relFileDestination);
        Files.deleteIfExists(filePath);
    }
}
