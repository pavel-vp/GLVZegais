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
import java.io.IOException;
import java.util.List;
import java.util.Set;

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

    Set<String> clearOldData(int numDaysOld);

    List<AlcCodeIn> loadAlcCode();

    List<MarkIn> loadMark(String shopId);

    SetupFtp loadSetupFtp();

    void deleteFileRec(BaseRec baseRec, String shopId);

    String getPhotoFileName(String skladId, String name);

    String getBasePath();

    void LogWrite(String shopId, String log_text);

    void exportDbFile(String shopId, String pathDb) throws IOException;
}
