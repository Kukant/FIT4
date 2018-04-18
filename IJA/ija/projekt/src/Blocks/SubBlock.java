package Blocks;

import Others.Value;

public class SubBlock extends Block {
    public SubBlock() {
        super();
        this.InputValues = new Value[2];
    }

    @Override
    public double Calculate() {
        return this.InputValues[0].val - this.InputValues[1].val;
    }
}
