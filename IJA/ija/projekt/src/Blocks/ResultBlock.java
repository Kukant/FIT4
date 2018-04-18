package Blocks;

import Others.Value;

public class ResultBlock extends Block  {
    public ResultBlock() {
        super();
        this.Inputs = new Block[1];
        this.InputValues = new Value[1];
    }

    @Override
    public void SendVal() {
        if (this.InputValues[0].defined) {
            this.MyVal = InputValues[0].val;
        }
    }
}
