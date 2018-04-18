package Tests;

import Blocks.*;
import Others.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;


public class AddSubMulTests {

    private Block add;
    private Block sub;
    private Block mul;

    @Before
    public void setUp() {
        add = new AddBlock();
        sub = new SubBlock();
        mul = new MulBlock();
    }

    /**
     * Addition block test.
     */
    @Test
    public void AddTest() {
        add.InputValues[0] = new Value(12, true);
        add.InputValues[1] = new Value(-11, true);
        Assert.assertEquals(1, add.Calculate(), 0);
    }

    /**
     * Sub block test.
     */
    @Test
    public void SubTest() {
        sub.InputValues[0] = new Value(30, true);
        sub.InputValues[1] = new Value(-12, true);
        Assert.assertEquals(42, sub.Calculate(), 0);
    }

    /**
     * Multiplier block test.
     */
    @Test
    public void MulTest() {
        mul.InputValues[0] = new Value(111, true);
        mul.InputValues[1] = new Value(6, true);
        Assert.assertEquals(666, mul.Calculate(), 0);
    }

}