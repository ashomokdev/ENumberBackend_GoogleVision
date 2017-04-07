package com.ashomok.enumbers.ocr;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;


import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.*;
import com.google.common.collect.ImmutableList;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Iuliia on 03.12.2015.
 */
public class OCRProcessorImpl implements OCRProcessor {

    private final Vision vision;

    private static final String REGEX_ENUMB = "E[ ]{0,2}[0-9]{3,4}[a-j]{0,1}";

    public OCRProcessorImpl() throws IOException, GeneralSecurityException {

          vision =  getVisionService();
    }

    public String[] doOCR(byte[] bytes) throws IOException {
        AnnotateImageRequest request =
                new AnnotateImageRequest()
                        .setImage(new Image().encodeContent(bytes))
                        .setFeatures(ImmutableList.of(
                                new Feature()
                                        .setType("TEXT_DETECTION")
                                        .setMaxResults(1)));
        Vision.Images.Annotate annotate =
                vision.images()
                        .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotate.setDisableGZipContent(true);

        BatchAnnotateImagesResponse batchResponse = annotate.execute();
        assert batchResponse.getResponses().size() == 1;
        String recognizedText = convertResponseToString(batchResponse);
        return obtainData(recognizedText);
    }

    private String[] obtainData(String input) {
        if (input!= null && input.length()>0) {
            return parseResult(input);
        } else {
            return new String[0];
        }
    }

    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "";

        List<EntityAnnotation> texts = response.getResponses().get(0).getTextAnnotations();
        if (texts != null && texts.size() > 0) {
            message += texts.get(0).getDescription();
        }
        return message;
    }

    private static final String APPLICATION_NAME = "ashomokdev-ENumbers/1.0";

    /**
     * Connects to the Vision API using Application Default Credentials.
     */
    public static Vision getVisionService() throws IOException, GeneralSecurityException {
        GoogleCredential credential =
                GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
        com.google.api.client.json.JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }




    private String[] parseResult(String input) {
        final String E = "E";
        final int lengthOfWord = 8;

        if (input.contains(E)) {
            //get possible E-numbers

            Set<String> words =  new HashSet<String>();

            int fromIndex = 0;
            while (fromIndex < input.length()) {

                int wordStart = input.indexOf(E, fromIndex);
                if (wordStart >= 0) { //if E - numbers exist

                    int wordEnd = input.indexOf(E, fromIndex) + lengthOfWord;
                    if (wordEnd > input.length() - 1) {
                        wordEnd = input.length();
                    }
                    String word = input.substring(wordStart, wordEnd);

                    String result = parseWord(word);
                    if (result != null) {
                        words.add(result);
                    }
                    fromIndex = wordStart + 1;
                }
                else
                {
                    fromIndex = input.length();
                }
            }
            return words.toArray(new String[words.size()]);
        } else {
            return new String[0];
        }
    }

    private String parseWord(String word) {
        Pattern pattern = Pattern.compile(REGEX_ENUMB);
        Matcher matcher = pattern.matcher(word);
        if (matcher.find()) {
            String result = matcher.group(0).replaceAll("\\s", "");
            return result;
        } else {
            return null;
        }
    }
}
