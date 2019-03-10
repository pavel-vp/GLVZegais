package com.glvz.egais.dao;

import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.PostIn;
import com.glvz.egais.integration.model.ShopIn;
import com.glvz.egais.integration.model.UserIn;

import java.util.List;

public interface Dictionary {

    NomenIn findNomenByBarcode(String s);

    NomenIn findNomenByBarcodeAlco(String s);

    PostIn findPostById(String s);

    NomenIn findNomenById(String id1c);

    UserIn findUserByBarcode(String barcodeData);

    List<ShopIn> getShopListByUser(UserIn userIn);
}
