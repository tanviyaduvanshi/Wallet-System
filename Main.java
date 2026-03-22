import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

// Wallet class
class Wallet {
    private int balance;
    private final ReentrantLock lock = new ReentrantLock();

    public Wallet(int initialBalance) {
        this.balance = initialBalance;
    }

    public int getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public void credit(int amount) {
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    public boolean debit(int amount) {
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
}

// Wallet Service
class WalletService {
    private final ConcurrentHashMap<String, Wallet> userWallets = new ConcurrentHashMap<>();

    public void createUser(String userId, int initialBalance) {
        userWallets.putIfAbsent(userId, new Wallet(initialBalance));
    }

    public int getBalance(String userId) {
        Wallet wallet = userWallets.get(userId);
        if (wallet == null) {
            throw new RuntimeException("User not found");
        }
        return wallet.getBalance();
    }

    public void credit(String userId, int amount) {
        Wallet wallet = userWallets.get(userId);
        if (wallet == null) {
            throw new RuntimeException("User not found");
        }
        wallet.credit(amount);
    }

    public boolean debit(String userId, int amount) {
        Wallet wallet = userWallets.get(userId);
        if (wallet == null) {
            throw new RuntimeException("User not found");
        }
        return wallet.debit(amount);
    }
}

// Main class
public class Main {
    public static void main(String[] args) {
        WalletService service = new WalletService();
        service.createUser("u1", 100);

        Runnable task1 = () -> {
            boolean result = service.debit("u1", 50);
            System.out.println("Thread1 debit 50: " + result);
        };

        Runnable task2 = () -> {
            boolean result = service.debit("u1", 70);
            System.out.println("Thread2 debit 70: " + result);
        };

        Thread t1 = new Thread(task1);
        Thread t2 = new Thread(task2);

        t1.start();
        t2.start();
    }
}