import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.apache.commons.cli.*;

public class main {

    private static final String SAVE = "s";
    private static final String CANCEL = "c";

    public static void main(String args[]) throws ParseException {
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        Scanner sc = new Scanner(System.in);

        //User user = new User("Peter", 10);
        //PersistenceManager pm = PersistenceManager.getInstance();#

        Options options = new Options();
        options.addOption("t", true, "display current time");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse( options, args);

        if(cmd.hasOption("t")) {
            System.out.println("test");
        }
        else {
            // print the date
        }
        System.out.println("Enter username");

        String userName = sc.nextLine();  // Read user input
        System.out.println("Username is: " + userName);  // Output user input
    }

}
