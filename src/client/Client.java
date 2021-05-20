package client;

import java.util.ArrayList;

public class Client {
    private String id;
    private String password;
    private ArrayList<Integer> funds;
    private boolean active;
    private ArrayList<String> activityLog;


    public Client(String id, String password, Integer ron, Integer dollar, Integer euro, Integer active, ArrayList<String> activityLog) {
        funds = new ArrayList<>();
        this.id = id;
        this.password = password;
        this.funds.add(ron);
        this.funds.add(dollar);
        this.funds.add(euro);
        this.active = active > 0;
        this.activityLog = activityLog;
    }

    public Client() {
    }

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    public ArrayList<String> getActivityLog() {
        return activityLog;
    }

    public ArrayList<Integer> getFunds() {
        return funds;
    }

    public boolean checkPassword(String guess) {
        return guess.equals(this.password);
    }

    public void balanceInquiry() {
        System.out.println(this.funds.get(0) + " RON");
        System.out.println(this.funds.get(1) + " DOLLARS");
        System.out.println(this.funds.get(2) + " EURO");
    }

    public void updateFunds(int updatedValue, int currencyType) {
        this.funds.set(currencyType, updatedValue);
    }

    public void updateLog(String logUpdate) {
        this.activityLog.add(logUpdate);
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", funds=" + funds +
                ", active=" + active +
                ", activityLog=" + activityLog +
                '}';
    }

}
