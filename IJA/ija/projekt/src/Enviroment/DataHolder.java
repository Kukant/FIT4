package Enviroment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.DataFormat;
import javafx.util.Pair;

public class DataHolder implements Serializable{

    /**
     *
     */

    public static final DataFormat DragNode = new DataFormat("MBDrag");

    private static final long serialVersionUID = -1458406119115196098L;

    public static final DataFormat AddNode = new DataFormat("MIDrag");

    private final List<Pair<String, Object>> DataList = new ArrayList<Pair<String, Object>>();


    public static final DataFormat AddLink = new DataFormat("LinkAdd");

    public DataHolder() {
    }

    public void importData(String k, Object v) {
        DataList.add(new Pair<>(k, v));
    }

    public <T> T fetchData(String key) {
        for (Pair<String, Object> data: DataList) {
            if (data.getKey().equals(key))
                return (T) data.getValue();
        }

        return null;
    }
}