package Blocks;

import Others.Debugger;
import Others.Value;
import com.sun.org.apache.xml.internal.security.Init;

public class ConstBlock extends Block {
    public ConstBlock() {
        super();
        this.InputValues = new Value[]{new Value(0)};
        this.InputValues[0].val = 1;
        this.InputValues[0].defined = true;
    }

    public double Calculate(){
        return InputValues[0].val;
    }

    public void setConstVal(double val) {
        this.InputValues[0].val = val;
    }

}
