import java.util.Hashtable;

public class PersistenceManager {
    // static variable single_instance of type Singleton
    private static PersistenceManager single_instance = null;

    // variable of type String
    public Hashtable buffer;
    public int ptaid = 0;

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

    public boolean commit(int taid){
        return false;
    }

    public boolean write (int taid, int pageid, String data){
        return false;
    }

}
