package Schemes;

import Blocks.Block;
import java.util.ArrayList;

public class Scheme implements java.io.Serializable{
    public ArrayList<Block> Blocks;

    public Scheme(){
        Blocks = new ArrayList<>();
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
}
