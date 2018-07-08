package com.glvz.egais.service;

import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.ShopIn;

public class ShopService {

    private static ShopService shopService;

    public synchronized static ShopService getInstance() {
        if (shopService == null) {
            shopService = new ShopService();
        }
        return shopService;
    }


    private ShopIn shopIn;

    private ShopService() {
    }

    public ShopIn getShopIn() {
        return shopIn;
    }

    public String getShopInName() {
        return shopIn == null ? "Не выбран" : shopIn.getName();
    }

    public void setShopIn(ShopIn shopIn) {
        this.shopIn = shopIn;
        DaoMem.getDaoMem().initDocuments(shopIn.getId());
    }



}
