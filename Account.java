package banking;

public class Account {
    String cardNumber;
    String pin;
    long balance;

    public Account(String ndNumber, String ndPin, long ndBalance) {
        cardNumber = ndNumber;
        pin = ndPin;
        balance = ndBalance;
    }

    public boolean verifyPIN(String pin) {
        return this.pin.equals(pin);
    }

    public long getBalance() {
        return balance;
    }

    public String getCardNumber() { return cardNumber; }
}
