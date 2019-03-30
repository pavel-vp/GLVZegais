package com.glvz.egais.ui.doc.writeoff;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.MarkIn;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.writeoff.WriteoffRec;
import com.glvz.egais.model.writeoff.WriteoffRecContent;
import com.glvz.egais.service.writeoff.WriteoffContentArrayAdapter;
import com.glvz.egais.service.writeoff.WriteoffRecHolder;
import com.glvz.egais.ui.ActCommentEdit;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;

import java.util.Collection;

public class ActWriteoffRec extends ActBaseDocRec {

    private final static int STATE_SCAN_MARK = 1;
    private final static int STATE_SCAN_EAN = 2;

    private final static int COMMENT_RETCODE = 1;

    private int currentState = STATE_SCAN_MARK;
    private MarkIn scannedMarkIn = null;

    private WriteoffRec writeoffRec;
    Button btnChangeComment;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.writeoffRec = DaoMem.getDaoMem().getMapWriteoffRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_writeoffrec);

        View container = findViewById(R.id.inclRecWriteoff);
        docRecHolder = new WriteoffRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        btnChangeComment = (Button)findViewById(R.id.btnChangeComment);
        btnChangeComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Открыть форму с редактироанием комментария
                Intent i = new Intent(ActWriteoffRec.this, ActCommentEdit.class);
                i.putExtra(ActCommentEdit.COMMENT_VALUE, writeoffRec.getComment());
                startActivityForResult(i, COMMENT_RETCODE);
            }
        });

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActWriteoffRec.this, writeoffRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        adapter = new WriteoffContentArrayAdapter(this, R.layout.rec_writeoff_position, list);
        lvContent.setAdapter(adapter);

    }

    @Override
    protected void updateData() {
        docRecHolder.setItem(writeoffRec);
        // Достать список позиций по накладной
        Collection<WriteoffRecContent> newList = DaoMem.getDaoMem().getWriteoffRecContentList(writeoffRec.getDocId());
        list.clear();
        list.addAll(newList);
        adapter.notifyDataSetChanged();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent i){
        if(requestCode == COMMENT_RETCODE ){
            if(resultCode==RESULT_OK){
                writeoffRec.setComment(i.getData().toString());
                DaoMem.getDaoMem().writeLocalWriteoffRec(writeoffRec);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_writeoffrec, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();

        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.action_export:
                exportDoc();
                return true;
            case R.id.action_clear:
                // - запрос на подтверждение очистки «Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу» Да/Нет
                //- удаляются данные о фактически собранном количестве и маркам по всему документу
                //- статус задания устанавливается в «новый»
                MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите очистку. Информация о собранном количестве и марках будет удалена по всему документу",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DaoMem.getDaoMem().rejectData(writeoffRec);
                                MessageUtils.showToastMessage("Данные по накладной удалены!");
                                updateData();
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exportDoc() {
        // - проверить, что все строки задания выполнены (или статус задания — завершено). Если проверка не пройдена — модальное сообщение
        //   «Задание еще не выполнено, выгрузка невозможна» и завершение обработки.
        if ( !(writeoffRec.getStatus() == BaseRecStatus.DONE || DaoMem.getDaoMem().isQtyAcceptedFull(writeoffRec)) ) {
            MessageUtils.showModalMessage(this, "Внимание!", "Задание еще не выполнено, выгрузка невозможна");
        } else {
            //- собранные данные выгружаются в JSON-файл во внутреннюю память терминала в каталог «GLVZ\Shops\#ShopID#\Out»
            boolean success = DaoMem.getDaoMem().exportData(writeoffRec);
            if (success) {
                MessageUtils.showToastMessage("Накладная выгружена!");
                updateData();
                syncDoc();
            } else {
                MessageUtils.showModalMessage(this, "Внимание!", "Имеются строки не сопоставленные с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки позиции");
            }
        }
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }


    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // no action
    }
}
