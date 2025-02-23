import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

class Expense {
    private String date;
    private String category;
    private double amount;

    public Expense(String date, String category, double amount) {
        this.date = date;
        this.category = category;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }
    public static void main(String[] args){}
}

class ExpenseTracker {
    private List<Expense> expenses;
    private String currentUser;

    public ExpenseTracker(String username) {
        this.currentUser = username;
        expenses = new ArrayList<>();
        loadExpenses();
    }

    private void loadExpenses() {
        String filename = currentUser + "_expenses.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue;
                try {
                    String date = parts[0].trim();
                    String category = parts[1].trim();
                    double amount = Double.parseDouble(parts[2].trim());
                    expenses.add(new Expense(date, category, amount));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid expense entry: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("No existing expenses found. Starting fresh.");
        }
    }

    public void saveExpenses() {
        String filename = currentUser + "_expenses.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Expense e : expenses) {
                String line = String.format("%s,%s,%.2f",
                        e.getDate(), e.getCategory(), e.getAmount());
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving expenses: " + e.getMessage());
        }
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    public void editExpense(int index, Expense newExpense) {
        if (index >= 0 && index < expenses.size()) {
            expenses.set(index, newExpense);
        }
    }

    public void deleteExpense(int index) {
        if (index >= 0 && index < expenses.size()) {
            expenses.remove(index);
        }
    }

    public List<Expense> getExpenses() {
        return new ArrayList<>(expenses);
    }

    public Map<String, Double> getCategorySummaries() {
        Map<String, Double> summaries = new HashMap<>();
        for (Expense e : expenses) {
            String category = e.getCategory();
            summaries.put(category, summaries.getOrDefault(category, 0.0) + e.getAmount());
        }
        return summaries;
    }
}

public class Main {
    private static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\nWelcome to Expense Tracker");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");

            int choice = getIntInput(scanner);
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    loginUser(scanner);
                    break;
                case 2:
                    registerUser(scanner);
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private static int getIntInput(Scanner scanner) {
        while (true) {
            try {
                return scanner.nextInt();
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Enter a number: ");
                scanner.nextLine();
            }
        }
    }

    private static void loginUser(Scanner scanner) {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        if (validateUser(username, password)) {
            System.out.println("\nLogin successful!");
            ExpenseTracker tracker = new ExpenseTracker(username);
            showMainMenu(scanner, tracker);
        } else {
            System.out.println("Invalid username or password.");
        }
    }

    private static void registerUser(Scanner scanner) {
        System.out.print("\nEnter new username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username cannot be empty.");
            return;
        }

        if (userExists(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter new password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }

        saveUser(username, password);
        System.out.println("Registration successful. You can now login.");
    }

    private static boolean userExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private static boolean validateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    private static void saveUser(String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    private static void showMainMenu(Scanner scanner, ExpenseTracker tracker) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\nMain Menu");
            System.out.println("1. Add Expense");
            System.out.println("2. View/Edit Expenses");
            System.out.println("3. View Category Summary");
            System.out.println("4. Save and Logout");
            System.out.print("Enter choice: ");

            int choice = getIntInput(scanner);
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    addExpense(scanner, tracker);
                    break;
                case 2:
                    viewExpenses(scanner, tracker);
                    break;
                case 3:
                    viewCategorySummary(tracker);
                    break;
                case 4:
                    tracker.saveExpenses();
                    loggedIn = false;
                    System.out.println("Expenses saved. Logging out.");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addExpense(Scanner scanner, ExpenseTracker tracker) {
        System.out.println("\nAdd New Expense");
        String date = getValidDate(scanner);
        if (date == null) return;

        System.out.print("Enter category: ");
        String category = scanner.nextLine().trim();
        if (category.contains(",")) {
            System.out.println("Category cannot contain commas.");
            return;
        }

        double amount = getValidAmount(scanner);
        if (amount < 0) return;

        tracker.addExpense(new Expense(date, category, amount));
        tracker.saveExpenses();
        System.out.println("Expense added successfully.");
    }

    private static String getValidDate(Scanner scanner) {
        System.out.print("Enter date (yyyy-MM-dd): ");
        String date = scanner.nextLine().trim();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(date);
            return date;
        } catch (ParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
            return null;
        }
    }

    private static double getValidAmount(Scanner scanner) {
        System.out.print("Enter amount: ");
        while (true) {
            try {
                double amount = Double.parseDouble(scanner.nextLine());
                if (amount < 0) throw new NumberFormatException();
                return amount;
            } catch (NumberFormatException e) {
                System.out.print("Invalid amount. Enter a positive number: ");
            }
        }
    }

    private static void viewExpenses(Scanner scanner, ExpenseTracker tracker) {
        List<Expense> expenses = tracker.getExpenses();
        if (expenses.isEmpty()) {
            System.out.println("\nNo expenses to display.");
            return;
        }

        System.out.println("\nExpense List:");
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            System.out.printf("%d. %s | %s | $%.2f%n",
                    i + 1, e.getDate(), e.getCategory(), e.getAmount());
        }

        System.out.print("\nEnter expense number to edit/delete (0 to cancel): ");
        int index = getIntInput(scanner) - 1;
        scanner.nextLine(); // Consume newline

        if (index == -1) return;
        if (index < 0 || index >= expenses.size()) {
            System.out.println("Invalid expense number.");
            return;
        }

        System.out.println("1. Edit\n2. Delete");
        System.out.print("Choose action: ");
        int action = getIntInput(scanner);
        scanner.nextLine(); // Consume newline

        if (action == 1) {
            editExpense(scanner, tracker, index);
        } else if (action == 2) {
            tracker.deleteExpense(index);
            tracker.saveExpenses();
            System.out.println("Expense deleted.");
        } else {
            System.out.println("Invalid action.");
        }
    }

    private static void editExpense(Scanner scanner, ExpenseTracker tracker, int index) {
        System.out.println("\nEdit Expense");
        String date = getValidDate(scanner);
        if (date == null) return;

        System.out.print("Enter new category: ");
        String category = scanner.nextLine().trim();
        if (category.contains(",")) {
            System.out.println("Category cannot contain commas.");
            return;
        }

        double amount = getValidAmount(scanner);
        if (amount < 0) return;

        tracker.editExpense(index, new Expense(date, category, amount));
        tracker.saveExpenses();
        System.out.println("Expense updated.");
    }

    private static void viewCategorySummary(ExpenseTracker tracker) {
        Map<String, Double> summaries = tracker.getCategorySummaries();
        if (summaries.isEmpty()) {
            System.out.println("\nNo expenses to summarize.");
            return;
        }

        System.out.println("\nCategory Summary:");
        for (Map.Entry<String, Double> entry : summaries.entrySet()) {
            System.out.printf("%-15s $%.2f%n", entry.getKey(), entry.getValue());
        }
    }
}
