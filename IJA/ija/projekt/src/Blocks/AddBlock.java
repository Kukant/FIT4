package Blocks;

import Others.Value;

public class AddBlock extends Block {
    public AddBlock() {
        super();
        this.InputValues = new Value[]{new Value(0), new Value(0)};
        this.Inputs = new Block[2];
    }

    @Override
    public double Calculate() {
        return this.InputValues[0].val + this.InputValues[1].val;
    }
}
