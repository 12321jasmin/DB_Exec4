import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PersistenceManager {
    // static variable single_instance of type Singleton
    private static PersistenceManager single_instance = null;

    // variable of type String
    public Hashtable<Integer, String> buffer = new Hashtable<Integer, String> (); //pageid zu userdata
    public Hashtable<Integer, Hashtable<Integer, Integer>> activeTrans = new Hashtable<Integer, Hashtable<Integer, Integer>> (); // transactionid, pageid LSN
    public int ptaid = 0; //public transaction id
    public int ptLSN = 0; //public log sequence number

    // private constructor restricted to this class itself
    private PersistenceManager()
    {

    }

    // static method to create instance of Singleton class
    public static PersistenceManager getInstance()
    {
        if (single_instance == null){
            single_instance = new PersistenceManager();
            //TODO:  taid und LSN aus logfile auslesen
        }

        return single_instance;
    }

    public int beginTransaction(){
        ptaid ++;
        activeTrans.put(ptaid, new Hashtable<Integer,Integer>());
        return ptaid;
    }

    public int getLSN(){
        ptLSN ++;
        return ptLSN;
    }

    public boolean endTransaction(int taid) {
        int LSN = getLSN();
        try {
            writeLogEOT(LSN, taid);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        //get modified pages
        Hashtable<Integer, Integer> modifiedEntries = activeTrans.get(taid);
        Set<Integer> modifiedPages = modifiedEntries.keySet();
        Iterator itr = modifiedPages.iterator();

        //make all modified pages persistent
        while(itr.hasNext()){
            int pageid = (int) itr.next();
            String data = buffer.get(pageid);
            try {
                writeToFile(modifiedEntries.get(pageid), pageid, data);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            //remove from buffer
            buffer.remove(pageid);

        }

        //state transaction as completed
        activeTrans.remove(taid);
        return true;
    }


    public boolean write (int taid, int pageid, String data){
        int LSN = getLSN();
        Hashtable<Integer, String> tcontent;

        //add userdata to buffer
        buffer.put(pageid, data);

        //add modified page to transactionid
        Hashtable<Integer, Integer> modifiedPages = activeTrans.get(taid);
        modifiedPages.put(pageid, LSN);


        //do logentry
        try {
            writeLogTran(LSN, taid, pageid, data);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Writing Transaction into Log not possible");
            return false;
        }
        return true;


    }


    //writes userdata to pageid
    public boolean writeToFile(int LSN, int pageid, String data) throws IOException {

        //open filewriter
        FileWriter logWriter = new FileWriter("page_" + Integer.toString(pageid) + ".txt");
        String logEntry = Integer.toString(LSN) + "," +  Integer.toString(pageid) + "," + data;
        logWriter.write(logEntry);

        //new line
        logWriter.write(System.getProperty("line.separator"));

        //close Writer
        logWriter.close();



        return false;
    }


    //logentry for transaction
    public boolean writeLogTran(int LSN, int taid, int pageid, String data ) throws IOException {

        //check if file exists
        File logFile = new File("logFile.txt");
        if (!logFile.exists()) {
            System.out.println("New Logfile Created");
            logFile.createNewFile();
        }

        //open filewriter
        FileWriter logWriter = new FileWriter(logFile, true);
        String logEntry = Integer.toString(LSN) + "," + Integer.toString(taid) +
                "," +  Integer.toString(pageid) + "," + data + ";";
        logWriter.write(logEntry);


        //new line
        logWriter.append(System.getProperty("line.separator"));

        //close Writer
        logWriter.close();

        return true;
    }


    //logentry for end of transaction
    public boolean writeLogEOT(int LSN, int taid) throws IOException {

        File logFile = new File("logFile.txt");
        if (!logFile.exists()) {
            System.out.println("new Logfile Created");
            logFile.createNewFile();
        }

        FileWriter logWriter = new FileWriter(logFile, true);
        String logEntry = Integer.toString(LSN) + "," + Integer.toString(taid) +
                ",EOT;" ;
        logWriter.write(logEntry);


        //new line
        logWriter.append(System.getProperty("line.separator"));

        //close Writer
        logWriter.close();

        return true;
    }


    public boolean recover(){
        File logFile = new File("logFile.txt");
        if (!logFile.exists()) {
            return true;
        }

        Scanner logScanner = null;
        try {
            logScanner = new Scanner(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        logScanner.useDelimiter(";");

        ArrayList<String[]> logEntries = new ArrayList<String[]>();
        while(logScanner.hasNext()) {
            logEntries.add(logScanner.next().split(","));
        }


        //determine winners
        ListIterator itr = logEntries.listIterator();
        String[] currentEntry;
        Set<Integer> winner = new HashSet<>();
        String eot = " EOT";
        String eotTest;
        while(itr.hasNext()){
            currentEntry = (String[]) itr.next();
            if(currentEntry.length > 1){
                System.out.println("CurrentEntry "+ currentEntry[0] + currentEntry[1] +  currentEntry[2]);
                System.out.println("Datatype: " + currentEntry[2].getClass());
                System.out.println("Datatype: " + currentEntry[2]);
                eotTest = currentEntry[2];
                if(currentEntry[2].equals("EOT")){

                    winner.add(Integer.valueOf(currentEntry[1]));
                    System.out.println("Successful Transaction: " + currentEntry[1]);
                }
            }
        }

        return true;
    }


}
