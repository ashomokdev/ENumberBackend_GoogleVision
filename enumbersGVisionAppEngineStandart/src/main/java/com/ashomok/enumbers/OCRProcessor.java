package com.ashomok.enumbers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by Iuliia on 03.12.2015.
 */
public interface OCRProcessor {



    String[] doOCR(byte[] bytes) throws IOException;
}
