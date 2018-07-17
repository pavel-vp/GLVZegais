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

import java.util.*;

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

    public int findLowerNum(int[] A) {
        Map<Integer, Integer> map = new HashMap<>();
        for (int key : A) {
            Integer value = map.get(key);
            if (value == null) {
                value = 1;
            } else {
                value++;
            }
            map.put(key, value);
        }
        int numCandiesToReturn = A.length / 2;
        // first pass
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue().intValue() > 1) {
                numCandiesToReturn = numCandiesToReturn - (entry.getValue().intValue() - 1);
                entry.setValue(1);
                if (numCandiesToReturn <= 0) {
                    break;
                }
            }
        }
        if (numCandiesToReturn > 0) {
            // second pass
            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                if (entry.getValue().intValue() > 0) {
                    numCandiesToReturn = numCandiesToReturn - 1;
                    entry.setValue(0);
                    if (numCandiesToReturn <= 0) {
                        break;
                    }
                }
            }
        }
        int result = 0;
        // last pass
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            if (entry.getValue().intValue() > 0) {
                result++;
            }
        }
        return result;
    }

    @Test
    public void find_test() {
        int[] inp = new int[] {80, 80, 1000000000, 80, 80, 80, 80, 80, 80, 123456789};
        int res = findLowerNum(inp);
        System.out.println(res);
    }


    public boolean perm(String s1, String s, Set<String> resultArray) {
        // find all permutations
        int n = s.length();
        if (n == 0) {
            resultArray.add(s1);
            // check
            return checkWord(s1);
        } else {
            for (int i = 0; i < n; i++) {
                boolean check = perm(s1 + s.charAt(i), s.substring(0, i) + s.substring(i + 1, n), resultArray);
                if (check) {
                    return true;
                }
            }
        }
        return false;
    }

    public String generateString(String s, int lenght) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i<= lenght; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private boolean checkWord(String s) {
        return !s.contains("aaa") && !s.contains("bbb");
    }

    private String findRightResult(Set<String> array) {
        for (String s : array) {
            if (checkWord(s)) {
                return s;
            }
        }
        return null;
    }

    @Test
    public void permTest() {
        String A = generateString("a", 10);
        String B = generateString("b", 3);
        String result = A + B;
        System.out.println(result);
        Set<String> array = new HashSet<>();
        perm("", result, array);
        System.out.println(array);
        String res = findRightResult(array);
        System.out.println(res);
    }



}