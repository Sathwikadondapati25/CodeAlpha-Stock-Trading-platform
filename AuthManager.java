import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthManager {
    private final StorageManager storageManager;
    private User currentUser;
    private Map<String, String> userCredentials;

    public AuthManager(StorageManager storageManager) {
        this.storageManager = storageManager;
        this.userCredentials = storageManager.loadUsers();
    }

    public synchronized boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        username = username.trim();
        if (userCredentials.containsKey(username)) {
            return false;
        }

        String hashedPassword = hashPassword(password);
        storageManager.saveUserCredentials(username, hashedPassword);
        storageManager.saveUserBalance(username, 100000.0);
        userCredentials.put(username, hashedPassword);
        return true;
    }

    public synchronized boolean login(String username, String password) {
        if (username == null || password == null) return false;
        username = username.trim();
        if (!userCredentials.containsKey(username)) {
            return false;
        }

        String storedHash = userCredentials.get(username);
        String enteredHash = hashPassword(password);
        if (storedHash.equals(enteredHash)) {
            double balance = storageManager.loadUserBalance(username);
            currentUser = new User(username, storedHash, balance);
            return true;
        }
        return false;
    }

    public synchronized void logout() {
        currentUser = null;
    }

    public synchronized User getCurrentUser() {
        return currentUser;
    }

    public synchronized boolean isLoggedIn() {
        return currentUser != null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    public synchronized boolean verifyPassword(String username, String password) {
        if (!userCredentials.containsKey(username)) {
            return false;
        }
        String storedHash = userCredentials.get(username);
        String enteredHash = hashPassword(password);
        return storedHash.equals(enteredHash);
    }

    public synchronized void changePassword(String username, String newPassword) {
        String hashedPassword = hashPassword(newPassword);
        storageManager.saveUserCredentials(username, hashedPassword);
        userCredentials.put(username, hashedPassword);
        if (currentUser != null && currentUser.getUsername().equals(username)) {
            currentUser = new User(username, hashedPassword, currentUser.getBalance());
        }
    }
}
