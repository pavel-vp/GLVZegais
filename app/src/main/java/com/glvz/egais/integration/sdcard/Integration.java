package com.glvz.egais.integration.sdcard;

import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.move.MoveIn;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.income.IncomeRec;
import com.glvz.egais.model.writeoff.WriteoffRec;

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
    List<WriteoffRec> loadWriteoff(String shopId);

    void writeBaseRec(String shopId, BaseRec rec);

    File loadNewApk();

    List<DocIn> clearOldData(int numDaysOld);

    List<AlcCodeIn> loadAlcCode();

    List<MarkIn> loadMark(String shopId);

    SetupFtp loadSetupFtp();
}
