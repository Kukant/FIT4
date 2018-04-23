package Tests;

import Blocks.*;
import Others.Debugger;
import Others.Value;
import Schemes.Scheme;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.io.*;
import java.util.Arrays;


public class SchemeTests {

    @Before
    public void setUp() {

    }

    @Test
    public void save() {
        Scheme newScheme = new Scheme();
        Block addBlock = new AddBlock();
        Block constBlock1 = new ConstBlock();
        Block constBlock2 = new ConstBlock();
        Block resultBlock = new ResultBlock();

        newScheme.Blocks.addAll(Arrays.asList(addBlock, constBlock1, constBlock2, resultBlock));


        constBlock1.BindOutput(addBlock, 0);
        constBlock2.BindOutput(addBlock, 1);

        addBlock.BindInput(constBlock1, 0);
        addBlock.BindInput(constBlock2, 1);

        try {
            FileOutputStream fileOut = new FileOutputStream("/tmp/employee.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(newScheme);
            out.close();
            fileOut.close();
            Debugger.log("Serialized data is saved in /tmp/employee.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }

    }

    @Test
    public void load() {
        Scheme scheme = null;
        try {
            FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            scheme = (Scheme) in.readObject();
            in.close();
            fileIn.close();
        } catch (IOException i) {
            i.printStackTrace();
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
        }

        Assert.assertNotNull(scheme);
        Assert.assertEquals(1, scheme.Blocks.get(1).MyVal, 0.01);
    }

}