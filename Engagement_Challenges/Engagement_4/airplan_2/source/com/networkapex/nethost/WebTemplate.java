package com.networkapex.nethost;

import com.networkapex.template.TemplateEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class WebTemplate {
    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;
    private final String templateText;
    private final TemplateEngine templateEngine;

    public WebTemplate(String resourceName, Class<?> loader) {
        templateText = takeTemplate(resourceName, loader);
        templateEngine = new TemplateEngine(templateText);
    }

    public TemplateEngine takeEngine() {
        return templateEngine;
    }

    private String takeTemplate(String resource, Class<?> loader) {
        InputStream inputStream = loader.getResourceAsStream(resource);

        if (inputStream == null) {
            return pullTemplateCoordinator(resource);
        }

        return readStream(inputStream, resource);
    }

    private String pullTemplateCoordinator(String resource) {
        throw new IllegalArgumentException("Can not find resource " + resource);
    }

    private static String readStream(InputStream inputStream, String resource) {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = new InputStreamReader(inputStream);

        try {
            int n;
            char[] buffer = new char[DEFAULT_BUFFER_SIZE];

            while ((n = reader.read(buffer)) != -1) {
                readStreamWorker(writer, n, buffer);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read: " + resource, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                //noinspection ThrowFromFinallyBlock
                throw new IllegalArgumentException("Unexpected trouble closing reader", e);
            }
        }

        return writer.toString();
    }

    private static void readStreamWorker(StringWriter writer, int n, char[] buffer) {
        writer.write(buffer, 0, n);
    }
}
