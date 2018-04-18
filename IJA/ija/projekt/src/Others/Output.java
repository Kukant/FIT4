package Others;

import Blocks.Block;

public class Output implements java.io.Serializable{

    public Output(int Index, Block b) {
        this.Index = Index;
        this.block = b;
    }

    public int Index;
    public Block block;
}
