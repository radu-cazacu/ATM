package atm;

import client.Client;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Math.abs;

public class Atm {
    private final int MAX_TRIES = 3;
    private final int MAX_WITHDRAWABLE_SUM_RON = 30;

    private Client currClient;
    private ArrayList<Integer> funds;
    private double[][] exchangeRates;
    Scanner inputScanner;
    Menu menu;

    public Atm() {
        try {
            inputScanner = new Scanner(System.in);
            menu = new Menu();
            File file = new File(".\\funds.txt");
            Scanner scanner = new Scanner(file);
            funds = new ArrayList<>();
            while(scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] values = line.split(" ");
                funds.add(Integer.parseInt(values[0]));
                funds.add(Integer.parseInt(values[1]));
                funds.add(Integer.parseInt(values[2]));
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
            ArrayList<String> activityLog = new ArrayList<>();

            JsonNode rootNode = mapper.readValue(new File(".\\clients7.txt"), JsonNode.class);
            JsonNode clientInfo = rootNode.get(id);

            if(clientInfo != null) {

                for (JsonNode logs : clientInfo.get("log")) {
                    activityLog.add(logs.asText());
                }

                this.currClient = new Client(
                        id,
                        clientInfo.get("password").asText(),
                        clientInfo.get("ron").asInt(),
                        clientInfo.get("dollar").asInt(),
                        clientInfo.get("euro").asInt(),
                        clientInfo.get("active").asInt(),
                        activityLog
                );
                return true;
            }
            return false;

        } catch (JsonParseException e) {
            e.printStackTrace();
            return false;
        } catch (JsonMappingException e) {
            e.printStackTrace();
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
            if(!file.exists()){
                file.createNewFile();
            }
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
            JsonNode rootNode = mapper.readValue(new File(".\\clients7.txt"), JsonNode.class);
            JsonNode clientInfo = rootNode.get(currClient.getId());

            ((ObjectNode) clientInfo).put("ron", currClient.getFunds().get(0));
            ((ObjectNode) clientInfo).put("dollar", currClient.getFunds().get(1));
            ((ObjectNode) clientInfo).put("euro", currClient.getFunds().get(2));
            ((ObjectNode) clientInfo).put("active", currClient.isActive() ? 1 : 0);
            ((ObjectNode) clientInfo).put("log", mapper.valueToTree(currClient.getActivityLog()));

            ((ObjectNode) rootNode).put(currClient.getId(), clientInfo);

            writer.writeValue(Paths.get(".\\clients7.txt").toFile(), rootNode);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(currClient);
    }


    public void run() {
        switch(isClient()) {
            case 1:
                if(logIn()) {
                    proceedAsClient(getClientOption());
                }
                break;
            case 2:
                proceedAsNotClient();
                break;
            default:
                menu.displayInvalidInput();
        }

        updateFunds();
        updateClient();
        inputScanner.close();
    }

    private int isClient() {
        menu.displayInitialMenu();

        try {
            int option = Integer.parseInt(inputScanner.nextLine());
            return (option > 0 && option < 3) ? option : -1;
        } catch (Exception e) {
            menu.displayInvalidInput();
            return -1;
        }
    }

    private void proceedAsClient(int option) {
        switch(option) {
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
    }

    private void proceedAsNotClient() {
        int option = -1;
        menu.displayOutsideClientMessage();

        try {
            option = Integer.parseInt(inputScanner.nextLine());
            System.out.println();
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        if(option == 1) {
            System.out.print("Enter sum : ");
            String sum = inputScanner.nextLine();
            if(Helper.checkValidSum(sum)) {
                String logUpdate = "Outside client has withdrawn ";
                logUpdate += sum;
                logUpdate += ".\n";
                logOutsideClientActivity(logUpdate);
            } else {
                System.out.println("Invalid sum.");
            }
        }
    }

    private boolean logIn() {
        boolean passCorrect = false;

        System.out.print("Input client code : ");

        while(!initializeClient(inputScanner.nextLine())) {
            System.out.println("Invalid client code.");
            System.out.print("Input client code : ");
        }

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
        menu.displayMainMenu();
        try {
            int option = Integer.parseInt(inputScanner.nextLine());
            System.out.println();
            return (option > 0 && option < 5) ? option : -1;
        } catch(Exception e) {
            menu.displayInvalidInput();
            return -1;
        }
    }

    private void withdrawFunds()  {
        System.out.print("Input amount to withdraw : ");
        String line = inputScanner.nextLine();

        if(Helper.checkValidSum(line)) {
            int sum = Helper.getSum(line);
            int currency = Helper.getCurrency(line);

            if (sum > currClient.getFunds().get(currency)) {
                System.out.println("Insufficient funds.");
            } else if (sum > this.funds.get(currency)) {
                System.out.println("Insufficient bills in ATM.");
            } else if (sum < abs(MAX_WITHDRAWABLE_SUM_RON - currency * 20)) {
                menu.displayMinimumWithdrawal(currency);
            } else if (sum > this.funds.get(currency) / 10) {
                System.out.println("The max sum you can withdraw is" + this.funds.get(currency) / 10);
            } else {
                System.out.println("WITHDRAWING ...");
                currClient.updateFunds(currClient.getFunds().get(currency) - sum, currency);
                this.funds.set(currency, this.funds.get(currency) - sum);
                currClient.updateLog("Withdrawn " + sum + Helper.getCurrencySymbol(currency));
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
    }


    @Override
    public String toString() {
        return "Atm{" +
                "MAX_TRIES=" + MAX_TRIES +
                ", currClient=" + currClient +
                ", funds=" + funds +
                ", inputScanner=" + inputScanner +
                ", menu=" + menu +
                '}';
    }
}
