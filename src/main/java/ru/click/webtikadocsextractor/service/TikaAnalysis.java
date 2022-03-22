package ru.click.webtikadocsextractor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class TikaAnalysis {

    private final FileService fileService;
    private final Tika tika = new Tika();

    public String extractContent(String fileRelPath) throws TikaException, IOException {
        InputStream stream = fileService.getFilesInputStream(fileRelPath);
        return tika.parseToString(stream);
    }

    public boolean extractContentToFile(String inRelPath, String outRelPath) throws TikaException, IOException {
        String content = extractContent(inRelPath);
        if (content == null) content = Strings.EMPTY;
        fileService.savetoFile(outRelPath, content);
        return !content.isBlank();
    }


 /*   // TODO for tests
    public void process() {
        List<InputStream> iss = fileService.getFilesInputStreams();
        iss.forEach(is -> {
            try {
                //log.info(detectDocTypeUsingDetector(is));
                //log.info(detectDocTypeUsingFacade(is));
                log.info(extractContentUsingFacade(is));
                //log.info(extractContentUsingParser(is));
                //log.info(extractMetadatatUsingFacade(is).toString());
                //log.info(extractMetadatatUsingParser(is).toString());
                log.info("------------------------------------");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        });
    }


    // TODO for tests
    public static String detectDocTypeUsingDetector(InputStream stream) throws IOException {
        Detector detector = new DefaultDetector();
        Metadata metadata = new Metadata();

        MediaType mediaType = detector.detect(stream, metadata);
        return mediaType.toString();
    }

    // TODO for tests
    public static String detectDocTypeUsingFacade(InputStream stream) throws IOException {
        Tika tika = new Tika();
        String mediaType = tika.detect(stream);
        return mediaType;
    }

    // TODO for tests
    public static String extractContentUsingParser(InputStream stream) throws IOException, TikaException, SAXException {
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        parser.parse(stream, handler, metadata, context);
        return handler.toString();
    }

    // TODO for tests
    public static String extractContentUsingFacade(InputStream stream) throws IOException, TikaException {
        Tika tika = new Tika();
        String content = tika.parseToString(stream);
        return content;
    }

    // TODO for tests
    public static Metadata extractMetadatatUsingParser(InputStream stream) throws IOException, SAXException, TikaException {
        Parser parser = new AutoDetectParser();
        ContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        ParseContext context = new ParseContext();

        parser.parse(stream, handler, metadata, context);
        return metadata;
    }

    // TODO for tests
    public static Metadata extractMetadatatUsingFacade(InputStream stream) throws IOException, TikaException {
        Tika tika = new Tika();
        Metadata metadata = new Metadata();

        tika.parse(stream, metadata);
        return metadata;
    }
*/
}
