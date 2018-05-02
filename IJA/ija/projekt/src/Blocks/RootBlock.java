package Blocks;

import Others.Value;

public class RootBlock extends Block {
    /**
     * Initialize new block input sockets
     */
    public RootBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[]{new Value(0), new Value(0)};
    }

    /**
     * Calculates input2 root of input1
     * @return calculated value
     */
    @Override
    public double Calculate() {
        return Math.pow(this.InputValues[0].val, 1/this.InputValues[1].val);
    }
}
