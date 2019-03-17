package com.glvz.egais.integration.sdcard;

import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.income.IncomeRec;

import java.io.File;
import java.util.List;

/**
 * Created by pasha on 07.06.18.
 */
public interface Integration {

    List<ShopIn> loadShops();

    List<PostIn> loadPosts();

    List<NomenIn> loadNomen();

    List<UserIn> loadUsers();

    void initDirectories(String shopId);

    List<IncomeIn> loadIncome(String shopId);
    List<MoveIn> loadMove(String shopId);

    void writeBaseRec(String shopId, BaseRec rec);

    File loadNewApk();

    List<IncomeIn> clearOldData(int numDaysOld);

    List<AlcCodeIn> loadAlcCode();

    List<MarkIn> loadMark();

    SetupFtp loadSetupFtp();
}