package Blocks;

import Others.Value;

public class AddBlock extends Block {
    public AddBlock() {
        this.InputValues = new Value[2];
    }

    @Override
    public double Calculate() {
        return this.InputValues[0].val + this.InputValues[1].val;
    }
}
