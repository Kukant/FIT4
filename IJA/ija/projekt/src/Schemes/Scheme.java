package Schemes;

import Blocks.Block;
import Blocks.ConstBlock;
import Blocks.ResultBlock;
import Others.Debugger;
import Others.Output;

import java.util.ArrayList;

public class Scheme implements java.io.Serializable{
    public ArrayList<Block> Blocks;

    public Scheme(){
        Blocks = new ArrayList<>();
    }

    public boolean ValidateScheme(){
        for (Block b: this.Blocks){


            //kontrola pripojeni vsech outputs
            if(!(b instanceof ConstBlock)) {
                for (Block in : b.Inputs) {
                    if (in == null) {
                        Debugger.log("WARNING: Unassigned Input detected, calculation STOPPED");
                        return false;
                    }
                }

            }

            //kotrola pripojeni aspon jednoho outputu
            if (!(b instanceof ResultBlock)){
                if (b.Outputs.size() < 1){
                    Debugger.log("WARNING: Unassigned Output detected, calculation STOPPED");
                    return false;
                }
            }

        }
        
        return true;
    }

    public void CalculateOnce() {
        for (Block b: Blocks) {
            if  (b != null) {
                int ret = b.SendVal();
                if (ret == 0) // some value has been set
                    return;
            }
        }
    }

    public void Serialize(){

    }

    public void Load(String filename){

    }
}
