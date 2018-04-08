package Blocks;

import Others.Value;

public class RootBlock extends Block {
    public RootBlock() {
        this.InputValues = new Value[2];
    }

    @Override
    public double Calculate() {
        return Math.sqrt(this.InputValues[0].val);
    }
}
