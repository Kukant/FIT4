package Blocks;
import Others.Value;

public class DivBlock extends Block {

    public DivBlock() {
        this.InputValues = new Value[2];
    }

    @Override
    public double Calculate() {
        return this.InputValues[0].val / this.InputValues[1].val;
    }
}






