/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ashomok.enumbers;


import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;

public class OCRPhotoServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse response) {
        ServletFileUpload upload = new ServletFileUpload();

        try {
            FileItemIterator it = upload.getItemIterator(req);

            while (it.hasNext()) {
                FileItemStream item = it.next();
                String fieldName = item.getFieldName();
                InputStream fieldValue = item.openStream();

                if ("file".equals(fieldName)) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    Streams.copy(fieldValue, out, true);
                    byte[] bytes = out.toByteArray();
                    OCRProcessor processor = new OCRProcessorImpl();
                    String[] jsonResult = processor.doOCR(bytes);

                    response.getWriter().write(Arrays.toString(jsonResult));
                    response.setStatus(HttpServletResponse.SC_ACCEPTED);
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
