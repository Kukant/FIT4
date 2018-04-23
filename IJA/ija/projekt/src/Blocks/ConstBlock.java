package Blocks;

import Others.Value;
import com.sun.org.apache.xml.internal.security.Init;

public class ConstBlock extends Block {
    public ConstBlock() {
        super();
    }

    public void Calculate(double InitValue){
        this.MyVal = InitValue;
    }

}
