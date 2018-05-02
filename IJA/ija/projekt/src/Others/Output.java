package Others;

import Blocks.Block;

public class Output implements java.io.Serializable{
    /**
     * Set output index and attached block
     *
     * @param Index index of input at block 'b' where is this block connected
     * @param b connected block
     */
    public Output(int Index, Block b) {
        this.Index = Index;
        this.block = b;
    }

    public int Index;
    public Block block;
}
