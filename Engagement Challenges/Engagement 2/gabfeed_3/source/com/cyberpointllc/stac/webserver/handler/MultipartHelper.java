package com.cyberpointllc.stac.webserver.handler;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides ability to parse multipart/form-data content from a POST.
 */
public class MultipartHelper {

    /**
     * Parses the multipart content of the <code>HttpExchange</code> and returns
     * the content associated with the specified field name as a String. If the
     * exchange is not multipart or is missing properties required for parsing,
     * a <code>RuntimeException</code> is raised. If no field matches,
     * <code>null</code> is returned.
     *
     * @param httpExchange holding the multipart request to be parsed
     * @param fieldName    of the form to grab the content
     * @return String representing the content of multipart field associated
     * with the specified field name; may be <code>null</code> if there
     * is no matching field
     * @throws IllegalArgumentException if the exchange is not a POST, is missing necessary
     *                                  properties, or has trouble being parsed
     */
    public static String getMultipartFieldContent(HttpExchange httpExchange, String fieldName) {
        if (httpExchange == null) {
            throw new  IllegalArgumentException("HttpExchange may not be null");
        }
        if (StringUtils.isBlank(fieldName)) {
            throw new  IllegalArgumentException("Field name may not be blank or null");
        }
        HttpExchangeRequestContext context = new  HttpExchangeRequestContext(httpExchange);
        String result = null;
        try {
            FileUpload fileUpload = new  FileUpload();
            FileItemIterator iterator = fileUpload.getItemIterator(context);
            while (iterator.hasNext()) {
                FileItemStream fileItemStream = iterator.next();
                String name = fileItemStream.getFieldName();
                if (name.equals(fieldName)) {
                    result = IOUtils.toString(fileItemStream.openStream(), "UTF-8");
                }
            }
        } catch (Exception e) {
            throw new  IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * Get the parameters from the multi part field. If there are duplicates, the last one wins.
     * @param httpExchange
     * @return
     */
    public static Map<String, String> getMultipartFieldContent(HttpExchange httpExchange) {
        if (httpExchange == null) {
            throw new  IllegalArgumentException("HttpExchange may not be null");
        }
        HttpExchangeRequestContext context = new  HttpExchangeRequestContext(httpExchange);
        String result = null;
        Map<String, String> postFields = new  HashMap();
        try {
            FileUpload fileUpload = new  FileUpload();
            FileItemIterator iterator = fileUpload.getItemIterator(context);
            while (iterator.hasNext()) {
                getMultipartFieldContentHelper(iterator, postFields);
            }
        } catch (Exception e) {
            throw new  IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }
        return postFields;
    }

    /**
     * Parses the multipart content of the HttpExchange and returns all
     * the information associated with the specified field names.
     *
     * @param httpExchange holding POST data
     * @param fieldNames   of interest to the method caller
     * @return Map of the field names to a List of their values;
     * may be empty but guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if either argument is <code>null</code> or
     *                                  there is trouble parsing the POST content
     */
    public static Map<String, List<String>> getMultipartValues(HttpExchange httpExchange, Set<String> fieldNames) {
        if (httpExchange == null) {
            throw new  IllegalArgumentException("HttpExchange may not be null");
        }
        if (fieldNames == null) {
            throw new  IllegalArgumentException("Field Names may not be null");
        }
        HttpExchangeRequestContext context = new  HttpExchangeRequestContext(httpExchange);
        FileUpload fileUpload = new  FileUpload();
        FileItemFactory fileItemFactory = new  DiskFileItemFactory();
        fileUpload.setFileItemFactory(fileItemFactory);
        Map<String, List<String>> fieldNameValues = new  HashMap();
        try {
            // create map of all given field names and their associated item as a string
            Map<String, List<FileItem>> parameterMap = fileUpload.parseParameterMap(context);
            for (String fieldName : fieldNames) {
                getMultipartValuesHelper(parameterMap, fieldNameValues, fieldName);
            }
        } catch (Exception e) {
            throw new  IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }
        return fieldNameValues;
    }

    /**
     * Parses the multipart content of the HttpExchange. Copies the file image
     * to the image destination directory given and returns all the information
     * necessary to create a new photo using the image.
     *
     * @param httpExchange   holding POST data
     * @param allFieldNames  of the form to grab the content necessary to create the photo;
     *                       should include the image field name
     * @param imageFieldName used to distinguish the image from other content
     * @param imageDestDir   path to local destination directory
     * @return Map of the field names to their file item as a string
     * @throws IllegalArgumentException if the exchange does not
     *                                  contain an image or has trouble being parsed
     */
    public static Map<String, String> getMultipartPhoto(HttpExchange httpExchange, Set<String> allFieldNames, String imageFieldName, Path imageDestDir, String imageName) {
        if (httpExchange == null) {
            throw new  IllegalArgumentException("HttpExchange may not be null");
        }
        if (allFieldNames == null) {
            throw new  IllegalArgumentException("Field Names may not be null");
        }
        if (StringUtils.isBlank(imageFieldName)) {
            throw new  IllegalArgumentException("Image Field Name many not be empty or null");
        }
        if (imageDestDir == null) {
            throw new  IllegalArgumentException("Image Destination Directory may not be null");
        }
        HttpExchangeRequestContext context = new  HttpExchangeRequestContext(httpExchange);
        FileUpload fileUpload = new  FileUpload();
        FileItemFactory fileItemFactory = new  DiskFileItemFactory();
        fileUpload.setFileItemFactory(fileItemFactory);
        Map<String, String> fieldNameItems = new  HashMap();
        InputStream fileIn = null;
        // Make sure imageFieldName is part of allFieldNames
        if (!allFieldNames.contains(imageFieldName)) {
            Set<String> newFieldNames = new  HashSet(allFieldNames);
            newFieldNames.add(imageFieldName);
            allFieldNames = newFieldNames;
        }
        MultipartHelperHelper0 conditionObj0 = new  MultipartHelperHelper0(1);
        try {
            // create map of all given field names and their associated item as a string
            Map<String, List<FileItem>> parameterMap = fileUpload.parseParameterMap(context);
            for (String fieldName : allFieldNames) {
                List<FileItem> items = parameterMap.get(fieldName);
                if (items != null) {
                    if (items.size() == conditionObj0.getValue()) {
                        // there should only be one FileItem per fieldName
                        // if we have the image field name, we need to capture
                        // the input stream containing the image and the image name
                        FileItem item = items.get(0);
                        String fileItem;
                        if (fieldName.equals(imageFieldName)) {
                            fileIn = item.getInputStream();
                            fileItem = item.getName();
                        } else {
                            fileItem = item.getString();
                        }
                        fieldNameItems.put(fieldName, fileItem);
                    } else {
                        throw new  IllegalArgumentException("Cannot handle more than one File Item for each Field Name");
                    }
                }
            }
            if ((fileIn == null) || StringUtils.isBlank(fieldNameItems.get(imageFieldName))) {
                throw new  IllegalArgumentException("Missing required POST image file associated with field name " + imageFieldName);
            }
            File newImage = imageDestDir.toFile();
            if (!newImage.exists()) {
                getMultipartPhotoHelper(newImage);
            }
            Path path;
            if (imageName != null) {
                path = Paths.get(imageDestDir.toString(), imageName);
            } else {
                path = Paths.get(imageDestDir.toString(), fieldNameItems.get(imageFieldName));
            }
            Files.copy(fileIn, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new  IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }
        return fieldNameItems;
    }

    /**
     * Parses the multipart content of the HttpExchange and returns a List of
     * the file items, as Strings, associated with the specified field name. If
     * there are no file items associated with the field name, or the field name
     * is not found, an empty list is returned.
     *
     * @param httpExchange holding the multipart request to be parsed
     * @param fieldName    of the form to grab the content
     * @return List of Strings representing the content of the multipart field
     * associated with the specified field name; may be empty
     * @throws IllegalArgumentException if the exchange is not a POST, is missing necessary
     *                                  properties, or has trouble being parsed
     */
    public static List<String> getMultipartFieldItems(HttpExchange httpExchange, String fieldName) {
        if (httpExchange == null) {
            throw new  IllegalArgumentException("HttpExchange may not be null");
        }
        if (StringUtils.isBlank(fieldName)) {
            throw new  IllegalArgumentException("Field name may not be blank or null");
        }
        HttpExchangeRequestContext context = new  HttpExchangeRequestContext(httpExchange);
        FileUpload fileUpload = new  FileUpload();
        FileItemFactory fileItemFactory = new  DiskFileItemFactory();
        fileUpload.setFileItemFactory(fileItemFactory);
        List<String> itemStrings = new  ArrayList();
        try {
            // get items associated with the field name
            List<FileItem> items = fileUpload.parseParameterMap(context).get(fieldName);
            if (items != null && !items.isEmpty()) {
                getMultipartFieldItemsHelper(itemStrings, items);
            }
        } catch (Exception e) {
            throw new  IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }
        return itemStrings;
    }

    private static class HttpExchangeRequestContext implements RequestContext {

        private static final String CONTENT_TYPE = "Content-Type";

        private static final String MULTIPART_FORM_DATA = "multipart/form-data";

        private static final String BOUNDARY_EQUALS = "boundary=";

        private static final String CONTENT_LENGTH = "Content-Length";

        private static final String CONTENT_ENCODING = "Content-Encoding";

        private final String contentType;

        private final String contentEncoding;

        private final int contentLength;

        private final InputStream inputStream;

        public HttpExchangeRequestContext(HttpExchange httpExchange) {
            if (!"POST".equals(httpExchange.getRequestMethod())) {
                throw new  IllegalArgumentException("Only POST method is permitted");
            }
            contentType = httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE);
            if (contentType == null) {
                throw new  IllegalArgumentException("The " + CONTENT_TYPE + " request header must exist");
            }
            if (!contentType.startsWith(MULTIPART_FORM_DATA)) {
                throw new  IllegalArgumentException("Content type must be " + MULTIPART_FORM_DATA);
            }
            int index = contentType.indexOf(BOUNDARY_EQUALS, MULTIPART_FORM_DATA.length());
            if (index == -1) {
                throw new  IllegalArgumentException("Content type must contain a boundary mapping");
            }
            String contentLengthHeader = httpExchange.getRequestHeaders().getFirst(CONTENT_LENGTH);
            if (contentLengthHeader == null) {
                throw new  IllegalArgumentException("The " + CONTENT_LENGTH + " request header must exist");
            }
            try {
                contentLength = Integer.parseInt(contentLengthHeader);
            } catch (NumberFormatException e) {
                throw new  IllegalArgumentException(CONTENT_LENGTH + " must be a number: " + e.getMessage(), e);
            }
            contentEncoding = httpExchange.getRequestHeaders().getFirst(CONTENT_ENCODING);
            inputStream = httpExchange.getRequestBody();
        }

        @Override
        public String getCharacterEncoding() {
            return contentEncoding;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        @Deprecated
        public int getContentLength() {
            return contentLength;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
    }

    public static class MultipartHelperHelper0 {

        public MultipartHelperHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private static void getMultipartFieldContentHelper(FileItemIterator iterator, Map<String, String> postFields) throws Exception {
        FileItemStream fileItemStream = iterator.next();
        String name = fileItemStream.getFieldName();
        String value = IOUtils.toString(fileItemStream.openStream(), "UTF-8");
        postFields.put(name, value);
    }

    private static void getMultipartValuesHelper(Map<String, List<FileItem>> parameterMap, Map<String, List<String>> fieldNameValues, String fieldName) throws Exception {
        List<FileItem> items = parameterMap.get(fieldName);
        if ((items != null) && !items.isEmpty()) {
            List<String> values = fieldNameValues.get(fieldName);
            if (values == null) {
                values = new  ArrayList();
                fieldNameValues.put(fieldName, values);
            }
            for (FileItem item : items) {
                values.add(item.getString());
            }
        }
    }

    private static void getMultipartPhotoHelper(File newImage) throws Exception {
        newImage.mkdirs();
    }

    private static void getMultipartFieldItemsHelper(List<String> itemStrings, List<FileItem> items) throws Exception {
        for (FileItem item : items) {
            itemStrings.add(item.getString());
        }
    }
}
