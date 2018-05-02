package Blocks;

import Others.Value;
import java.lang.Math;

public class PowBlock extends Block {
    /**
     * Initialize new block input sockets
     */
    public PowBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[]{new Value(0), new Value(0)};
    }

    /**
     * Calculate first input to the second input power
     * @return calculated value
     */
    @Override
    public double Calculate() {
        return Math.pow(this.InputValues[0].val, this.InputValues[1].val);
    }
}
