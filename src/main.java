import java.util.Scanner;

public class main {

    public static void main(String args[]) {
        String taid;

        Scanner sc = new Scanner(System.in);

        PersistenceManager pm = PersistenceManager.getInstance();

        System.out.println("Welcome to the the database. You have to following options:");
        System.out.println("start - Will start the transaction");

        while (true) {
            while (true) {
                String input = sc.nextLine();

                if (input.contains("start")) {
                    pm.beginTransaction();
                    System.out.println("Your transaction has started. You can now insert data");
                    break;
                } else {
                    System.out.println("Wrong command. Type: start");
                }
            }

            while (true) {
                String[] inputData = new String[0];

                if (inputData[0]!=null) {
                    System.out.println("save - To save your data in the database.");
                }

                System.out.println("add [ID] [DATA] - To add data to the database. ID has to be an integer.");

                String input = sc.nextLine();
                inputData = input.split(" ");

                if (inputData.length == 3 && inputData[0].equals("add") && isNumeric(inputData[1])) {
                    System.out.println("geschafft");
                    input = sc.nextLine();
                    System.out.println("geschafft");
                } else if (input.equals("end")) {
                    System.out.println("You have ended the transaction. The data will be now be saved");
                    //pm.endtransaction()
                }

            }
        }

    }

    public static boolean isNumeric(final String str) {

        // null or empty
        if (str == null || str.length() == 0) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;

    }

}
