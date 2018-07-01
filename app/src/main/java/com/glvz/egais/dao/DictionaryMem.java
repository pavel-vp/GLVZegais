package com.glvz.egais.dao;

import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.PostIn;
import com.glvz.egais.integration.model.ShopIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryMem implements Dictionary {

    private List<ShopIn> listShop;
    private List<PostIn> listPost;
    private List<NomenIn> listNomen;

    private Map<String, NomenIn> mapNomen = new HashMap<>();

    public DictionaryMem(List<ShopIn> listS, List<PostIn> listP, List<NomenIn> listN) {
        this.listShop = listS;
        this.listPost = listP;
        this.listNomen = listN;
        // Трансформировать в мапу
        for (NomenIn nomen : listNomen) {
            if (nomen.getBarcode() != null && nomen.getBarcode().length > 0) {
                for (String barcode : nomen.getBarcode()) {
                    mapNomen.put(barcode, nomen);
                }
            }
        }

    }


    @Override
    public NomenIn findNomenByBarcode(String s) {
        return mapNomen.get(s);
    }

    @Override
    public PostIn findPostById(String s) {
        for (PostIn post : listPost) {
            if (s.equals(post.getClientRegID()))
                return post;
        }
        return null;
    }

    @Override
    public NomenIn findNomenById(String id1c) {
        for (NomenIn nomenIn : listNomen) {
            if (nomenIn.getId().equals(id1c)) {
                return nomenIn;
            }
        }
        return null;
    }
}
