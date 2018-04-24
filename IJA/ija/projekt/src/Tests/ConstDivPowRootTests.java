package Tests;

import Blocks.*;
import Others.Value;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;


public class ConstDivPowRootTests {

    private ConstBlock Const;
    private Block Div;
    private Block Pow;
    private Block Root;

    @Before
    public void setUp() {
        Const = new ConstBlock();
        Div = new DivBlock();
        Pow = new PowBlock();
        Root = new RootBlock();

    }


    /**
     * Constant block test.
     */
    @Test
    public void ConstTest() {
        Assert.assertEquals(9.9999, Const.MyVal.val, 0);
    }

    /**
     * Division block test.
     */
    @Test
    public void DivTest1() {
        Div.InputValues[0] = new Value(12, true);
        Div.InputValues[1] = new Value(4, true);
        Assert.assertEquals(3, Div.Calculate(), 0);
    }
    @Test
    public void DivTest2() {
        Div.InputValues[0] = new Value(1, true);
        Div.InputValues[1] = new Value(0, true);
        Assert.assertEquals(Double.POSITIVE_INFINITY, Div.Calculate(), 0);
    }
    @Test
    public void DivTest3() {
        Div.InputValues[0] = new Value(0, true);
        Div.InputValues[1] = new Value(0, true);
        Assert.assertEquals(Double.NaN , Div.Calculate(), 0);
    }
    @Test
    public void DivTest4() {
        Div.InputValues[0] = new Value(9, true);
        Div.InputValues[1] = new Value(3, true);
        Assert.assertEquals(3, Div.Calculate(), 0.34);
    }

    /**
     * Pow block test.
     */
    @Test
    public void PowTest1() {
        Pow.InputValues[0] = new Value(2, true);
        Pow.InputValues[1] = new Value(2, true);
        Assert.assertEquals(4, Pow.Calculate(), 0);
    }
    @Test
    public void PowTest2() {
        Pow.InputValues[0] = new Value(2, true);
        Pow.InputValues[1] = new Value(-2, true);
        Assert.assertEquals(0.25, Pow.Calculate(), 0);
    }
    @Test
    public void PowTest3() {
        Pow.InputValues[0] = new Value(2, true);
        Pow.InputValues[1] = new Value(1, true);
        Assert.assertEquals(2, Pow.Calculate(), 0);
    }
    @Test
    public void PowTest4() {
        Pow.InputValues[0] = new Value(1.25, true);
        Pow.InputValues[1] = new Value(2.3, true);
        Assert.assertEquals(1.67, Pow.Calculate(), 0.01);
    }

    /**
     * Root block test.
     */
    @Test
    public void RootTest1() {
        Root.InputValues[0] = new Value(16, true);
        Root.InputValues[1] = new Value(2, true);
        Assert.assertEquals(4, Root.Calculate(), 0);
    }


}