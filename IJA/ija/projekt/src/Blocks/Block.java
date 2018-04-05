package Blocks;

import java.util.ArrayList;
import Others.Output;

abstract public class Block {

    ArrayList<Output> Outputs;
    ArrayList<Block> Inputs;

    public Block(){

    }

    public int BindOutput(Block b, int index){
        return 0;
    }

    public int BindInput(Block b){
        return 0;
    }

    public int UnbindOutput(Block b, int index){
        return 0;
    }

    public int UnbindInput(Block b){

        return 0;
    }

    public int SendVal(){
        return 0;
    }


}
