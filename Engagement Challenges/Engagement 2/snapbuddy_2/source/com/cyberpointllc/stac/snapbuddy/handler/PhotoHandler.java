package com.cyberpointllc.stac.snapbuddy.handler;

import com.cyberpointllc.stac.snapservice.ImageService;
import com.cyberpointllc.stac.snapservice.SnapService;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import com.cyberpointllc.stac.webserver.handler.HttpHandlerResponse;
import com.sun.net.httpserver.HttpExchange;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;

public class PhotoHandler extends AbstractSnapBuddyHandler {

    private static final String PATH = "/photo/";

    private static final String IMAGE_FORMAT = "jpeg";

    private final ImageService imageService;

    public PhotoHandler(SnapService snapService, ImageService imageService) {
        super(snapService);
        if (imageService == null) {
            throw new  IllegalArgumentException("ImageService may not be null");
        }
        this.imageService = imageService;
    }

    @Override
    public String getPath() {
        return PATH;
    }

    @Override
    protected HttpHandlerResponse handleGet(HttpExchange httpExchange) {
        String path = httpExchange.getRequestURI().getPath();
        if (path.startsWith(getPath())) {
            path = path.substring(getPath().length());
        }
        Photo photo = getSnapService().getPhoto(path);
        HttpHandlerResponse response;
        // Determine if this person is allowed to see this photo
        Person person = getPerson(httpExchange);
        if ((photo == null) || getSnapService().isPhotoVisible(person, photo)) {
            // good to go
            response = processBufferedImage(getBufferedImage(imageService, photo));
        } else {
            response = getErrorResponse(HttpURLConnection.HTTP_FORBIDDEN, "You're not allowed to see this photo: " + path);
        }
        return response;
    }

    protected BufferedImage getBufferedImage(ImageService imageService, Photo photo) {
        if (photo == null) {
            photo = getDefaultPhoto();
        }
        return imageService.getPhotoImage(photo);
    }

    protected HttpHandlerResponse processBufferedImage(BufferedImage bufferedImage) {
        HttpHandlerResponse response;
        try {
            ByteArrayOutputStream stream = new  ByteArrayOutputStream();
            boolean success = ImageIO.write(bufferedImage, IMAGE_FORMAT, stream);
            stream.close();
            if (success) {
                response = new  ImageHandlerResponse(stream.toByteArray());
            } else {
                response = getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Could not convert the image into format " + IMAGE_FORMAT);
            }
        } catch (IOException e) {
            response = getErrorResponse(HttpURLConnection.HTTP_BAD_REQUEST, "Trouble converting the image: " + e.getMessage());
        }
        return response;
    }

    private static class ImageHandlerResponse extends HttpHandlerResponse {

        private static final String CONTENT_TYPE = "image/jpeg";

        private final byte[] bytes;

        ImageHandlerResponse(byte[] bytes) {
            super();
            this.bytes = bytes;
        }

        @Override
        protected String getContentType() {
            return CONTENT_TYPE;
        }

        @Override
        protected byte[] getResponseBytes(HttpExchange httpExchange) throws IOException {
            return bytes;
        }
    }
}
