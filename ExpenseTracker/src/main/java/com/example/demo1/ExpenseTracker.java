package com.example.demo1;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

enum Category {
    FOOD, TRANSPORT, BILLS, ENTERTAINMENT, SHOPPING, OTHER
}

class Transaction {
    int id;
    LocalDate date;
    double amount;
    Category category;
    String note;

    public Transaction(int id, LocalDate date, double amount, Category category, String note) {
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.note = note;
    }

    @Override
    public String toString() {
        return id + " | " + date + " | " + category + " | " + amount + " | " + note;
    }
}

public class ExpenseTracker {
    private static final TreeMap<LocalDate, List<Transaction>> transactionsByDate = new TreeMap<>();
    private static final EnumMap<Category, List<Transaction>> transactionsByCategory = new EnumMap<>(Category.class);
    private static final TreeMap<YearMonth, Double> totalsByMonth = new TreeMap<>();
    private static final EnumMap<Category, Double> totalsByCategory = new EnumMap<>(Category.class);
    private static final Map<Integer, Transaction> transactionsById = new HashMap<>();

    private static int idCounter = 1;
    private static final Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        for (Category c : Category.values()) {
            transactionsByCategory.put(c, new LinkedList<>());
            totalsByCategory.put(c, 0.0);
        }

        while (true) {
            System.out.println("\n=== Expense Tracker ===");
            System.out.println("1) Add transaction");
            System.out.println("2) View all transactions");
            System.out.println("3) Search by category");
            System.out.println("4) Search by date range");
            System.out.println("5) Monthly summary");
            System.out.println("6) Category summary");
            System.out.println("7) Delete by ID");
            System.out.println("8) Exit");
            System.out.print("Pick option: ");

            int option = readInt();

            switch (option) {
                case 1 -> addTransaction();
                case 2 -> showAll();
                case 3 -> searchCategory();
                case 4 -> searchDateRange();
                case 5 -> showMonthlySummary();
                case 6 -> showCategorySummary();
                case 7 -> deleteById();
                case 8 -> {
                    System.out.println("Goodbye!");
                    in.close();
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addTransaction() {
        try {
            System.out.print("Enter date (YYYY-MM-DD): ");
            LocalDate date = LocalDate.parse(in.nextLine().trim());

            System.out.print("Amount: ");
            double amt = readDouble();

            System.out.println("Choose category:");
            Category[] cats = Category.values();
            for (int i = 0; i < cats.length; i++) {
                System.out.println((i + 1) + ". " + cats[i]);
            }
            int cIndex = readInt();
            Category cat = (cIndex >= 1 && cIndex <= cats.length) ? cats[cIndex - 1] : Category.OTHER;

            System.out.print("Description: ");
            String note = in.nextLine();

            Transaction t = new Transaction(idCounter++, date, amt, cat, note);

            transactionsByDate.computeIfAbsent(date, k -> new LinkedList<>()).add(t);
            transactionsByCategory.get(cat).add(t);
            totalsByMonth.put(YearMonth.from(date), totalsByMonth.getOrDefault(YearMonth.from(date), 0.0) + amt);
            totalsByCategory.put(cat, totalsByCategory.get(cat) + amt);
            transactionsById.put(t.id, t);

            System.out.println("Added transaction with id " + t.id);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format.");
        }
    }

    private static void showAll() {
        if (transactionsByDate.isEmpty()) {
            System.out.println("No data yet.");
            return;
        }
        for (Map.Entry<LocalDate, List<Transaction>> e : transactionsByDate.entrySet()) {
            for (Transaction t : e.getValue()) {
                System.out.println(t);
            }
        }
    }

    private static void searchCategory() {
        System.out.println("Pick a category:");
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            System.out.println((i + 1) + ". " + cats[i]);
        }
        int choice = readInt();
        Category cat = (choice >= 1 && choice <= cats.length) ? cats[choice - 1] : Category.OTHER;

        List<Transaction> list = transactionsByCategory.get(cat);
        if (list.isEmpty()) {
            System.out.println("No records for " + cat);
        } else {
            for (Transaction t : list) {
                System.out.println(t);
            }
        }
    }

    private static void searchDateRange() {
        try {
            System.out.print("Start date: ");
            LocalDate start = LocalDate.parse(in.nextLine().trim());
            System.out.print("End date: ");
            LocalDate end = LocalDate.parse(in.nextLine().trim());

            NavigableMap<LocalDate, List<Transaction>> range = transactionsByDate.subMap(start, true, end, true);
            if (range.isEmpty()) {
                System.out.println("No records in range.");
            } else {
                for (List<Transaction> list : range.values()) {
                    for (Transaction t : list) {
                        System.out.println(t);
                    }
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Bad date input.");
        }
    }

    private static void showMonthlySummary() {
        if (totalsByMonth.isEmpty()) {
            System.out.println("Nothing recorded.");
            return;
        }
        for (Map.Entry<YearMonth, Double> e : totalsByMonth.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    private static void showCategorySummary() {
        for (Map.Entry<Category, Double> e : totalsByCategory.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    private static void deleteById() {
        System.out.print("Enter id to delete: ");
        int id = readInt();
        Transaction t = transactionsById.remove(id);
        if (t == null) {
            System.out.println("Not found.");
            return;
        }
        transactionsByDate.get(t.date).remove(t);
        if (transactionsByDate.get(t.date).isEmpty()) {
            transactionsByDate.remove(t.date);
        }
        transactionsByCategory.get(t.category).remove(t);
        totalsByMonth.put(YearMonth.from(t.date), totalsByMonth.get(YearMonth.from(t.date)) - t.amount);
        totalsByCategory.put(t.category, totalsByCategory.get(t.category) - t.amount);
        System.out.println("Deleted " + id);
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(in.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Enter a number: ");
            }
        }
    }

    private static double readDouble() {
        while (true) {
            try {
                return Double.parseDouble(in.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Enter a decimal: ");
            }
        }
    }
}
