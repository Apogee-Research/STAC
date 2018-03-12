package com.tweeter.model;

import com.tweeter.model.hibernate.User;
import vash.Vash;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

/**
 * Avatars are images which are automatically assigned to users. They are unique 128x128 images made from some user data.
 */
public class Avatar {
    private byte[] data;

    public Avatar(User user, int i) throws IOException, NoSuchAlgorithmException, SQLException {
        if (!(i == 128 || i == 256 || i == 512)) i = 128;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BufferedImage image = Vash.createImage("1.1", user.getUniqueData(), i, i);
        ImageIO.write(image, "bmp", stream);
        this.data = stream.toByteArray();
    }

    public Avatar(User user) throws IOException, NoSuchAlgorithmException, SQLException {
        this(user, 0);
    }

    public byte[] getImageData() {
        return data;
    }
}
