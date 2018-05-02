package Blocks;

import java.util.ArrayList;

import Enviroment.DraggableNode;
import Others.*;

public class Block implements java.io.Serializable{

    public ArrayList<Output> Outputs;
    public Block[] Inputs;
    public Value[] InputValues;
    public Value MyVal;
    public double parentPosX;
    public double parentPosY;
    public DraggableNode parent;

    /**
     * Initialize new empty block
     */
    public Block() {
        this.Outputs = new ArrayList<>();
        MyVal = new Value(0);
    }

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
            Debugger.log("Trying to override already connected input.");
            return 1;
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
        //Debugger.log("Block was not unbinded, as it is not in the Outputs list");
    }

    /**
     * Manager will call this when Block will be destroyed.
     * @param Index
     */
    public void UnbindInput(int Index){

        this.Inputs[Index] = null;


        return;
    }

    /**
     * This method will be overriden.
     * @return
     */
    public double Calculate() {
        return 0;
    }

    /**
     * Manager will call this function on every Block until all blocks have a value.
     */
    public int SendVal(){
        if (this.MyVal.defined) {
            return 1;
        }

        boolean AllDefined = true;
        for(Value v: this.InputValues) {
            if (v == null || !v.defined) {
                AllDefined = false;
                break;
            }
        }


        if (AllDefined) {
            Debugger.log(this.getClass().getSimpleName() + ": My value is: " + Calculate() + " Outpus num: " + this.Outputs.size());
            this.MyVal.val = Calculate();
            this.MyVal.defined = true;
            for (Output output : this.Outputs ) {
                output.block.InputValues[output.Index] = new Value(this.MyVal.val, true);
            }
            return 0;
        } else {
            return 1;
        }

    }
}
