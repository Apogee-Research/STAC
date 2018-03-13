package com.tweeter.web;

import com.tweeter.model.Avatar;
import com.tweeter.model.hibernate.User;
import com.tweeter.model.repositories.UserRepository;
import com.tweeter.service.AvatarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Avatars are delivered to clients via this controller.
 */
@Controller
@ComponentScan
public class AvatarController {

    private final UserRepository users;
    private final AvatarService avatars;

    @Autowired
    public AvatarController(@Qualifier("userRepository") UserRepository users, @Qualifier("avatarService") AvatarService avatars) {
        this.avatars = avatars;
        this.users = users;
    }

    @RequestMapping(value = "/avatar/me", produces = "image/bmp")
    public
    @ResponseBody
    byte[] getAvatar() throws SQLException, IOException, URISyntaxException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            User oneByName = users.findOneByUsername(authentication.getName());
            if (oneByName == null) {
                return getDefault();
            }
            Avatar avatar = avatars.get(oneByName.getId());
            return avatar.getImageData();
        } else {
            return getDefault();
        }
    }

    @RequestMapping(value = "/avatar/id/{userid}", produces = "image/bmp")
    public
    @ResponseBody
    byte[] getAvatar(@PathVariable Long userid) throws SQLException, IOException, URISyntaxException {
        User one = users.findOne(userid);
        if (one == null) {
            return getDefault();
        }
        Avatar avatar = avatars.get(one.getId());
        return avatar.getImageData();
    }

    @RequestMapping(value = "/avatar/username/{username:.+}", produces = "image/bmp")
    public
    @ResponseBody
    byte[] getAvatar(@PathVariable String username) throws SQLException, IOException, URISyntaxException {
        User one = users.findOneByUsername(username);
        if (one == null) {
            return getDefault();
        }
        Avatar avatar = avatars.get(one.getId());
        return avatar.getImageData();
    }

    private byte[] getDefault() throws IOException {
        InputStream defaultIcon = ClassLoader.getSystemResourceAsStream("static/images/default.bmp");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        while (defaultIcon.available() > 0) {
            int read = defaultIcon.read(buffer);
            byteArrayOutputStream.write(buffer, 0, read);
        }

        return byteArrayOutputStream.toByteArray();
    }
}
