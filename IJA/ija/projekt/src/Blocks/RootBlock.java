package Blocks;

import Others.Value;

public class RootBlock extends Block {
    public RootBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[]{new Value(0), new Value(0)};
    }

    @Override
    public double Calculate() {
        return Math.pow(this.InputValues[0].val, 1/this.InputValues[1].val);
    }
}
