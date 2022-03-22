package ru.click.webtikadocsextractor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TesseractAnalysis {

    @Value("${tesseract.lang:eng,rus}")
    private List<String> langList;
    @Value("${tesseract.datapath}")
    private String dataPath;

    private final ITesseract tesseract = new Tesseract();
    private final FileService fileService;

    @PostConstruct
    private void init() {
        tesseract.setDatapath(dataPath);
        tesseract.setLanguage(String.join("+", langList));

        //tesseract.setPageSegMode(1);
        //tesseract.setOcrEngineMode(1);
    }

    public void extractContentToFile(String inRelFileDest, String outRelFileDest) throws IOException, TesseractException {
            String content = tesseract.doOCR(fileService.getFile(inRelFileDest));
            fileService.savetoFile(outRelFileDest, content);

    }

    // TODO add zip and rar recognition
}
