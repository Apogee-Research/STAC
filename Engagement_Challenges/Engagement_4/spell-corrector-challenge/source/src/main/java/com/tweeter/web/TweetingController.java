package com.tweeter.web;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tweeter.model.Avatar;
import com.tweeter.model.TweetCorrectionService;
import com.tweeter.model.hibernate.HashTag;
import com.tweeter.model.hibernate.Tweet;
import com.tweeter.model.hibernate.User;
import com.tweeter.model.hibernate.UserRole;
import com.tweeter.model.repositories.HashTagRepository;
import com.tweeter.model.repositories.TweetRepository;
import com.tweeter.model.repositories.UserRepository;
import com.tweeter.model.repositories.UserRolesRepository;
import com.tweeter.service.AvatarService;
import com.tweeter.service.SpellingCorrectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * All of the non-avatar related routes are accessible via this controller.
 */
@Controller
public class TweetingController {

    private static final Pattern INVALID_USERNAME = Pattern.compile("[^a-zA-Z]");
    private static final SecureRandom RNG = new SecureRandom();

    private final PasswordEncoder passwordEncoder;
    private final SpellingCorrectionService spellingCorrectionService;
    private final TweetCorrectionService tweetCorrectionService;
    private final UserRepository users;
    private final UserRolesRepository user_roles;
    private final TweetRepository tweets;
    private final HashTagRepository hashTags;
    private final AvatarService avatars;

    @Autowired
    public TweetingController(@Qualifier("userRepository") UserRepository users,
                              @Qualifier("userRolesRepository") UserRolesRepository user_roles,
                              @Qualifier("tweetRepository") TweetRepository tweets,
                              @Qualifier("hashTagRepository") HashTagRepository hashTags,
                              @Qualifier("avatarService") AvatarService avatars,
                              @Qualifier("spellingCorrectionService") SpellingCorrectionService spellingCorrectionService,
                              @Qualifier("passwordEncoder") PasswordEncoder passwordEncoder,
                              @Qualifier("tweetCorrectionService") TweetCorrectionService tweetCorrectionService) {
        this.tweetCorrectionService = tweetCorrectionService;
        this.passwordEncoder = passwordEncoder;
        this.spellingCorrectionService = spellingCorrectionService;
        this.avatars = avatars;
        this.hashTags = hashTags;
        this.tweets = tweets;
        this.user_roles = user_roles;
        this.users = users;
    }

    @RequestMapping(value = "/follow/author/{username:.+}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void follow(@PathVariable String username, HttpServletResponse response, HttpServletRequest httpServletRequest) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            response.sendRedirect("/login");
            return;
        }
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User thisUser = users.findOneByUsername(name);
        User otherUser = users.findOneByUsername(username);
        if (name.equals(username)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User attempted to follow one's self.");
        } else if (thisUser == null || otherUser == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to retrieve a requested user.");
        } else {
            Set<User> following = thisUser.getFollowing();
            following.add(otherUser);
            users.save(thisUser);
        }
    }

    @RequestMapping(value = "/unfollow/author/{username:.+}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable String username, HttpServletResponse response, HttpServletRequest httpServletRequest) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            response.sendRedirect("/login");
            return;
        }
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        User thisUser = users.findOneByUsername(name);
        User otherUser = users.findOneByUsername(username);
        if (name.equals(username)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "User attempted to follow one's self.");
        } else if (thisUser == null || otherUser == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to retrieve a requested user.");
        } else {
            Set<User> following = thisUser.getFollowing();
            following.remove(otherUser);
            users.save(thisUser);
        }
    }

    @RequestMapping(value = "/tweet", method = RequestMethod.POST)
    public String tweet(@RequestParam(value = "value", required = true) String tweet, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return "redirect:/";
        }
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (tweet.length() <= 140) {
            spellingCorrectionService.correctAndSaveTweet(name, tweet);
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String twitterRegister() {
        return "register";
    }

    @RequestMapping(value = "/register/create", method = RequestMethod.GET)
    public void twitterRegisterCreateGET(HttpServletResponse response) throws IOException {
        response.sendRedirect("/register");
    }

    @RequestMapping(value = "/register/create", method = RequestMethod.POST)
    public String twitterRegisterCreate(
            @RequestParam String fullname,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String password2,
            @RequestParam(required = false, defaultValue = "0") String size,
            HttpServletRequest request) throws NoSuchAlgorithmException, SQLException, IOException {

        if (!password.equals(password2)) {
            return "redirect:/register?passwords_not_matching";
        }

        if (INVALID_USERNAME.matcher(username).find()) {
            return "redirect:/register?invalid_username";
        }

        if (users.findOneByUsername(username) != null) {
            return "redirect:/register?already_registered";
        }

        User user = new User(fullname, username, passwordEncoder.encode(password), request.getRemoteAddr());
        UserRole role = new UserRole(user.getUsername());
        user_roles.save(role);
        users.save(user);
        Avatar avatar = new Avatar(user, parseDimensions(size));
        avatars.put(user.getId(), avatar);

        return "redirect:/";
    }

    private int parseDimensions(String size) {
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView twitterLoginGet() throws IOException, ServletException {
        return new ModelAndView("login");
    }

    @RequestMapping(value = "/logout")
    public void logout(HttpServletRequest httpServletRequest) throws IOException, ServletException {
        //httpServletRequest.getSession().invalidate();
    }

    @RequestMapping(value = "/")
    public ModelAndView twitterIndex(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        ModelAndView twitter = new ModelAndView("twitter");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return new ModelAndView("login");
        }
        twitter.addObject("username", authentication.getName());
        twitter.addObject("user", user);
        twitter.addObject("state", "index");
        twitter.addObject("tweetCount", tweets.countAllByAuthor(user));
        twitter.addObject("followingCount", user != null ? user.getFollowing().size() : 0);
        twitter.addObject("followersCount", users.findByFollowing(user).size());
        twitter.addObject("tweets", tweets.findTop20ByActionRequiredFalseOrderByTweetedDesc());
        twitter.addObject("notifications", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user).size());
        twitter.addObject("randoms", randomSelect(users.findAll(), 5));
        twitter.addObject("trending", getTrending());

        return twitter;
    }

    @RequestMapping(value = "/following/{username:.+}")
    public ModelAndView followingIndex(@PathVariable String username, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        ModelAndView twitter = new ModelAndView("twitter");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return new ModelAndView("login");
        }
        twitter.addObject("username", username);
        twitter.addObject("user", user);
        twitter.addObject("state", "following");
        twitter.addObject("tweetCount", tweets.countAllByAuthor(user));
        twitter.addObject("followingCount", user != null ? user.getFollowing().size() : 0);
        twitter.addObject("followersCount", users.findByFollowing(user).size());
        twitter.addObject("randoms", randomSelect(users.findAll(), 5));
        twitter.addObject("trending", getTrending());
        twitter.addObject("notifications", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user).size());

        return twitter;
    }

    @RequestMapping(value = "/followers/{username:.+}")
    public ModelAndView followersIndex(@PathVariable String username, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        ModelAndView twitter = new ModelAndView("twitter");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return new ModelAndView("login");
        }
        twitter.addObject("username", username);
        twitter.addObject("user", user);
        twitter.addObject("state", "followers");
        twitter.addObject("tweetCount", tweets.countAllByAuthor(user));
        twitter.addObject("followingCount", user != null ? user.getFollowing().size() : 0);
        twitter.addObject("followersCount", users.findByFollowing(user).size());
        twitter.addObject("followers", users.findByFollowing(user));
        twitter.addObject("randoms", randomSelect(users.findAll(), 5));
        twitter.addObject("trending", getTrending());
        twitter.addObject("notifications", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user).size());

        return twitter;
    }

    @RequestMapping(value = "/i/moments")
    public String other(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        return "redirect:/";
    }

    @RequestMapping(value = "/i/notifications")
    public ModelAndView notifications(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        ModelAndView twitter = new ModelAndView("twitter");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return new ModelAndView("login");
        }

        twitter.addObject("username", authentication.getName());
        twitter.addObject("user", user);
        twitter.addObject("state", "messages");
        twitter.addObject("tweetCount", tweets.countAllByAuthor(user));
        twitter.addObject("followingCount", user != null ? user.getFollowing().size() : 0);
        twitter.addObject("followersCount", users.findByFollowing(user).size());
        twitter.addObject("messages", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user));
        twitter.addObject("randoms", randomSelect(users.findAll(), 5));
        twitter.addObject("trending", getTrending());
        twitter.addObject("notifications", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user).size());

        return twitter;
    }

    @RequestMapping(value = "/hashtag/{tag:.+}")
    public ModelAndView hashtags(@PathVariable String tag, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        ModelAndView twitter = new ModelAndView("twitter");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return new ModelAndView("login");
        }

        twitter.addObject("username", authentication.getName());
        twitter.addObject("user", user);
        twitter.addObject("state", "hashtags");
        twitter.addObject("tweetCount", tweets.countAllByAuthor(user));
        twitter.addObject("followingCount", user != null ? user.getFollowing().size() : 0);
        twitter.addObject("followersCount", users.findByFollowing(user).size());
        twitter.addObject("tweets", tweets.findByHashTagsAndActionRequiredFalseOrderByTweetedDesc(hashTags.findOneByHashTag(tag)));
        twitter.addObject("randoms", randomSelect(users.findAll(), 5));
        twitter.addObject("trending", getTrending());
        twitter.addObject("notifications", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user).size());

        return twitter;
    }

    @RequestMapping(value = "/author/{username:.+}")
    public ModelAndView author(@PathVariable String username, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        ModelAndView twitter = new ModelAndView("twitter");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return new ModelAndView("login");
        }

        twitter.addObject("username", authentication.getName());
        twitter.addObject("user", user);
        twitter.addObject("state", "index");
        twitter.addObject("tweetCount", tweets.countAllByAuthor(user));
        twitter.addObject("followingCount", user != null ? user.getFollowing().size() : 0);
        twitter.addObject("followersCount", users.findByFollowing(user).size());
        List<Tweet> tweetsOfUsername = new ArrayList<>();
        tweetsOfUsername.addAll(tweets.findByAuthorAndActionRequiredFalseOrderByTweetedDesc(users.findOneByUsername(username)));
        tweetsOfUsername.addAll(tweets.findByAtMentionsAndActionRequiredFalseOrderByTweetedDesc(users.findOneByUsername(username)));
        twitter.addObject("tweets", tweetsOfUsername);
        twitter.addObject("randoms", randomSelect(users.findAll(), 5));
        twitter.addObject("trending", getTrending());
        twitter.addObject("notifications", tweets.findByAuthorAndActionRequiredTrueOrderByTweetedDesc(user).size());

        return twitter;
    }

    @RequestMapping(value = "/changeWord")
    public String changeWord(@RequestParam String messageId, @RequestParam String wordIndex, @RequestParam(defaultValue = "false", required = false) String finish, @RequestParam(defaultValue = "false", required = false) String change, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return "redirect:/";
        }
        boolean fin = finish.equalsIgnoreCase("true");
        boolean chg = change.equalsIgnoreCase("true");

        long mId = Long.decode(messageId);
        int wId = Integer.decode(wordIndex);

        if (chg && !fin) {
            tweetCorrectionService.nextCorrection(mId, wId);
        }

        if (fin && !chg) {
            tweetCorrectionService.finalizeCorrections(mId, wId);
        }

        return "redirect:/i/notifications";
    }

    @RequestMapping(value = "/deleteStaged")
    public String deleteStaged(@RequestParam String messageId, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = users.findOneByUsername(authentication.getName());
        if (authentication.isAuthenticated() && user == null) {
            httpServletRequest.getSession().invalidate();
            httpServletResponse.sendRedirect("/login");
            return "redirect:/";
        }
        long mId = Long.decode(messageId);
        Tweet one = tweets.findOne(mId);
        if (one.getActionRequired()) {
            tweets.delete(one);
        }
        return "redirect:/i/notifications";
    }

    @RequestMapping(value = "/undefined")
    public String redirectFaulty() {
        return "redirect:/";
    }

    public Iterable<ObjectNode> getTrending() {
        Iterable<HashTag> all = randomSelect(hashTags.findAll(), 10);

        JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(true);
        ArrayList<ObjectNode> converted = new ArrayList<>();

        for (HashTag hashTag : all) {
            ObjectNode jsonNodes = jsonNodeFactory.objectNode();
            jsonNodes.put("hashTag", hashTag.getHashTag());

            double count = tweets.countAllByHashTagsIn(hashTag);
            String cnt;

            if (count == 1) {
                cnt = "1 Tweet";
            } else if (count < 1000) {
                cnt = String.format("%d %s", (int)count, " Tweets");
            } else if (count < 1000000) {
                cnt = String.format("%.2f %s", count / 1000.0, "K Tweets");
            } else if (count < 1000000000) {
                cnt = String.format("%.2f %s", count / 1000000.0, "M Tweets");
            } else {
                cnt = String.format("%.2f %s", count / 1000000000.0, "B Tweets");
            }

            jsonNodes.put("tweeted", cnt);
            converted.add(jsonNodes);
        }

        return converted;
    }

    public <T> List<T> randomSelect(Iterable<T> iterable, int number) {
        List<T> randomQuestions = new ArrayList<>();
        List<T> copy = new ArrayList<>((Collection<? extends T>) iterable);

        int min = Math.min(number, copy.size());
        for (int i = 0; i < min; i++) randomQuestions.add(copy.remove(RNG.nextInt(copy.size())));

        return randomQuestions;
    }
}
