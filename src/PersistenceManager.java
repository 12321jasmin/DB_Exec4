import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PersistenceManager {
    // static variable single_instance of type Singleton
    private static PersistenceManager single_instance = null;

    // variable of type String
    public Hashtable<Integer, Hashtable> buffer = new Hashtable<Integer, Hashtable> ();
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
        return ptaid + 1;
    }

    public int getLSN(){
        int LSN = ptLSN;
        ptLSN ++;
        return LSN;
    }

    public boolean commit(int taid){
        Hashtable<Integer, String> tcontent = buffer.get(taid);

        //get all keys (pageid) of the buffer belonging to the transaction ID
        Set<Integer> keys = tcontent.keySet();
        Iterator itr = keys.iterator();

        int key;
        String data;

        while(itr.hasNext()){

            key = (int) itr.next();
            data = tcontent.get(key);

            makePersistent(taid, key, data);

        }

        return false;
    }


    public boolean makePersistent(int taid, int pageid, String data){

        return false;
    }

    public boolean write (int taid, int pageid, String data){
        Hashtable<Integer, String> tcontent;

        //check if buffer contains transaction information
        if(buffer.contains(taid)){
            tcontent = buffer.get(taid);
        }
        else {
            //if not: create new Hashtable belonging to specific transaction
            tcontent= new Hashtable<Integer, String>();
            buffer.put(taid, tcontent);
        }
        tcontent.put(pageid, data);
        return false;
    }

}
