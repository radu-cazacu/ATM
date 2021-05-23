package atm;

public class Helper {

    private final static String[] currencies = {"RON", "$", "€"};

    public static boolean checkValidSum(String sum) {
        try {
            if (sum != null) {
                if (sum.trim().endsWith("RON")) {
                    Integer.parseInt(sum.substring(0, sum.lastIndexOf("RON")));
                    return true;
                } else if (sum.trim().endsWith("$")) {
                    Integer.parseInt(sum.substring(0, sum.lastIndexOf("$")));
                    return true;
                } else if (sum.trim().endsWith("€")) {
                    Integer.parseInt(sum.substring(0, sum.lastIndexOf("€")));
                    return true;
                }
            }
            return false;
        }
        catch(Exception e) {
            return false;
        }
    }
    
    public static int getSum(String sum) {
        if (sum.trim().endsWith("RON")) {
            return Integer.parseInt(sum.substring(0, sum.lastIndexOf("RON")));
        } else if (sum.trim().endsWith("$")) {
            return Integer.parseInt(sum.substring(0, sum.lastIndexOf("$")));
        } else if (sum.trim().endsWith("€")) {
            return Integer.parseInt(sum.substring(0, sum.lastIndexOf("€")));
        }
        return -1;
    }
    
    public static int getCurrency(String sum) {
        if (sum.trim().endsWith("RON")) {
            return 0;
        } else if (sum.trim().endsWith("$")) {
            return 1;
        } else if (sum.trim().endsWith("€")) {
            return 2;
        }
        return -1;
    }

    public static String getCurrencySymbol(int currency) {
        return currencies[currency];
    }

    public static int getCurrencyId(String currency) {
        switch(currency){
            case "RON":
                return 0;
            case "$":
                return 1;
            case "€":
                return 2;
            default:
                return -1;
        }
    }
}
