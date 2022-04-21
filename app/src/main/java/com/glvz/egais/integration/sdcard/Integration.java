package com.glvz.egais.integration.sdcard;

import com.glvz.egais.integration.model.*;
import com.glvz.egais.integration.model.doc.DocIn;
import com.glvz.egais.integration.model.doc.checkmark.CheckMarkIn;
import com.glvz.egais.integration.model.doc.findmark.FindMarkIn;
import com.glvz.egais.integration.model.doc.income.IncomeIn;
import com.glvz.egais.integration.model.doc.inv.InvIn;
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

    List<CommandIn> loadCommands();

    void initDirectories(String shopId);

    List<IncomeIn> loadIncome(String shopId);
    List<MoveIn> loadMove(String shopId);
    List<CheckMarkIn> loadCheckMark(String shopId);
    List<FindMarkIn> loadFindMark(String shopId);
    List<InvIn> loadInv(String shopId);
    List<File> loadPhotoFiles(String shopId);

    void writeBaseRec(String shopId, BaseRec rec);

    File loadNewApk();

    List<String> clearOldData(int numDaysOld);

    List<AlcCodeIn> loadAlcCode();

    List<MarkIn> loadMark(String shopId);

    SetupFtp loadSetupFtp();

    void deleteFileRec(BaseRec baseRec, String shopId);

    String getPhotoFileName(String skladId, String name);

    String getBasePath();

    void LogWrite(String shopId, String log_text);
}
