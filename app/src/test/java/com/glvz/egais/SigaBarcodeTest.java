package com.glvz.egais;

import com.glvz.egais.utils.BarcodeObject;
import org.junit.Assert;
import org.junit.Test;

public class SigaBarcodeTest {


    @Test
    public void one_piece_mark_test() {
        String m = "00000046218070p\"01b<,0P9kkTG2";
        String exMark = BarcodeObject.extractSigaMark(m);
        Assert.assertEquals(m.substring(0, 25), exMark);
    }

    @Test
    public void mark_test_2() {
        String m = "010460043993932021wuiGqW;"+'\u001D'+"8005158000"+'\u001D'+"937kgM"+'\u001D'+"24014580490";
        String mr = "010460043993932021wuiGqW;8005158000";
        String exMark = BarcodeObject.extractSigaMark(m).replace("\u001D", "");
        Assert.assertEquals(mr, exMark);
    }

    @Test
    public void mark_test_3() {
        String m = "010460043993932021wuiGqW;"+'\u001D'+"937kgM"+'\u001D'+"24014580490";
        String mr = "010460043993932021wuiGqW;";
        String exMark = BarcodeObject.extractSigaMark(m).replace("\u001D", "");
        Assert.assertEquals(mr, exMark);
    }
}
