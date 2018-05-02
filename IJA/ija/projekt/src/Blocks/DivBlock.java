package Blocks;
import Others.Value;

public class DivBlock extends Block {
    /**
     * Initialize new block input sockets
     */
    public DivBlock() {
        super();
        this.Inputs = new Block[2];
        this.InputValues = new Value[]{new Value(0), new Value(0)};
    }

    /**
     * Divide input values of this block
     * @return calculated value
     */
    @Override
    public double Calculate() {
        return this.InputValues[0].val / this.InputValues[1].val;
    }
}






