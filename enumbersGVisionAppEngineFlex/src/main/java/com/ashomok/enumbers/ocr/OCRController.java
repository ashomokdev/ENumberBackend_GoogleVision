package com.ashomok.enumbers.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;


/**
 * Created by Iuliia on 02.12.2015.
 */
@RestController
public class OCRController {

    private final static Logger log = LoggerFactory.getLogger(OCRController.class);


    @Autowired
    private HttpServletRequest request;

    @RequestMapping(value = "/upload", method = RequestMethod.GET)
    public
    @ResponseBody
    String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();

                OCRProcessor processor = new OCRProcessorImpl();
                String[] jsonResult = processor.doOCR(bytes);


                return new ResponseEntity(jsonResult, HttpStatus.ACCEPTED);

            } catch (Exception e) {
                return new ResponseEntity("You failed to upload file. " + e.getMessage(), HttpStatus.BAD_REQUEST);

            }
        } else {
            return new ResponseEntity("You failed to upload file because the file was empty. ", HttpStatus.BAD_REQUEST);
        }
    }


    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity testhandleFileUpload() {
        String[] jsonResult = new String[]{"test", "E951", "E954", "E955", "E1621", "E211", "E202", "E950"};
        return new ResponseEntity(jsonResult, HttpStatus.ACCEPTED);
    }

    private void CreatePathIfNeeded(String path) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean iscreated = dir.mkdir();
            if (!iscreated) {
                throw new IOException("Directory " + path + " can not be created");
            }
        }
    }
}