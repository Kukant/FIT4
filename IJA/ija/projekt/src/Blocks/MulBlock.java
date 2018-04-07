package Blocks;

import Others.Value;

public class MulBlock extends Block {
    public MulBlock() {
        this.InputValues = new Value[2];
    }

    @Override
    public double Calculate() {
        return this.InputValues[0].val * this.InputValues[1].val;
    }
}
