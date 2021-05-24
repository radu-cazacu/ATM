package atm;

public class Menu {

    public static void displayAcceptPrompt() {
        System.out.println("1.Yes\t2.No");
        System.out.print("> ");
    }

    public static void displayMainMenu() {
        System.out.println("1. Balance inquiry.");
        System.out.println("2. Withdrawal.");
        System.out.println("3. Deposit.");
        System.out.println("4. Exchange. ");
        System.out.print("> ");
    }

    public static void displayMinimumWithdrawal(int currency) {
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

    public static void displayOutsideClientMessage() {
        System.out.println("The bank will take 1% commission from the next transaction.");
        System.out.println("Do you agree to continue?");
        displayAcceptPrompt();
    }

    public static void displayInvalidInput() {
        System.out.println("Invalid input.");
    }
}
