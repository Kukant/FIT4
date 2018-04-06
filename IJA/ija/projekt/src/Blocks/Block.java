package Blocks;

import java.util.ArrayList;
import Others.*;

abstract public class Block {

    private ArrayList<Output> Outputs;
    private Block[] Inputs;
    private Value[] InputValues;

    /**
     * Manager will call this when new connection will be established.
     * @param b
     * @param Index
     * @return
     */
    public int BindOutput(Block b, int Index){
        for (Output o: this.Outputs) {
            if (o.block == b && o.Index == Index) {
                Debugger.log("Trying to override already binded output.");
                return 1;
            }
        }
        this.Outputs.add(new Output(Index, b));
        return 0;
    }

    /**
     * Manager will call this when new connection will be established.
     * @param b
     * @param Index
     * @return
     */
    public int BindInput(Block b, int Index){
        if (this.Inputs[Index] != null) {
            Debugger.log("Trying to override old input.");
        }

        this.Inputs[Index] = b;
        return 0;
    }

    /**
     * Manager will call this when connection will be deleted.
     * @param b
     * @param Index
     */
    public void UnbindOutput(Block b, int Index){
        for (Output o : this.Outputs) {
            if (o.block == b && o.Index == Index) {
                this.Outputs.remove(o);
                return;
            }
        }
        Debugger.log("Block was not unbinded, as it is not in the Outputs list");
    }

    /**
     * Manager will call this when Block will be destroyed.
     * @param b
     * @param Index
     * @return
     */
    public int UnbindInput(Block b, int Index){
        this.Inputs[Index].UnbindOutput(b, Index);
        this.Inputs[Index] = null;

        return 0;
    }

    /**
     * This method will be overriden.
     * @return
     */
    private double Calculate() {
        return 0;
    }

    public void SendVal(){
        boolean AllDefined = true;
        for(Value v: this.InputValues) {
            if (!v.defined) {
                AllDefined = false;
                break;
            }
        }

        if (AllDefined) {
            double myVal = Calculate();
            for (Output output : this.Outputs ) {
                output.block.InputValues[output.Index] = new Value(myVal, true);
            }
        }
    }
}
