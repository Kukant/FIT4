package Blocks;

import Others.Value;
import java.lang.Math;

public class PowBlock extends Block {
    public PowBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[2];
    }

    @Override
    public double Calculate() {
        return Math.pow(this.InputValues[0].val, this.InputValues[1].val);
    }
}
