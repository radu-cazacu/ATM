package client;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private String id;
    private String password;
    private List<Double> funds;
    private boolean active;
    private List<String> activityLog;


    public Client(String id, String password, Double ron, Double dollar, Double euro, Integer active, List<String> activityLog) {
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

    public List<String> getActivityLog() {
        return activityLog;
    }

    public List<Double> getFunds() {
        return funds;
    }

    public boolean checkPassword(String guess) {
        if (guess.equals(new StringBuilder(password).reverse().toString())) {
            this.active = false;
            return true;
        }
        return guess.equals(this.password);
    }

    public void setActive() {
        this.active = true;
    }

    public void balanceInquiry() {
        System.out.println(this.funds.get(0) + " RON");
        System.out.println(this.funds.get(1) + " DOLLARS");
        System.out.println(this.funds.get(2) + " EURO");
    }

    public void updateFunds(double updatedValue, int currencyType) {
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
