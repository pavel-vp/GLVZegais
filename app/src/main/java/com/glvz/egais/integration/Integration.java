package com.glvz.egais.integration;

import com.glvz.egais.integration.model.IncomeIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.PostIn;
import com.glvz.egais.integration.model.ShopIn;

import java.util.List;

/**
 * Created by pasha on 07.06.18.
 */
public interface Integration {

    List<ShopIn> loadShops();

    List<PostIn> loadPosts();

    List<NomenIn> loadNomen();

    List<IncomeIn> loadIncome(String shopId);

}
