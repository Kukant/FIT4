package Blocks;

import Others.Debugger;
import Others.Value;

public class ResultBlock extends Block  {
    public ResultBlock() {
        super();
        this.Inputs = new Block[1];
        this.InputValues = new Value[]{new Value(0)};
    }

    @Override
    public int SendVal() {
        if (this.MyVal.defined)
            return 1;
        if (this.InputValues[0].defined) {
            this.MyVal.val = InputValues[0].val;
            this.MyVal.defined = true;
            Debugger.log(this.getClass().getSimpleName() + ": My value is: " + this.MyVal.val);
            return 0;
        } else {
            return 1;
        }

    }
}
