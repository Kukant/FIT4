package Enviroment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.DataFormat;
import javafx.util.Pair;

/**
 * Associative list holding object information. Different objects use this class and stores different data.
 */
public class DataHolder implements Serializable{

    public static final DataFormat BlockDragged = new DataFormat("MBDrag");

    private static final long UniqueID = -1458406119115196098L;

    public static final DataFormat BlockAdded = new DataFormat("MIDrag");

    private final List<Pair<String, Object>> ListOfStoredData = new ArrayList<Pair<String, Object>>();


    public static final DataFormat ConnectionAdded = new DataFormat("LinkAdd");

    public DataHolder() {
    }

    /**
     * Saves data into DataHolder with specified key.
     * @param k key data
     * @param v data
     */
    public void importData(String k, Object v) {
        ListOfStoredData.add(new Pair<>(k, v));
    }

    /**
     * Finds in list and returns item represented by specified key.
     *
     * @param key key of desired data.
     * @return found object on success, null otherwise
     */
    public <T> T fetchData(String key) {
        for (Pair<String, Object> data: ListOfStoredData) {
            if (data.getKey().equals(key))
                return (T) data.getValue();
        }

        return null;
    }
}