package Others;

import com.sun.javafx.geom.Vec2d;

public class Value implements java.io.Serializable {
    public Value(double val, boolean defined) {
        this.val = val;
        this.defined = defined;
    }

    public Value(double val) {
        this.val = val;
        this.defined = false;
    }

    public double val;
    public boolean defined;
}
