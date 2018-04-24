package Blocks;
import Others.Value;

public class DivBlock extends Block {

    public DivBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[]{new Value(0), new Value(0)};
    }

    @Override
    public double Calculate() {
        return this.InputValues[0].val / this.InputValues[1].val;
    }
}






