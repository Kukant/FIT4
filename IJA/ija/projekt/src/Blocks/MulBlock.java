package Blocks;

import Others.Value;

public class MulBlock extends Block {
    /**
     * Initialize new block input sockets
     */
    public MulBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[]{new Value(0), new Value(0)};
    }

    /**
     * Multiply input values of this block
     * @return calculated value
     */
    @Override
    public double Calculate() {
        return this.InputValues[0].val * this.InputValues[1].val;
    }
}
