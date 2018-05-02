package Schemes;

import Blocks.Block;
import Blocks.ConstBlock;
import Blocks.ResultBlock;
import Others.Debugger;
import Others.Output;

import java.util.ArrayList;
import java.util.Arrays;

public class Scheme implements java.io.Serializable{
    /**
     * array of block of scheme
     */
    public ArrayList<Block> Blocks;

    /**
     * creates new array of blocks
     */
    public Scheme(){
        Blocks = new ArrayList<>();
    }

    /**
     * Controls if scheme is ready for computation.
     *
     *
     * @return 0 scheme is valid
     * @return 1 unattached input
     * @return 2 unattached output
     * @return 3 scheme contain cycles
     */
    public int ValidateScheme(){
        for (Block b: this.Blocks){



            //kontrola pripojeni vsech outputs
            if(!(b instanceof ConstBlock)) {
                for (Block in : b.Inputs) {
                    if (in == null) {
                        Debugger.log("WARNING: Unassigned Input detected, calculation has been STOPPED");
                        return 1;
                    }
                }

            }

            //kotrola pripojeni aspon jednoho outputu
            if (!(b instanceof ResultBlock)){
                if (b.Outputs.size() < 1){
                    Debugger.log("WARNING: Unassigned Output detected, calculation has been STOPPED");
                    return 2;
                }
            }


            //kontrola nepritomnosti cyklu
            if (b instanceof ConstBlock){
                ArrayList<Block> inPath = new ArrayList<>();
                inPath.add(b);
                if(!(this.CycleDetection(b, inPath))){
                    Debugger.log("WARNING: Scheme contains cycle, calculation has been STOPPED");
                    return 3;
                }
            }

        }

        return 0;
    }

    /**
     * Recursive function which go trough scheme and controls if there are any blocks connected in cycle
     *
     * @param b  first controlled block
     * @param ControlledBlocks all blocks which were all ready controlled
     * @return true, cycle wasn't found
     * @return false, cycle was found
     */
    private boolean CycleDetection(Block b , ArrayList<Block> ControlledBlocks){

        for (Output o : b.Outputs){
            if (ControlledBlocks.contains(o.block)){
                return false;
            }
            else{
                ControlledBlocks.add(o.block);
                boolean returnedRecursion = this.CycleDetection(o.block, ControlledBlocks);
                return returnedRecursion;

            }
        }
        return true;

    }

    /**
     * Finds first not calculated block in scheme and calculates it
     */
    public void CalculateOnce() {

        for (Block b: Blocks) {
            if  (b != null) {
                int ret = b.SendVal();
                if (ret == 0) // some value has been set
                    return;
            }
        }
    }
}
