package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class BankSystem {
    String IIN = "400000";
    //HashMap<String, Account> accounts;
    String database;

    public BankSystem(String database) {
    //    accounts = new HashMap<>();
        this.database = database;
    }

    public void menu() {
        Scanner scanner = new Scanner(System.in);
        int opt;
        do {
            printLogMenu();
            opt = scanner.nextInt();
            System.out.println();

            switch (opt) {
                case 1:
                    // 1. Create an account
                    String newCardNumber = generateCardNumber();
                    String newPIN = generatePIN();
                    //Account newAccount = new Account(newCardNumber, newPIN);
                    //accounts.put(newCardNumber, newAccount);
                    insertCardToDB(newCardNumber, newPIN);

                    System.out.println("Your card has been created");
                    System.out.println("Your card number:");
                    System.out.println(newCardNumber);
                    System.out.println("Your card PIN:");
                    System.out.println(newPIN);
                    System.out.println();
                    break;
                case 2:
                    System.out.println("Enter your card number:");
                    String number = scanner.next();
                    System.out.println("Enter your PIN:");
                    String pin = scanner.next();
                    System.out.println();

                    //Account account = accounts.get(number);
                    Account account = verifyAccessData(number, pin);
                    if (account != null) {
                        System.out.println("You have successfully logged in!");
                        System.out.println();
                        int optUser;

                        do {
                            printUserMenu();
                            optUser = scanner.nextInt();
                            System.out.println();

                            switch (optUser) {
                                case 1:
                                    System.out.println("Balance: " + account.getBalance());
                                    System.out.println();
                                    break;
                                case 2:
                                    System.out.println("Enter income:");
                                    long incomeToAdd = scanner.nextLong();
                                    addIncome(account.getCardNumber(), incomeToAdd);
                                    System.out.println("Income was added!");
                                    System.out.println();
                                    break;
                                case 3:
                                    System.out.println("Enter card number:");
                                    String cardToTransfer = scanner.next();
                                    if (checkCheckSum(cardToTransfer)) {
                                        if (verifyCardExists(number)) {
                                            System.out.println("Enter how much money you want to transfer:");
                                            long moneyToTransfer = scanner.nextLong();
                                            if (moneyToTransfer <= account.getBalance()) {
                                                transferMoney(account.getCardNumber(), cardToTransfer, moneyToTransfer);
                                                System.out.println("Success!");
                                            }
                                            else {
                                                System.out.println("Not enough money!");
                                            }
                                        }
                                        else {
                                            System.out.println("Such a card does not exist.");
                                        }
                                    }
                                    else {
                                        System.out.println("Probably you made a mistake in the card number. Please try again!");
                                    }
                                    System.out.println();
                                    break;
                                case 4:
                                    closeAccount(account.getCardNumber());
                                    System.out.println("The account has been closed!");
                                    break;
                                case 5:
                                    System.out.println("You have successfully logged out!");
                                    System.out.println();
                                    break;
                                case 0:
                                    System.out.println("Bye!");
                                    return;
                                default:
                                    System.out.println("Not valid option");
                                    System.out.println();
                            }
                        } while(optUser != 2 || optUser != 4);
                    }
                    else {
                        System.out.println("Wrong card number or PIN!");
                        System.out.println();
                    }
                    break;
                case 0:
                    System.out.println("Bye!");
                    break;
                default:
                    System.out.println("Not valid option.");
                    System.out.println();
            }

        } while (opt != 0);
    }

    private void closeAccount(String cardNumber) {
        String url = "jdbc:sqlite:" + database;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String query = "DELETE FROM card WHERE number=" + cardNumber;
                statement.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void printLogMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    private void printUserMenu() {
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    private String generateCardNumber() {
        // TODO: Only generate numbers that are not in accounts
        String newNumber = IIN + generateCAN();
        //String newNumber = IIN + "481051382";
        return newNumber + generateCheckSum(newNumber);
    }

    private String generateCAN() {
        Random random = new Random();
        String CAN = "";
        for (int i = 0; i < 9; i++) {
            CAN = CAN + random.nextInt(10);
        }
        return CAN;
    }

    private String generatePIN() {
        Random random = new Random();
        String PIN = "";
        for (int i = 0; i < 4; i++) {
            PIN = PIN + random.nextInt(10);
        }
        return PIN;
    }

    // Generate checksum using Luhn algorithm
    private int generateCheckSum(String number) {
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {
            int currentDigit = Character.getNumericValue(number.charAt(i));
            if ((i + 1) % 2 == 0) {
                sum += currentDigit;
            }
            else {
                currentDigit *= 2;
                sum += currentDigit > 9 ? currentDigit - 9 : currentDigit;
            }
        }

        if (sum % 10 == 0)
            return 0;
        else {
            return Math.abs((sum % 10) - 10);
        }
    }

    private boolean checkCheckSum(String cardNumber) {
        int correctCheckSum = generateCheckSum(cardNumber.substring(0, cardNumber.length() - 1));
        int cardCheckSum = Character.getNumericValue(cardNumber.charAt(cardNumber.length() - 1));
        if (correctCheckSum == cardCheckSum) {
            return true;
        }
        else {
            return false;
        }
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    private void insertCardToDB(String number, String pin) {
        String url = "jdbc:sqlite:" + database;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String query = "INSERT INTO card (number, pin) VALUES (" + number + ", " + pin + ")";
                statement.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Account verifyAccessData(String number, String pin) {
        String url = "jdbc:sqlite:" + database;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String query = "SELECT * FROM card WHERE number=" + number + " AND pin=" + pin;
                ResultSet card = statement.executeQuery(query);
                if (card.next()) {
                    String accountNumber = card.getString("number");
                    String accountPIN = card.getString("pin");
                    long accountBalance = card.getLong("balance");

                    return new Account(accountNumber, accountPIN, accountBalance);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean verifyCardExists(String number) {
        String url = "jdbc:sqlite:" + database;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String query = "SELECT * FROM card WHERE number=" + number;
                ResultSet card = statement.executeQuery(query);
                if (card.next()) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addIncome(String number, long income) {
        String url = "jdbc:sqlite:" + database;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String query = "UPDATE card SET balance=balance+" + income + " WHERE number=" + number;
                statement.executeUpdate(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void transferMoney(String source, String dest, long ammount) {
        String url = "jdbc:sqlite:" + database;

        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                String querySub = "UPDATE card SET balance=balance-" + ammount + " WHERE number=" + source;
                String queryAdd = "UPDATE card SET balance=balance+" + ammount + "WHERE number=" + dest;
                statement.executeUpdate(querySub);
                statement.executeUpdate(queryAdd);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
