package Others;

import com.sun.javafx.geom.Vec2d;

public class Value implements java.io.Serializable {
    /**
     *
     * @param val holds the value of block
     * @param defined switch value which tells if is value will be defined
     */
    public Value(double val, boolean defined) {
        this.val = val;
        this.defined = defined;
    }

    /**
     *
     * @param val sets the value
     */
    public Value(double val) {
        this.val = val;
        this.defined = false;
    }

    public double val;
    public boolean defined;
}
