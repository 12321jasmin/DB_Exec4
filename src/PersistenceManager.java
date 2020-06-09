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

        if(buffer.size() >= 5) {
            writeBuffer(true);
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
        logWriter.append("\n");

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
        logWriter.append("\n");

        //close Writer
        logWriter.close();

        return true;
    }


    public boolean recover(){
        buffer = new Hashtable<Integer, String> (); //pageid zu userdata
        activeTrans = new Hashtable<Integer, Hashtable<Integer, Integer>> (); // transactionid, pageid LSN

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
        String[] currentLogEntry;
        String eot = " EOT";
        String eotTest;
        while(itr.hasNext()){
            currentLogEntry = (String[]) itr.next();
            if(currentLogEntry.length > 1){
                eotTest = currentLogEntry[2];
                if(currentLogEntry[2].equals("EOT")){
                    activeTrans.put(Integer.valueOf(currentLogEntry[1]), new Hashtable<Integer,Integer>());
                    System.out.println("Successful Transaction: " + currentLogEntry[1]);
                }
            }
        }

        //redo process
        itr = logEntries.listIterator();
        Integer logpageid;
        Integer logLSN;
        Integer logtaid;
        String logdata;
        Integer pageLSN;
        String pagedata;
        ArrayList<String[]> changedEntries = new ArrayList<String[]>();
        while(itr.hasNext()){
            currentLogEntry = (String[]) itr.next();
            if(currentLogEntry.length > 3){
                logLSN = Integer.parseInt(currentLogEntry[0].replace("\n", ""));
                logtaid = Integer.valueOf(currentLogEntry[1].replace("\n", ""));
                logpageid = Integer.valueOf(currentLogEntry[2].replace("\n", ""));
                logdata = currentLogEntry[3].replace("\n", "");
                System.out.println("Compares "+ Integer.toString(logLSN)+ " with " + getPageLSN(logpageid));
                pageLSN = getPageLSN(logpageid);
                if(activeTrans.contains(logtaid)){
                    if(pageLSN < logLSN){
                        System.out.println("Update page necessary.");
                        updateBuffer(logtaid, logpageid, logdata);
                    } else if(pageLSN == logLSN){
                        pagedata = getPageData(logpageid);
                        if(!logdata.equals(pagedata)){
                            System.out.println("Update data necessary.");
                            updateBuffer(logtaid, logpageid, logdata);
                        }
                    }
                }
            }
        }


        //make persistent entries
        writeBuffer(false);


        return true;
    }

    public boolean updateBuffer(Integer taid, Integer pageid, String data){
        //add userdata to buffer
        buffer.put(pageid, data);

        //add modified page to transactionid
        Hashtable<Integer, Integer> modifiedPages = activeTrans.get(taid);
        modifiedPages.put(pageid, 0);
        return true;
    }

    public boolean writeBuffer(Boolean b){
        //get modified pages
        Set<Integer> activeTs = activeTrans.keySet();
        Iterator itr = activeTs.iterator();
        Iterator itr2;

        Hashtable<Integer, Integer> modifiedEntries;
        Set<Integer> modifiedPages;

        while(itr.hasNext()){

            modifiedEntries = activeTrans.get(itr.next());
            modifiedPages = modifiedEntries.keySet();
            itr2 = modifiedPages.iterator();

            //make all modified pages persistent
            while(itr2.hasNext()){
                int pageid = (int) itr.next();
                String data = buffer.get(pageid);
                try {
                    if(b){
                        writeToFile(getLSN(), pageid, data);
                    } else
                    {
                        writeToFile(modifiedEntries.get(pageid), pageid, data);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

                //remove from buffer
                buffer.remove(pageid);

            }

        }

        return true;

    }



    public Integer random(){
        return hashCode();
    }

    private Integer getPageLSN(Integer pageid){
        File pageFile = new File("page_" + Integer.toString(pageid) + ".txt");
        if (!pageFile.exists()) {
            return 0;
        }
        Scanner pageScanner = null;
        try {
            pageScanner = new Scanner(pageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pageScanner.useDelimiter(",");
        return pageScanner.nextInt();
    }

    private String getPageData(Integer pageid){        File pageFile = new File("page_" + Integer.toString(pageid) + ".txt");
        if (!pageFile.exists()) {
            return "";
        }
        Scanner pageScanner = null;
        try {
            pageScanner = new Scanner(pageFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pageScanner.useDelimiter(",");
        pageScanner.next();
        pageScanner.next();

        return pageScanner.next();

    }


}
