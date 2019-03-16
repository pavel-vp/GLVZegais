package com.glvz.egais.ui.doc.move;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.BaseRecStatus;
import com.glvz.egais.model.move.MoveRec;
import com.glvz.egais.model.move.MoveRecContent;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.move.MoveRecContentHolder;
import com.glvz.egais.ui.doc.ActBaseDocRec;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

public class ActMoveRecContent extends Activity implements BarcodeReader.BarcodeListener {

    private final static int STATE_SCAN_MARK = 1;
    private final static int STATE_SCAN_EAN = 2;

    MoveRecContentHolder moveRecContentHolder;
    TextView tvAction;

    Button btnBack;
    Button btnNext;

    MoveRec moveRec;
    MoveRecContent moveRecContent;
    int currentState = STATE_SCAN_MARK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomereccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String docId = extras.getString(ActBaseDocRec.REC_DOCID);
        moveRec = DaoMem.getDaoMem().getMapMoveRec().get(docId);

        String position = extras.getString(ActBaseDocRec.RECCONTENT_POSITION);
        moveRecContent = (MoveRecContent) DaoMem.getDaoMem().getRecContentByPosition(moveRec, position);

        String barcode = extras.getString(ActBaseDocRec.RECCONTENT_LASTMARK);
        int addQty = extras.getInt(ActBaseDocRec.RECCONTENT_ADDQTY);

        //prepareActWithData(wbRegId, irc, addQty, barcode);
        updateData();

    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.setCurrentListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.setCurrentListener(null);
    }

    private void setResources() {
        View container = findViewById(R.id.inclRecMoveContent);
        moveRecContentHolder = new MoveRecContentHolder(container);


        tvAction = (TextView) findViewById(R.id.tvAction);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Возврат к форме «Карточка задания на перемещение».
                ActMoveRecContent.this.finish();
            }
        });
        btnNext = (Button) findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Если «Количество факт» = 0, то запросить подтверждение
                if (moveRecContent.getQtyAccepted() == null || moveRecContent.getQtyAccepted() == 0) {
                    MessageUtils.ShowModalAndConfirm(ActMoveRecContent.this, "Внимание!", "«Количество факт не установлено. Если товар для перемещения отсутствует — нажмите Да. Для возврата к вводу количества — нажмите Нет.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    proceedNext();
                                }
                            });

                } else {
                    proceedNext();
                }
            }
        });
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.ShowModalAndConfirm(ActMoveRecContent.this, "Внимание!", "Подтвердите действие: у текущей позиции фактическое количество и все сканированные марки будут удалены",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //  «Количество факт» = 0; в позиции удалить все марки; статус задания = «в работе».
                                moveRecContent.setQtyAccepted(null);
                                moveRecContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
                                moveRecContent.getBaseRecContentMarkList().clear();
                                moveRec.setStatus(BaseRecStatus.INPROGRESS);
                                DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
                                updateData();
                            }
                        });

            }
        });

    }

    private void proceedNext() {
        //  Текущую позицию отметить как выполненную
        moveRecContent.setStatus(BaseRecContentStatus.DONE);
        DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
        // искать следующую не выполненную товарную позицию задания
        MoveRecContent nextRecContent = (MoveRecContent) moveRec.tryGetNextRecContent();
        if (nextRecContent != null) {
            // если найдена: обновить данные текущей формы найденной позицией
            this.moveRecContent = nextRecContent;
            updateData();
        } else {
            // если не найдена: статус задания = Выполнено, перейти к форме «Карточка задания на расход»
            moveRec.setStatus(BaseRecStatus.DONE);
            DaoMem.getDaoMem().writeLocalDataBaseRec(moveRec);
            this.finish();
        }
    }

    private void updateData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Текст надписи зависит от текущего статуса
                switch (currentState) {
                    case STATE_SCAN_MARK:
                        tvAction.setText("Сканируйте марку");
                        break;
                    case STATE_SCAN_EAN:
                        tvAction.setText("Сканируйте ШК товара");
                        break;
                }
                moveRecContentHolder.setItem(moveRecContent, 0, DocContentArrayAdapter.RECCONTENT_MODE);

            }
        });
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
