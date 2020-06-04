import java.util.Scanner;

public class main {

    public static void main(String args[]) {
        int ptaid = 0;

        Scanner sc = new Scanner(System.in);

        PersistenceManager pm = PersistenceManager.getInstance();

        System.out.println("Welcome to the the database. You have to following options:");
        System.out.println("start - Will start the transaction");

        while (true) {
            while (true) {
                String input = sc.nextLine();

                if (input.contains("start")) {
                    ptaid = pm.beginTransaction();
                    System.out.println("Your transaction has started. You can now insert data");
                    break;
                } else {
                    System.out.println("Wrong command. Type: start");
                }
            }

            while (true) {
                String[] inputData = new String[0];

                if (inputData != null && inputData.length > 0) {
                    System.out.println("save - To save your data in the database.");
                }

                System.out.println("add [ID] [DATA] - To add data to the database. ID has to be an integer.");

                String input = sc.nextLine();
                inputData = input.split(" ");

                if (inputData.length == 3 && inputData[0].equals("add") && isNumeric(inputData[1])) {
                    if (pm.write(ptaid, Integer.parseInt(inputData[1]), inputData[2])){
                        System.out.println("Data was saved to the buffer. Add more data to the buffer");
                    }
                    System.out.println("The ID you are accessing is currently not available, please wait or take another ID");
                } else if (input.equals("end")) {
                    System.out.println("You have ended the transaction. The data will be now be saved");
                    pm.endTransaction(ptaid);
                    System.out.println("start - Will start a new transaction");
                    break;
                } else {
                    System.out.println("Wrong command");
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
