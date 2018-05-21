package Blocks;

import Others.Debugger;
import Others.Value;

public class ConstBlock extends Block {
    /**
     * Initialize new block input
     */
    public ConstBlock() {
        super();
        this.InputValues = new Value[]{new Value(0)};
        this.InputValues[0].val = 1;
        this.InputValues[0].defined = true;
    }

    /**
     *
     * @return value of this block
     */
    public double Calculate(){
        return InputValues[0].val;
    }

    /**
     * Sets value of this constant block
     * @param val value which will be set
     */
    public void setConstVal(double val) {
        this.InputValues[0].val = val;
    }

}
