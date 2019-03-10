package com.glvz.egais.dao;

import com.glvz.egais.integration.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryMem implements Dictionary {

    private List<UserIn> listUser;
    private List<ShopIn> listShop;
    private List<PostIn> listPost;
    private List<NomenIn> listNomen;
    private List<AlcCodeIn> listAlcCode;
    private List<MarkIn> listMark;

    private Map<String, NomenIn> mapNomen = new HashMap<>();

    public DictionaryMem(List<UserIn> listU, List<ShopIn> listS, List<PostIn> listP, List<NomenIn> listN, List<AlcCodeIn> listA, List<MarkIn> listM) {
        this.listUser = listU;
        this.listShop = listS;
        this.listPost = listP;
        this.listNomen = listN;
        this.listAlcCode = listA;
        this.listMark = listM;
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
    public NomenIn findNomenByBarcodeAlco(String s) {
        NomenIn result = findNomenByBarcode(s);
        if (result != null &&
                (result.getNomenType() == NomenIn.NOMENTYPE_ALCO_MARK || result.getNomenType() == NomenIn.NOMENTYPE_ALCO_NOMARK)) {
            return result;
        }
        return null;
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

    @Override
    public UserIn findUserByBarcode(String barcodeData) {
        for (UserIn userIn : listUser) {
           if (userIn.getId().equals(barcodeData)) {
               return userIn;
           }
        }
        return null;
    }

    @Override
    public List<ShopIn> getShopListByUser(UserIn userIn) {
        List<ShopIn> list = new ArrayList<>();
        for (ShopIn shop : listShop) {
            if (userIn.getUsersPodrs() == null || userIn.getUsersPodrs().length == 0) {
                list.add(shop);
            } else {
                for (String podr : userIn.getUsersPodrs()) {
                    if (podr.equals(shop.getId())) {
                        list.add(shop);
                        break;
                    }
                }
            }
        }
        return list;
    }
}
