import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
        //add to activetrans
        return ptaid;
    }

    public int getLSN(){
        ptLSN ++;
        return ptLSN;
    }

    public boolean endTransaction(int taid){
        Set<Integer> changedPages = activeTrans.get(taid);

        //get all keys (pageid) of the buffer belonging to the transaction ID


        return false;
    }


    public boolean write (int taid, int pageid, String data){
        Hashtable<Integer, String> tcontent;

        //check if buffer contains transaction information
        if(buffer.contains(taid)){
            //tcontent = buffer.get(taid);
        }
        else {
            //if not: create new Hashtable belonging to specific transaction
            tcontent= new Hashtable<Integer, String>();
            //buffer.put(taid, tcontent);
        }
        //tcontent.put(pageid, data);
        return false;
    }


    public boolean writeToFile(int taid, int pageid, String data){

        return false;
    }


    public boolean writeLogTran(int LSN, int taid, int pageid, String data ){
        return false;
    }

    public boolean writeLogEOT(int LSN, int taid, int pageid){
        return false;
    }

}
