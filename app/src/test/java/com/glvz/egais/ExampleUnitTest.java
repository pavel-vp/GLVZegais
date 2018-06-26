package com.glvz.egais;

import com.glvz.egais.dao.Dictionary;
import com.glvz.egais.dao.DictionaryMem;
import com.glvz.egais.dao.Document;
import com.glvz.egais.dao.DocumentMem;
import com.glvz.egais.integration.Integration;
import com.glvz.egais.integration.IntegrationSDCard;
import com.glvz.egais.integration.model.*;
import com.glvz.egais.utils.BarcodeObject;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    Integration integrationFile;
    Dictionary dictionary;
    Document document;

    @Before
    public void setup() {
        integrationFile = new IntegrationSDCard("/home/pasha/soft/mobile-me/Android/GLVZegais/GLVZ");
        List<ShopIn> listS = integrationFile.loadShops();
        List<PostIn> listP = integrationFile.loadPosts();
        List<NomenIn> listN = integrationFile.loadNomen();
        dictionary = new DictionaryMem(listS, listP, listN);
        List<IncomeIn> listIncomeIn = integrationFile.loadIncome("00-000083") ;
        document = new DocumentMem(listIncomeIn);
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void searchNomenByBarcode() {
        NomenIn nomen = dictionary.findNomenByBarcode("4603040010655");

        System.out.println(nomen);
    }

    @Test
    public void searchPostById() {
        PostIn post = dictionary.findPostById("010000004469");

        System.out.println(post);
    }

    @Test
    public void searchIncomeById() {
        IncomeIn incomeIn = document.findIncomeInById("TTN-0105456068");

        System.out.println(incomeIn);
    }

    @Test
    public void searchPositionIncomeByMarkDM() {
        IncomeContentIn incomeContentIn = document.findIncomeContentByMarkDM("СЕ000009806", "MARK02-01");

        System.out.println(incomeContentIn);
    }


    @Test
    public void pdf417_toAlcode() {
        String pdf = "20N00001CGUMZYCB99J1NKN31105001000056NQQMS5VP4HTF5SB46ZSQQJD8BNJP891";
        String alcode = BarcodeObject.extractAlcode(pdf);
        System.out.println(alcode);
    }


}