package atm;

import client.Client;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Atm {
    private final int MAX_TRIES = 3;
    private final Double[] MIN_WITHDRAW_SUM = {30.0, 10.0, 10.0};
    private final Double[] WITHDRAW_LIMITATION_THRESHOLD  = {10000.0, 1000.0, 1000.0};
    private final String ADMIN_ID = "000";

    private Client currClient;
    private List<Double> funds;
    private double[][] exchangeRates;
    private Scanner inputScanner;

    public Atm() {
        try {
            inputScanner = new Scanner(System.in);
            File file = new File(".\\funds.txt");
            Scanner scanner = new Scanner(file);
            funds = new ArrayList<>();
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] values = line.split(" ");
                funds.add(Double.parseDouble(values[0]));
                funds.add(Double.parseDouble(values[1]));
                funds.add(Double.parseDouble(values[2]));
            }
            setExchangeRates();
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Funds file does not exist. ");
        }
    }

    private boolean initializeClient(String id) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> activityLog = new ArrayList<>();

            JsonNode rootNode = mapper.readValue(new File(".\\clients.txt"), JsonNode.class);
            JsonNode clientInfo = rootNode.get(id);

            if(clientInfo != null) {

                for (JsonNode logs : clientInfo.get("log")) {
                    activityLog.add(logs.asText());
                }

                this.currClient = new Client(
                        id,
                        clientInfo.get("password").asText(),
                        clientInfo.get("ron").asDouble(),
                        clientInfo.get("dollar").asDouble(),
                        clientInfo.get("euro").asDouble(),
                        clientInfo.get("active").asInt(),
                        activityLog
                );
                return true;
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setExchangeRates() {
        exchangeRates = new double[3][3];
        try {
            File file = new File(".\\exchangerate.txt");
            Scanner scanner = new Scanner(file);
            int i = 0;

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(" ");
                exchangeRates[i][0] = Double.parseDouble(values[0]);
                exchangeRates[i][1] = Double.parseDouble(values[1]);
                exchangeRates[i][2] = Double.parseDouble(values[2]);
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void updateFunds() {
        try {
            String line = "";
            line += this.funds.get(0);
            line += " ";
            line += this.funds.get(1);
            line += " ";
            line += this.funds.get(2);

            FileWriter writer = new FileWriter(".\\funds.txt");
            writer.write(line);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error occurred. ");
            e.printStackTrace();
        }
    }

    private void logOutsideClientActivity(String update) {
        try {
            File file = new File(".\\log.txt");
            FileWriter fileWriter = new FileWriter(file.getName(), true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(update);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateClient() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

        try {
            JsonNode rootNode = mapper.readValue(new File(".\\clients.txt"), JsonNode.class);
            JsonNode clientInfo = rootNode.get(currClient.getId());

            ((ObjectNode) clientInfo).put("ron", currClient.getFunds().get(0));
            ((ObjectNode) clientInfo).put("dollar", currClient.getFunds().get(1));
            ((ObjectNode) clientInfo).put("euro", currClient.getFunds().get(2));
            ((ObjectNode) clientInfo).put("active", currClient.isActive() ? 1 : 0);
            ((ObjectNode) clientInfo).put("log", mapper.valueToTree(currClient.getActivityLog()));

            ((ObjectNode) rootNode).put(currClient.getId(), clientInfo);

            writer.writeValue(Paths.get(".\\clients.txt").toFile(), rootNode);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(currClient);
    }


    private boolean isAdmin(String id) {
        return id.equals(ADMIN_ID);
    }

    private void proceedAsAdmin() {
        System.out.println("ADMIN MODE");
        String command;

        do {
            System.out.print("*");
            command = inputScanner.nextLine();
            adminExecuteCommand(command);
        } while(!command.equalsIgnoreCase("exit"));
    }

    private void adminExecuteCommand(String command) {
        String commandType;
        String commandArgument;
        if (command.contains("_")) {
            commandType = command.substring(0, command.lastIndexOf("_"));
            commandArgument = command.substring(command.lastIndexOf("_") + 1);
            switch (commandType) {
                case("ADD_MONEY"):
                    adminAddMoney(commandArgument);
                    break;
                case("WITHDRAW_MONEY"):
                    adminWithdrawMoney(commandArgument);
                    break;
                case("TRACE"):
                    adminTrace(commandArgument);
                    break;
                case("UNLOCK"):
                    adminUnlock(commandArgument);
                    break;
                default:
                    break;
            }
        }
    }

    private void adminUnlock(String commandArgument) {
        initializeClient(commandArgument);
        currClient.setActive();
        updateClient();
    }

    private void adminTrace(String commandArgument) {
        initializeClient(commandArgument);
        List<String> logs = currClient.getActivityLog();
        for(String activity : logs) {
            System.out.println(activity);
        }
    }

    private void adminWithdrawMoney(String commandArgument) {
        if(Helper.checkValidSum(commandArgument)) {
            double sum = Helper.getSum(commandArgument);
            int currency = Helper.getCurrency(commandArgument);
            this.funds.set(currency, this.funds.get(currency) - sum);
        }
    }

    private void adminAddMoney(String commandArgument) {
        if(Helper.checkValidSum(commandArgument)) {
            double sum = Helper.getSum(commandArgument);
            int currency = Helper.getCurrency(commandArgument);
            this.funds.set(currency, this.funds.get(currency) + sum);
        }
    }

    private boolean canWithdraw(double amount, int currency) {
        if (amount > this.funds.get(currency)) {
            System.out.println("Insufficient bills in ATM.");
            return false;
        } else if (amount < MIN_WITHDRAW_SUM[currency]) {
            Menu.displayMinimumWithdrawal(currency);
            return false;
        } else if (this.funds.get(currency) < WITHDRAW_LIMITATION_THRESHOLD[currency]
                && amount > this.funds.get(currency) / 10) {
            System.out.println("The max sum you can withdraw  " + this.funds.get(currency) / 10 +
                    Helper.getCurrencySymbol(currency));
            return false;
        } else {
            return true;
        }
    }

    private void proceedAsNotClient(String id) {
        int option = -1;
        Menu.displayOutsideClientMessage();

        try {
            option = Integer.parseInt(inputScanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        if(option == 1) {
            System.out.print("Enter sum : ");
            String sum = inputScanner.nextLine();

            if(Helper.checkValidSum(sum)) {
                double amount = Helper.getSum(sum);
                int currency = Helper.getCurrency(sum);
                if(canWithdraw(amount, currency)) {
                    funds.set(currency, funds.get(currency) - amount + (amount / 100));

                    String logUpdate = "Outside client ";
                    logUpdate += id;
                    logUpdate += " has withdrawn ";
                    logUpdate += sum;
                    logUpdate += ".\n";
                    logOutsideClientActivity(logUpdate);
                }
            } else {
                System.out.println("Invalid sum.");
            }
        }
    }

    private void proceedAsClient(int option) {
        if(currClient.isActive()) {
            switch (option) {
                case 1:
                    currClient.balanceInquiry();
                    break;
                case 2:
                    withdrawFunds();
                    break;
                case 3:
                    depositFunds();
                    break;
                case 4:
                    exchangeFunds();
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } else {
            System.out.println("Account inactive.");
        }

    }

    private boolean logIn() {
        boolean passCorrect = false;
        System.out.println(currClient);
        System.out.print("PIN : ");

        for (int i = 0; i < MAX_TRIES; i++) {
            String guess = inputScanner.nextLine();
            if (currClient.checkPassword(guess)) {
                passCorrect = true;
                break;
            }
            System.out.println("Incorrect PIN. Tries left : " + (MAX_TRIES - 1 - i));
            if (MAX_TRIES - 1 - i > 0) {
                System.out.print("PIN : ");
            }
        }

        if(passCorrect) {
            return true;
        }
        else {
            currClient.updateLog("Entered incorrect PIN number 3 times.");
            System.out.println("Entered incorrect PIN number 3 times.");
            return false;
        }
    }

    private int getClientOption() {
        if(currClient.isActive()) {
            Menu.displayMainMenu();
            try {
                int option = Integer.parseInt(inputScanner.nextLine());
                System.out.println();
                return (option > 0 && option < 5) ? option : -1;
            } catch (Exception e) {
                Menu.displayInvalidInput();
                return -1;
            }
        }
        return -1;
    }

    private void withdrawFunds()  {
        System.out.print("Input amount to withdraw : ");
        String line = inputScanner.nextLine();

        if(Helper.checkValidSum(line)) {
            int amount = Helper.getSum(line);
            int currency = Helper.getCurrency(line);

            if (amount > currClient.getFunds().get(currency)) {
                System.out.println("Insufficient funds.");
            } else if (canWithdraw(amount, currency)) {
                System.out.println("WITHDRAWING ...");
                currClient.updateFunds(currClient.getFunds().get(currency) - amount, currency);
                this.funds.set(currency, this.funds.get(currency) - amount);
                currClient.updateLog("Withdrawn " + amount + Helper.getCurrencySymbol(currency));
            }
        } else {
            System.out.println("Invalid sum.");
        }
    }

    private void depositFunds() {
        System.out.print("Input amount to deposit : ");
        String line = inputScanner.nextLine();

        if(Helper.checkValidSum(line)) {
            int sum = Helper.getSum(line);
            int currency = Helper.getCurrency(line);

            System.out.println("DEPOSITING ...");
            currClient.updateFunds(currClient.getFunds().get(currency) + sum, currency);
            this.funds.set(currency, this.funds.get(currency) + sum);
            currClient.updateLog("Deposited " + sum + Helper.getCurrencySymbol(currency));

        } else {
            System.out.println("Invalid sum.");
        }
    }

    private void exchangeFunds() {
        int exchangeFrom, exchangeTo;
        double amount;

        try{
            System.out.print("Exchange from:\n1.RON \n2.$\n3.€\n>");
            exchangeFrom = Integer.parseInt(inputScanner.nextLine()) - 1;

            System.out.print("To:\n1.RON \n2.$\n3.€\n>");
            exchangeTo = Integer.parseInt(inputScanner.nextLine()) - 1;

            System.out.print("Enter amount:");
            amount = Integer.parseInt(inputScanner.nextLine());

            if(currClient.getFunds().get(exchangeFrom) > amount) {
                currClient.updateFunds(currClient.getFunds().get(exchangeFrom) - amount, exchangeFrom);
                currClient.updateFunds(currClient.getFunds().get(exchangeTo) + (amount * exchangeRates[exchangeFrom][exchangeTo]), exchangeTo);
                currClient.updateLog("Exchanged " + amount + Helper.getCurrencySymbol(exchangeFrom) + " to " +
                        (amount * exchangeRates[exchangeFrom][exchangeTo]) + Helper.getCurrencySymbol(exchangeTo));
            } else {
                System.out.println("Insufficient funds.");
            }
        } catch(InputMismatchException e) {
            System.out.println("Invalid input.");
        }
    }

    @Override
    public String toString() {
        return "Atm{" +
                "MAX_TRIES=" + MAX_TRIES +
                ", currClient=" + currClient +
                ", funds=" + funds +
                ", inputScanner=" + inputScanner +
                '}';
    }


    public boolean run() {
        System.out.print("Input client code : ");
        String id = inputScanner.nextLine();
        if(id.equals("exit")){
            inputScanner.close();
            return false;
        } else {
            if (initializeClient(id)) {
                if (logIn()) {
                    proceedAsClient(getClientOption());
                    updateClient();
                }
            } else if (isAdmin(id)) {
                proceedAsAdmin();
            } else {
                proceedAsNotClient(id);
            }

            updateFunds();
            return true;
        }
    }

}
