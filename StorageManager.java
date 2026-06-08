import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StorageManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.properties";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public StorageManager() {
        createDirectory(new File(DATA_DIR));
    }

    private void createDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public synchronized Map<String, String> loadUsers() {
        Map<String, String> users = new HashMap<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return users;
        }
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            props.load(input);
            for (String key : props.stringPropertyNames()) {
                users.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    public synchronized void saveUserCredentials(String username, String password) {
        File file = new File(USERS_FILE);
        Properties props = new Properties();
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Error loading users for update: " + e.getMessage());
            }
        }
        props.setProperty(username, password);
        try (OutputStream output = new FileOutputStream(file)) {
            props.store(output, "User Credentials");
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }

        // Save account creation date in profile if not exists
        Properties profile = loadUserProfile(username);
        if (!profile.containsKey("createdDate")) {
            profile.setProperty("createdDate", java.time.LocalDateTime.now().format(DATE_FORMATTER));
            saveUserProfile(username, profile);
        }
    }

    private File getUserDir(String username) {
        File userDir = new File(DATA_DIR + "/" + username);
        createDirectory(userDir);
        return userDir;
    }

    public synchronized double loadUserBalance(String username) {
        File profileFile = new File(getUserDir(username), "profile.properties");
        if (!profileFile.exists()) {
            return 100000.0; // Default balance
        }
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(profileFile)) {
            props.load(input);
            String balanceStr = props.getProperty("balance");
            if (balanceStr != null) {
                return Double.parseDouble(balanceStr);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading user balance: " + e.getMessage());
        }
        return 100000.0;
    }

    public synchronized void saveUserBalance(String username, double balance) {
        File profileFile = new File(getUserDir(username), "profile.properties");
        Properties props = new Properties();
        props.setProperty("username", username);
        props.setProperty("balance", String.valueOf(balance));
        try (OutputStream output = new FileOutputStream(profileFile)) {
            props.store(output, "User Profile");
        } catch (IOException e) {
            System.err.println("Error saving user balance: " + e.getMessage());
        }
    }

    public synchronized Properties loadUserProfile(String username) {
        File profileFile = new File(getUserDir(username), "profile.properties");
        Properties props = new Properties();
        if (profileFile.exists()) {
            try (InputStream input = new FileInputStream(profileFile)) {
                props.load(input);
            } catch (IOException e) {
                System.err.println("Error loading profile: " + e.getMessage());
            }
        }
        return props;
    }

    public synchronized void saveUserProfile(String username, Properties props) {
        File profileFile = new File(getUserDir(username), "profile.properties");
        try (OutputStream output = new FileOutputStream(profileFile)) {
            props.store(output, "User Profile");
        } catch (IOException e) {
            System.err.println("Error saving profile: " + e.getMessage());
        }
    }

    public synchronized java.util.List<Holding> loadPortfolio(String username) {
        java.util.List<Holding> portfolio = new ArrayList<>();
        File portfolioFile = new File(getUserDir(username), "portfolio.txt");
        if (!portfolioFile.exists()) {
            return portfolio;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(portfolioFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String symbol = parts[0];
                    int quantity = Integer.parseInt(parts[1]);
                    double avgPrice = Double.parseDouble(parts[2]);
                    portfolio.add(new Holding(symbol, quantity, avgPrice));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading portfolio: " + e.getMessage());
        }
        return portfolio;
    }

    public synchronized void savePortfolio(String username, java.util.List<Holding> holdings) {
        File portfolioFile = new File(getUserDir(username), "portfolio.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(portfolioFile))) {
            for (Holding holding : holdings) {
                writer.write(String.format("%s,%d,%.2f\n",
                        holding.getSymbol(),
                        holding.getQuantity(),
                        holding.getAveragePrice()));
            }
        } catch (IOException e) {
            System.err.println("Error saving portfolio: " + e.getMessage());
        }
    }

    public synchronized java.util.List<Transaction> loadTransactions(String username) {
        java.util.List<Transaction> transactions = new ArrayList<>();
        File transFile = new File(getUserDir(username), "transactions.txt");
        if (!transFile.exists()) {
            return transactions;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(transFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    LocalDateTime timestamp = LocalDateTime.parse(parts[0], DATE_FORMATTER);
                    Transaction.Type type = Transaction.Type.valueOf(parts[1]);
                    String symbol = parts[2];
                    int quantity = Integer.parseInt(parts[3]);
                    double price = Double.parseDouble(parts[4]);
                    double total = Double.parseDouble(parts[5]);
                    transactions.add(new Transaction(timestamp, type, symbol, quantity, price, total));
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
        // Return reverse sorted (newest first)
        transactions.sort((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp()));
        return transactions;
    }

    public synchronized void saveTransaction(String username, Transaction transaction) {
        File transFile = new File(getUserDir(username), "transactions.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(transFile, true))) {
            writer.write(String.format("%s,%s,%s,%d,%.2f,%.2f\n",
                    transaction.getTimestamp().format(DATE_FORMATTER),
                    transaction.getType().name(),
                    transaction.getSymbol(),
                    transaction.getQuantity(),
                    transaction.getPrice(),
                    transaction.getTotal()));
        } catch (IOException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    // Watchlist Management
    public synchronized java.util.List<String> loadWatchlist(String username) {
        java.util.List<String> watchlist = new ArrayList<>();
        File watchlistFile = new File(getUserDir(username), "watchlist.txt");
        if (!watchlistFile.exists()) {
            return watchlist;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(watchlistFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                watchlist.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading watchlist: " + e.getMessage());
        }
        return watchlist;
    }

    public synchronized void saveWatchlist(String username, java.util.List<String> watchlist) {
        File watchlistFile = new File(getUserDir(username), "watchlist.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(watchlistFile))) {
            for (String symbol : watchlist) {
                writer.write(symbol + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving watchlist: " + e.getMessage());
        }
    }

    public synchronized void addToWatchlist(String username, String symbol) {
        java.util.List<String> watchlist = loadWatchlist(username);
        if (!watchlist.contains(symbol)) {
            watchlist.add(symbol);
            saveWatchlist(username, watchlist);
        }
    }

    public synchronized void removeFromWatchlist(String username, String symbol) {
        java.util.List<String> watchlist = loadWatchlist(username);
        watchlist.remove(symbol);
        saveWatchlist(username, watchlist);
    }

    // Price Alerts Management
    public synchronized void saveAlerts(String username, java.util.Map<String, Double> alerts) {
        File alertsFile = new File(getUserDir(username), "alerts.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(alertsFile))) {
            for (java.util.Map.Entry<String, Double> entry : alerts.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue() + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error saving alerts: " + e.getMessage());
        }
    }

    public synchronized java.util.Map<String, Double> loadAlerts(String username) {
        java.util.Map<String, Double> alerts = new java.util.HashMap<>();
        File alertsFile = new File(getUserDir(username), "alerts.txt");
        if (!alertsFile.exists()) {
            return alerts;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(alertsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    alerts.put(parts[0], Double.parseDouble(parts[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading alerts: " + e.getMessage());
        }
        return alerts;
    }

    public synchronized void addAlert(String username, String symbol, double targetPrice) {
        java.util.Map<String, Double> alerts = loadAlerts(username);
        alerts.put(symbol, targetPrice);
        saveAlerts(username, alerts);
    }

    public synchronized void removeAlert(String username, String symbol) {
        java.util.Map<String, Double> alerts = loadAlerts(username);
        alerts.remove(symbol);
        saveAlerts(username, alerts);
    }
}
