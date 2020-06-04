import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PersistenceManager {
    // static variable single_instance of type Singleton
    private static PersistenceManager single_instance = null;

    // variable of type String
    public Hashtable<Integer, String> buffer = new Hashtable<Integer, String> ();
    public Hashtable<Integer, Set<Integer>> activeTrans = new Hashtable<Integer, Set<Integer>> ();
    public int ptaid = 0;
    public int ptLSN = 0;

    // private constructor restricted to this class itself
    private PersistenceManager()
    {

    }

    // static method to create instance of Singleton class
    public static PersistenceManager getInstance()
    {
        if (single_instance == null)
            single_instance = new PersistenceManager();

        return single_instance;
    }

    public int beginTransaction(){
        ptaid ++;
        activeTrans.put(ptaid, new HashSet<Integer>());
        return ptaid;
    }

    public int getLSN(){
        ptLSN ++;
        return ptLSN;
    }

    public boolean endTransaction(int taid){
        Set<Integer> modifiedPages = activeTrans.get(taid);

        Iterator itr = modifiedPages.iterator();

        //make all modified pages persistent
        while(itr.hasNext()){
            int pageid = (int) itr.next();
            String data = buffer.get(pageid);
            writeToFile(taid, pageid, data);

            //remove from buffer
            buffer.remove(pageid);

        }

        //state transaction as completed
        activeTrans.remove(taid);
        return false;
    }


    public boolean write (int taid, int pageid, String data){
        int LSN = getLSN();
        Hashtable<Integer, String> tcontent;

        //check if buffer contains transaction information
        if(buffer.contains(pageid)){
            //page modified, no change possible
            return false;
        }
        else {
            //if not: create new Hashtable belonging to specific transaction

            //add userdata to buffer
            buffer.put(pageid, data);

            //add modified page to transactionid
            Set<Integer> modifiedPages = activeTrans.get(pageid);
            modifiedPages.add(LSN);

            //do logentry
            writeLogTran(LSN, taid, pageid, data);
            return true;
        }
    }


    public boolean writeToFile(int taid, int pageid, String data){

        return false;
    }


    public boolean writeLogTran(int LSN, int taid, int pageid, String data ) throws IOException {

        File logFile = new File("logFile.txt");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        FileWriter logWriter = new FileWriter(logFile);
        String logEntry = Integer.toString(LSN) + ", " + Integer.toString(taid) +
                ", " +  Integer.toString(pageid) + ", " + data;
        logWriter.write(logEntry);


        //new line
        logWriter.write(System.getProperty("line.separator"));

        //close Writer
        logWriter.close();

        return true;
    }

    public boolean writeLogEOT(int LSN, int taid) throws IOException {

        File logFile = new File("logFile.txt");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }

        FileWriter logWriter = new FileWriter(logFile);
        String logEntry = Integer.toString(LSN) + ", " + Integer.toString(taid) +
                ", EOT" ;
        logWriter.write(logEntry);


        //new line
        logWriter.write(System.getProperty("line.separator"));

        //close Writer
        logWriter.close();

        return true;
    }


}
