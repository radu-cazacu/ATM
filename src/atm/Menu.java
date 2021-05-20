package atm;

public class Menu {

    public void displayInitialMenu() {
        System.out.println("Are you a client of this bank?");
        displayAcceptPrompt();
    }

    public void displayAcceptPrompt() {
        System.out.println("1.Yes\t2.No");
        System.out.print("> ");
    }

    public void displayMainMenu() {
        System.out.println("1. Balance inquiry.");
        System.out.println("2. Withdrawal.");
        System.out.println("3. Deposit.");
        System.out.println("4. Exchange. ");
        System.out.print("> ");
    }

    public void displayMinimumWithdrawal(int currency) {
        System.out.print("The minimum amount you can withdraw is ");
        switch (currency) {
            case 0:
                System.out.println("30RON");
                break;
            case 1:
                System.out.println("10$");
                break;
            case 2:
                System.out.println("10€");
                break;
        }
    }

    public void displayOutsideClientMessage() {
        System.out.println("The bank will take 0.1% commission from the next transaction.");
        System.out.println("Do you agree to continue?");
        displayAcceptPrompt();
    }

    public void displayInvalidInput() {
        System.out.println("Invalid input.");
    }
}
