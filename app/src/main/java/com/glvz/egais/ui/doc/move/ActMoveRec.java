package com.glvz.egais.ui.doc.move;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.service.DocArrayAdapter;
import com.glvz.egais.service.move.MoveContentArrayAdapter;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.MessageUtils;

import java.util.Collection;

public class ActMoveRec extends ActBaseDocRec {

    private MoveRec moveRec;
    private Button btnAction;

    @Override
    protected void initRec() {
        Bundle extras = getIntent().getExtras();
        String key = extras.getString(ActBaseDocRec.REC_DOCID);
        this.moveRec = DaoMem.getDaoMem().getMapMoveRec().get(key);
    }

    @Override
    protected void setResources() {
        setContentView(R.layout.activity_moverec);

        View container = findViewById(R.id.inclRecMove);
        docRecHolder = new DocArrayAdapter.DocRecHolder(container);

        lvContent = (ListView) findViewById(R.id.lvContent);

        lvContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                pickRec(ActMoveRec.this, moveRec.getDocId(), list.get(position), 0, null, false, false);
            }
        });
        adapter = new MoveContentArrayAdapter(this, R.layout.rec_move_position, list);
        lvContent.setAdapter(adapter);

        btnAction = (Button)findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (moveRec.getStatus()) {
                    case NEW:
                    case INPROGRESS:
                        proceedNextRow();
                        break;
                    case DONE:
                        exportDoc();
                        break;
                }
            }
        });

    }

    @Override
    protected void updateData() {
        docRecHolder.setItem(moveRec);
        // Достать список позиций по накладной
        Collection<MoveRecContent> newList = DaoMem.getDaoMem().getMoveRecContentList(moveRec.getDocId());
        list.clear();
        list.addAll(newList);
        adapter.notifyDataSetChanged();
        // В зависимости от состояния - вывести текст на кнопке
        btnAction.setEnabled(true);
        switch (moveRec.getStatus()) {
            case NEW: btnAction.setText("Начать");
                break;
            case INPROGRESS: btnAction.setText("Продолжить");
                break;
            case DONE: btnAction.setText("Выгрузить");
                break;
            default:
                btnAction.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_moverec, menu);
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
                                DaoMem.getDaoMem().rejectData(moveRec);
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
        if ( !(moveRec.getStatus() == BaseRecStatus.DONE || DaoMem.getDaoMem().isQtyAcceptedFull(moveRec)) ) {
            MessageUtils.showModalMessage(this, "Внимание!", "Задание еще не выполнено, выгрузка невозможна");
        } else {
            //- собранные данные выгружаются в JSON-файл во внутреннюю память терминала в каталог «GLVZ\Shops\#ShopID#\Out»
            boolean success = DaoMem.getDaoMem().exportData(moveRec);
            if (success) {
                MessageUtils.showToastMessage("Накладная выгружена!");
                updateData();
                //- выполняется проверка подключенного WiFi, при наличии JSON-файл выгружается по FTP с записью в журнал.
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DaoMem.getDaoMem().syncWiFiFtpShopDocs();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                MessageUtils.showModalMessage(this, "Внимание!", "Имеются строки не сопоставленные с номенклатурой 1С, но принятым количеством. Необходимо сопоставить номенклатуру или отказаться от приемки позиции");
            }
        }
    }

    private void proceedNextRow() {
        MoveRecContent nextRecContent = (MoveRecContent) moveRec.tryGetNextRecContent();
      // переход к форме «Товарная позиция задания на перемещение», активируется первая невыполненная строка задания.
        if (nextRecContent != null) {
            moveRec.setStatus(BaseRecStatus.INPROGRESS);
            DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
            pickRec(this, moveRec.getDocId(), nextRecContent, 0, null, false, false);
        }
    }

    @Override
    protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan) {
        // Открыть форму строки
    }


}
