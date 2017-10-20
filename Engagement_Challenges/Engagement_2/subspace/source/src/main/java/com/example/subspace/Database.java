package com.example.subspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.util.GeoMultiTrie;
import com.example.util.GeoPoint;
import com.example.util.GeoSearchResult;

/**
 * Store all the persistent data for the subspace app.
 */
public class Database
    implements Serializable
{
    private static final long serialVersionUID = 0L;

    private static final Logger LOGGER =
        Logger.getLogger(Database.class.getName());

    /**
     * Minimum number of threads to maintain in {@link
     * #executorService}.
     */
    private static final int CORE_POOL_SIZE = 4;

    /**
     * Load the database from the specified file, or create a new
     * database if the file does not exist yet.
     */
    public static Database load(
        String filename)
        throws IOException, SecurityException
    {
        Database db = null;

        File file = new File(filename);

        // Try to load from an existing file.
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try
        {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);

            db = (Database)ois.readObject();
            db.backingFile = file;
        }
        catch (FileNotFoundException e)
        {
            // Mark the database as not loaded so it can be created
            // below.
            db = null;
        }
        catch (ClassCastException e)
        {
            throw new IOException("Invalid database: " + filename);
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Invalid database: " + filename);
        }
        finally
        {
            if (ois != null)
            {
                ois.close();
            }
            else if (fis != null)
            {
                fis.close();
            }
        }

        if (db == null)
        {
            // Create a new database.
            db = new Database(file);
            db.save();
        }

        return db;
    }

    /**
     * Lock over the whole database.
     */
    protected transient ReadWriteLock lock;

    /**
     * File that backs this database.
     */
    private transient File backingFile;

    /**
     * Service for delayed database operations that require a write
     * lock.
     */
    protected transient ScheduledExecutorService writeExecutorService;

    /**
     * Service for all other delayed database operations.
     */
    protected transient ScheduledExecutorService executorService;

    /**
     * Map from username to user object.
     */
    protected Map<String, User> usernameToUser;

    /**
     * Map from registration confirmation token to username.
     *
     * Only users in the {@link User.Status#NEW} state should be in
     * this map.
     */
    protected Map<String, String> tokenToUsername;

    /**
     * Map from user email address to username.
     */
    protected Map<String, String> emailToUsername;

    /**
     * Map from user location to username.
     */
    protected GeoMultiTrie<String> locationToUsername;

    /**
     * Map from anonymous email alias to the set of usernames who can
     * send email to the alias, and who should receive email from the
     * alias.
     */
    protected Map<String, Set<String>> aliasToUsers;

    /**
     * Random number generator.
     */
    protected transient Random random;

    /**
     * Create a new, empty database.
     */
    protected Database(
        File backingFile)
    {
        lock = new ReentrantReadWriteLock(true);

        this.backingFile = backingFile;

        writeExecutorService =
            Executors.newSingleThreadScheduledExecutor();

        executorService =
            Executors.newScheduledThreadPool(CORE_POOL_SIZE);

        usernameToUser = new HashMap<String, User>();

        tokenToUsername = new HashMap<String, String>();

        emailToUsername = new HashMap<String, String>();

        locationToUsername = new GeoMultiTrie<String>();

        aliasToUsers = new HashMap<String, Set<String>>();

        random = new SecureRandom();
    }

    private void readObject(
        ObjectInputStream in)
        throws ClassNotFoundException, IOException
    {
        in.defaultReadObject();

        lock = new ReentrantReadWriteLock(true);

        writeExecutorService =
            Executors.newSingleThreadScheduledExecutor();

        executorService =
            Executors.newScheduledThreadPool(CORE_POOL_SIZE);

        random = new SecureRandom();
    }

    /**
     * Save the database to its backing file.
     */
    public void save()
        throws IOException, SecurityException
    {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;

        lock.readLock().lock();
        try
        {
            File backingDir = backingFile.getParentFile();
            if (backingDir == null)
            {
                throw new IOException(String.format(
                    "Unable to get parent directory of: %s",
                    backingFile));
            }

            File tmp = File.createTempFile(
                "subspace",
                null,
                backingDir);

            fos = new FileOutputStream(tmp);
            oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            Files.move(
                tmp.toPath(),
                backingFile.toPath(),
                StandardCopyOption.ATOMIC_MOVE);
        }
        finally
        {
            lock.readLock().unlock();

            if (oos != null)
            {
                oos.close();
            }
            else if (fos != null)
            {
                fos.close();
            }
        }
    }

    /**
     * Task to call {@link #save()}.
     */
    private class SaveTask
        implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                save();
            }
            catch (IOException e)
            {
                LOGGER.log(
                    Level.WARNING,
                    "Error saving the database",
                     e);
            }
        }
    }

    /**
     * Provide a hint that the database should be saved at some point
     * in the future.
     */
    public void saveHint()
    {
        executorService.submit(new SaveTask());
    }

    /**
     * Retrieve user information by username, or null if the user does
     * not exist (or is not active).
     */
    public User getUser(
        String username)
    {
        lock.readLock().lock();
        try
        {
            User user = usernameToUser.get(username);
            if (user == null || !user.isActive())
            {
                return null;
            }

            return user;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Same as {@link #getUser(String)}, but retrieve by email
     * address.
     */
    public User getUserByEmail(
        String emailAddress)
    {
        lock.readLock().lock();
        try
        {
            String username = emailToUsername.get(emailAddress);
            if (username == null)
            {
                return null;
            }

            return getUser(username);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Register a new user (in the unconfirmed state).
     *
     * @see #confirmUser(String)
     *
     * @return
     *     The registration confirmation token, or null if
     *     registration failed.
     */
    public String registerUser(
        String username,
        String password,
        String emailAddress)
    {
        boolean changed = false;

        lock.writeLock().lock();
        try
        {
            boolean conflict = false;
            if (usernameToUser.containsKey(username))
            {
                conflict = true;
            }
            if (emailToUsername.containsKey(emailAddress))
            {
                conflict = true;
            }
            if (conflict)
            {
                return null;
            }

            changed = true;

            User user = new User(
                username,
                password,
                emailAddress);

            String token = UUID.randomUUID().toString();

            usernameToUser.put(username, user);

            tokenToUsername.put(token, username);

            emailToUsername.put(emailAddress, username);

            return token;
        }
        finally
        {
            lock.writeLock().unlock();

            if (changed)
            {
                saveHint();
            }
        }
    }

    /**
     * Confirm a newly registered user.
     *
     * @see #registerUser(String, String, String)
     *
     * @return
     *     The successfully confirmed user, or null if confirmation
     *     failed.
     */
    public User confirmUser(
        String token)
    {
        boolean changed = false;

        lock.writeLock().lock();
        try
        {
            String username = tokenToUsername.remove(token);
            if (username == null)
            {
                return null;
            }

            changed = true;

            User user = usernameToUser.get(username);
            if (user == null)
            {
                LOGGER.log(
                    Level.SEVERE,
                    String.format(
                        "Username %s was in tokenToUsername but " +
                            "not usernameToUser.",
                        username));
                return null;
            }

            user.setStatus(User.Status.OK);

            return user;
        }
        finally
        {
            lock.writeLock().unlock();

            if (changed)
            {
                saveHint();
            }
        }
    }

    /**
     * Task to call {@code updateUserLocation} synchronously.
     */
    private class UpdateUserLocationTask
        implements Runnable
    {
        private User user;
        private GeoPoint location;

        UpdateUserLocationTask(
            User user,
            GeoPoint location)
        {
            this.user = user;

            if (location == null)
            {
                this.location = null;
            }
            else
            {
                this.location = new GeoPoint(location);
            }
        }

        @Override
        public void run()
        {
            updateUserLocation(user, location, true);
        }
    }

    /**
     * Update a user's location.
     *
     * @param user
     *     The user to update the location of.
     * @param location
     *     The user's new location. If this is null, the user's old
     *     location is removed and no new location is added.
     * @param synchronous
     *     If this is false, an update will be scheduled, and this
     *     function will return immediately. Otherwise, this function
     *     won't return until the update is complete.
     * @return
     *     In synchronous operation, null. In asynchronous operation,
     *     a Future object for the update operation.
     */
    public Future<?> updateUserLocation(
        User user,
        GeoPoint location,
        boolean synchronous)
    {
        if (!synchronous)
        {
            return writeExecutorService.submit(
                new UpdateUserLocationTask(user, location));
        }

        lock.writeLock().lock();
        try
        {
            synchronized (user)
            {
                GeoPoint oldLocation = user.getLocation();
                if (oldLocation != null)
                {
                    locationToUsername.remove(
                        oldLocation,
                        user.getUsername());
                }

                user.setLocation(location);

                if (location != null)
                {
                    locationToUsername.add(
                        user.getLocation(),
                        user.getUsername());
                }
            }

            return null;
        }
        finally
        {
            lock.writeLock().unlock();

            saveHint();
        }
    }

    /**
     * Given a user, find the closest other user, or null if the user
     * is alone.
     *
     * If the specified user does not yet have a location (and they
     * are not alone) this will return a randomly chosen user.
     */
    public User findNearestNeighbor(
        User user)
    {
        lock.readLock().lock();
        try
        {
            String username = user.getUsername();
            GeoPoint location = user.getLocation();
            if (location == null)
            {
                location = GeoPoint.random(random);
            }

            Iterator<GeoSearchResult<String>> searchIt =
                locationToUsername.search(location);

            while (searchIt.hasNext())
            {
                GeoSearchResult<String> result = searchIt.next();

                if (!username.equals(result.value))
                {
                    User neighbor = getUser(result.value);
                    if (neighbor != null)
                    {
                        return neighbor;
                    }
                }
            }

            return null;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Given an anonymous email alias, return the set of email
     * addresses of the alias's members, or null if the alias does not
     * exist.
     */
    public Set<String> getAliasMemberEmails(
        String alias)
    {
        lock.readLock().lock();
        try
        {
            Set<String> usernames = aliasToUsers.get(alias);
            if (usernames == null)
            {
                return null;
            }

            Set<String> emails = new HashSet<String>();
            for (String username : usernames)
            {
                User user = getUser(username);
                if (user == null)
                {
                    continue;
                }

                emails.add(user.getEmailAddress());
            }

            return emails;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    /**
     * Create a new alias shared by the specified users.
     *
     * If {@code users} contains the {@link InvalidUser#USER invalid
     * user}, then this method will take roughly as long as usual, and
     * an alias that was unique at some unspecified point in time will
     * be returned, but no alias will be registered.
     */
    public String registerAlias(
        AliasGenerator generator,
        User[] users)
    {
        lock.writeLock().lock();
        try
        {
            boolean containsInvalid = false;

            String alias;
            do
            {
                alias = generator.generateAlias();
            }
            while (aliasToUsers.containsKey(alias));

            Set<String> usernames = new HashSet<String>();
            for (User user : users)
            {
                containsInvalid =
                    (user instanceof InvalidUser) || containsInvalid;

                usernames.add(user.getUsername());
            }

            Map<String, Set<String>> discardAliasToUsers =
                new HashMap<String, Set<String>>();
            if (containsInvalid)
            {
                discardAliasToUsers.put(alias, usernames);
            }
            if (!containsInvalid)
            {
                aliasToUsers.put(alias, usernames);
            }

            return alias;
        }
        finally
        {
            lock.writeLock().unlock();

            saveHint();
        }
    }
}
