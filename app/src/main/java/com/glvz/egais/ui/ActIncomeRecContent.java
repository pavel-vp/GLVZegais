package com.glvz.egais.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.IncomeContentBoxTreeIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.*;
import com.glvz.egais.service.IncomeContentArrayAdapter;
import com.glvz.egais.service.PickBottliingDateCallback;
import com.glvz.egais.service.TransferCallback;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.List;

public class ActIncomeRecContent extends Activity implements BarcodeReader.BarcodeListener, PickBottliingDateCallback, TransferCallback {

    private String lastMark;
    private IncomeRec incomeRec;
    private IncomeRecContent incomeRecContent;
    private IncomeContentArrayAdapter.DocRecContentHolder docRecContentHolder;

    TextView tvAction;
    EditText etQtyAccepted;
    LinearLayout llAccepted;
    Button btnAdd;
    private boolean isBoxScanned = false;
    private boolean isOpenByScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomereccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String wbRegId = extras.getString(ActIncomeRec.INCOMEREC_WBREGID);
        IncomeRec ir = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeRec.INCOMERECCONTENT_POSITION);
        IncomeRecContent irc = DaoMem.getDaoMem().getIncomeRecContentByPosition(ir, Integer.valueOf(position));
        String barcode = extras.getString(ActIncomeRec.INCOMERECCONTENT_LASTMARK);
        int addQty = extras.getInt(ActIncomeRec.INCOMERECCONTENT_ADDQTY);
        this.isBoxScanned = extras.getBoolean(ActIncomeRec.INCOMERECCONTENT_ISBOXSCANNED);
        this.isOpenByScan = extras.getBoolean(ActIncomeRec.INCOMERECCONTENT_ISOPENBYSCAN);

        prepareActWithData(wbRegId, irc, addQty, barcode);


    }

    private void prepareActWithData(String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        this.incomeRecContent = irc;
        this.lastMark = barcode;

        boolean checkMark = addQty > 0 && checkQtyOnLastMark();
        if (checkMark && this.incomeRecContent.getNomenIn() != null && (addQty != 0 )) {
            // Если товар сопоставлен - сохраняем сразу
            proceedAddQtyInternal(addQty);
        }
        updateDisplayData();
    }

    private boolean checkQtyOnLastMark() {
        if (this.lastMark != null) {
            // Проверить: [количество по ТТН] > [Принятое количество]
            if (incomeRecContent.getQtyAccepted() !=null &&
                    incomeRecContent.getIncomeContentIn().getQty().compareTo(incomeRecContent.getQtyAccepted()) <= 0) {
                if (!this.isBoxScanned) {
                    String alcocode = BarcodeObject.extractAlcode(this.lastMark);
                    MessageUtils.showModalMessage(this, "Внимание!","По позиции номер: %d, алкокод: %s, (%s) уже принято полное количество %s. Сканированная бутылка лишняя, принимать нельзя. Верните поставщику!",
                            incomeRecContent.getPosition(),
                            alcocode,
                            incomeRecContent.getIncomeContentIn().getName(),
                            incomeRecContent.getQtyAccepted()
                    );
                }
                this.lastMark = null;
                return false;
            }
        }
        return true;
    }

    // обработчик добавления марки и/или количества
    private void proceedAddQtyInternal(double addQty) {
        // Если в режиме сканирования упаковки
        if (this.isBoxScanned) {
            // посчитать количесвто общее товара по этой упаковке
            IncomeContentBoxTreeIn icb = DaoMem.getDaoMem().findIncomeContentBoxTreeIn(this.incomeRecContent, this.lastMark);

            List<DaoMem.MarkInBox> resList = new ArrayList<>();
            DaoMem.getDaoMem().getAllIncomeRecMarksByBoxBarcode(resList, this.incomeRecContent, icb, 1);
            // пройтись по каждой из них
            for (DaoMem.MarkInBox mb : resList) {
                IncomeRecContentMark ircm = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(this.incomeRec, mb.icm.getMark());
                //Если добавляемой марки еще нет в списке принятых: добавить ее в список принятых, признак сканирования установить в значение уровня вложенности упаковки (см. пред. пункт), принятое количество увеличить на 1 шт.
                if (ircm == null) {
                    // В этот момент ШК товара уже должен быть, можем добавлять марку
                    incomeRecContent.getIncomeRecContentMarkList().add(new IncomeRecContentMark(mb.icm.getMark(), mb.level, this.lastMark));
                }
                //Если добавляемая марка уже есть в списке принятых - нужно только изменить признак сканирования (установить меньшее значение из того что уже стоит по марке и уровня вложенности текущей упаковки). Принятое количество менять не надо.
                if (ircm != null) {
                    int currentLevel = ircm.getMarkScannedAsType();
                    ircm.setMarkScannedAsType(Math.min(currentLevel, mb.level));
                }
            }
        } else {
            // При коробочном сканировании - не добавлять саму коробку
            if (this.lastMark != null) {
                this.incomeRecContent.getIncomeRecContentMarkList().add(new IncomeRecContentMark(this.lastMark, IncomeRecContentMark.MARK_SCANNED_AS_MARK, this.lastMark));
            }
        }
        if (addQty != 0) {
            incomeRecContent.setQtyAccepted(incomeRecContent.getQtyAccepted() == null ? addQty : incomeRecContent.getQtyAccepted() + addQty);
        }
        if (incomeRecContent.getQtyAccepted() == null) {
            incomeRecContent.setStatus(IncomeRecContentStatus.NOT_ENTERED);
        } else {
            if (incomeRecContent.getQtyAccepted().equals(Double.valueOf(0))) {
                incomeRecContent.setStatus(IncomeRecContentStatus.REJECTED);
            } else {
                if (incomeRecContent.getQtyAccepted().compareTo(incomeRecContent.getIncomeContentIn().getQty()) == 0 && incomeRecContent.getNomenIn() != null) {
                    incomeRecContent.setStatus(IncomeRecContentStatus.DONE);
                } else {
                    incomeRecContent.setStatus(IncomeRecContentStatus.IN_PROGRESS);
                }
            }
        }
        DaoMem.getDaoMem().writeLocalDataIncomeRec(incomeRec);
        if (addQty == 1) {
            MessageUtils.playSound(R.raw.bottle_one);
        }
        if (addQty > 1) {
            MessageUtils.playSound(R.raw.bottle_many);
        }

        this.lastMark = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.linkToListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.unLinkFromListener(this);
    }

    private void setResources() {
        View container = findViewById(R.id.inclRecPrihContent);
        docRecContentHolder = new IncomeContentArrayAdapter.DocRecContentHolder(container);


        tvAction = (TextView) findViewById(R.id.tvAction);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Вычисление: [Количество принятое (надпись)] = [Количество принятое (надпись)] + [Принимаемое количество].
                // Перед вычислением производится проверка: Количество принятое - не может быть больше количества по ТТН.
                // Если превышает - сообщение “Принятое количество не может быть больше количества по ТТН”, вычисление не выполняется.
                try {
                    double currQty = incomeRecContent.getQtyAccepted() == null ? 0 : incomeRecContent.getQtyAccepted();
                    double addQty = Double.valueOf(etQtyAccepted.getText().toString());
                    if ((currQty + addQty) > incomeRecContent.getIncomeContentIn().getQty()) {
                        MessageUtils.showToastMessage("Принятое количество не может быть больше количества по ТТН");
                    } else {
                        proceedAddQtyInternal(addQty);
                        // После успешного вычисления - возврат в форму “Приход ЕГАИС”
                        ActIncomeRecContent.this.finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    etQtyAccepted.setText("");
                    MessageUtils.showToastMessage("Неверное количество!");
                }
            }
        });
        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageUtils.ShowModalAndConfirm(ActIncomeRecContent.this, "Внимание!", "Очистить данные по позиции?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                incomeRecContent.setQtyAccepted(null);
                                incomeRecContent.getIncomeRecContentMarkList().clear();
                                lastMark = null;
                                isBoxScanned = false;
                                proceedAddQtyInternal(0);
                                updateDisplayData();
                            }
                        });

            }
        });
        etQtyAccepted = (EditText) findViewById(R.id.etQtyAccepted);
        llAccepted = (LinearLayout) findViewById(R.id.llAccepted);
    }

    private void updateDisplayData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (incomeRecContent.getPositionType() == IncomeRecContentPositionType.MARKED) {
                    etQtyAccepted.setEnabled(false);
                    llAccepted.setVisibility(View.GONE);
                } else {
                    etQtyAccepted.setEnabled(true);
                    llAccepted.setVisibility(View.VISIBLE);
                }
                //Для немаркированной продукции кнопка доступна только если ранее была определена номенклатура 1С (по ШК) или если позиция является разливным пивом (Емкость (ЕГАИС) = 0).
                // Для маркированной продукции - кнопка не доступна.
                if (incomeRecContent.getPositionType() == IncomeRecContentPositionType.NONMARKED_LIQUID ||
                        (incomeRecContent.getPositionType() == IncomeRecContentPositionType.NONMARKED &&
                                incomeRecContent.getNomenIn() != null )
                        ) {
                    btnAdd.setEnabled(true);
                } else {
                    btnAdd.setEnabled(false);
                }
                // Текст надписи зависит от типа позиции:
                switch (incomeRecContent.getPositionType()) {
                    case NONMARKED_LIQUID:
                        tvAction.setText("Введите Принимаемое количество в декалитрах и нажмите Добавить");
                        break;
                    case NONMARKED:
                        tvAction.setText("Сканируйте ШК с бутылки, введите Принимаемое количество и нажмите Добавить");
                        break;
                    case MARKED:
                        if (incomeRecContent.getNomenIn() == null) {
                            tvAction.setText("Сканируйте ШК");
                        } else {
                            tvAction.setText("Сканируйте марки со всех бутылок этой позиции");
                        }
                }
                int countToAddInFuture = 0;
                if (ActIncomeRecContent.this.lastMark != null) {
                    // Посчитать количество добавляемое, в случае успеха сканирования ШК ЕАН (предварительное добавленное колво)
                    countToAddInFuture = DaoMem.getDaoMem().calculateQtyToAdd(ActIncomeRecContent.this.incomeRec, ActIncomeRecContent.this.incomeRecContent, ActIncomeRecContent.this.lastMark);
                }
                docRecContentHolder.setItem(incomeRecContent, countToAddInFuture);
            }
        });
    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        int addQty = this.lastMark != null ? 1 : 0;
        if (this.isBoxScanned && this.lastMark !=null) {
            addQty = DaoMem.getDaoMem().calculateQtyToAdd(this.incomeRec, this.incomeRecContent, this.lastMark);
        }
        // Определить тип ШК
        final BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        IncomeRecContent incomeRecContentLocal;
        switch (barCodeType) {
            case EAN13:
                if (!this.isOpenByScan) {
                    MessageUtils.showModalMessage(this,"ВНИМАНИЕ!","Устанавливать связь со ШК можно только после сканирования марки");
                    break;
                }
                //Сканирование ШК номенклатуры (EAN):
                // Проверить наличие ШК в справочнике номенклатуры 1С.
                NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcode(barcodeReadEvent.getBarcodeData());
                //Если в номенклатуре нет такого ШК - запрет приемки: звуковой сигнал и сообщение “Штрихкод [указать номер] отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику”. Запретить ввод значения в поле “Принимаемое количество”
                if (nomenIn == null) {
                    MessageUtils.showModalMessage(this, "ВНИМАНИЕ!","Штрихкод "+barcodeReadEvent.getBarcodeData()+" отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику");
                    MessageUtils.playSound(R.raw.no_ean);
                    incomeRecContent.setNomenIn(null);
                    this.lastMark= null;
                    this.isBoxScanned = false;
                    proceedAddQtyInternal(0);
                } else {
                    //Если ШК товара найден в номенклатуре 1С - заполнить все надписи формы из номенклатуры 1С (код, наименование, ….)
                    incomeRecContent.setNomenIn(nomenIn);
                    proceedAddQtyInternal(addQty);
                }
                updateDisplayData();
                break;
            case PDF417:
                // Сканирование Pdf417 в карточке позиции
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showToastMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                this.isBoxScanned = false;
                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                ActIncomeRec.ActionOnScanPDF417Wrapper actionOnScanPDF417Wrapper = ActIncomeRec.proceedPdf417(this, incomeRec, barcodeReadEvent.getBarcodeData(), this);
                if (actionOnScanPDF417Wrapper != null) {

                    if (actionOnScanPDF417Wrapper.ircList.size() == 1) {
                        proceedWithPdf417AndBarcode(actionOnScanPDF417Wrapper.ircList.get(0), actionOnScanPDF417Wrapper.addQty, actionOnScanPDF417Wrapper.addQty == 0 ? null : barcodeReadEvent.getBarcodeData());
                    } else {
                        // тоже выбор дат....
                        ActIncomeRec.pickBottlingDate(this, incomeRec.getWbRegId(), actionOnScanPDF417Wrapper.ircList, barcodeReadEvent.getBarcodeData(), this);
                    }
                }
                break;
            case DATAMATRIX:
                // Сканирование DataMatrix в карточке позиции
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showToastMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                this.isBoxScanned = false;
                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                ActIncomeRec.ActionOnScanDataMatrixWrapper actionOnScanDataMatrixWrapper = ActIncomeRec.proceedDataMatrix(incomeRec, barcodeReadEvent.getBarcodeData());
                if (actionOnScanDataMatrixWrapper != null) {
                    this.incomeRecContent = actionOnScanDataMatrixWrapper.irc;
                    this.lastMark = barcodeReadEvent.getBarcodeData();
                    boolean resCheck = checkQtyOnLastMark();
                    if (resCheck && this.incomeRecContent.getNomenIn() != null) {
                        // Если товар сопоставлен - сохраняем сразу
                        proceedAddQtyInternal(actionOnScanDataMatrixWrapper.addQty);
                    }

                    updateDisplayData();
                    this.isOpenByScan = true;
                }

                break;
            case CODE128:
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showToastMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                // Проверить сканирован ли этот ШК коробки уже
                IncomeRecContentMark scannedMark = DaoMem.getDaoMem().findIncomeRecContentScannedMarkBox(incomeRec, barcodeReadEvent.getBarcodeData());
                if (scannedMark != null) {
                    MessageUtils.showToastMessage("Марка коробки уже сканирована!");
                }

                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                incomeRecContentLocal = ActIncomeRec.proceedCode128(incomeRec, barcodeReadEvent.getBarcodeData());
                if (incomeRecContentLocal != null) {
                    addQty = DaoMem.getDaoMem().calculateQtyToAdd(incomeRec, incomeRecContentLocal, barcodeReadEvent.getBarcodeData());

                    this.isBoxScanned = true;
                    this.incomeRecContent = incomeRecContentLocal;
                    this.lastMark = barcodeReadEvent.getBarcodeData();
                    boolean resCheck = checkQtyOnLastMark();
                    if (resCheck && this.incomeRecContent.getNomenIn() != null) {
                        // Если товар сопоставлен - сохраняем сразу
                        proceedAddQtyInternal(addQty);
                    }
                    updateDisplayData();
                }
                break;
        }

    }

    private void proceedWithPdf417AndBarcode(IncomeRecContent icr, Integer addQty, String barcode) {
        this.incomeRecContent = icr;
        this.lastMark = barcode;
        boolean resCheck = addQty > 0 && checkQtyOnLastMark();
        if (resCheck && this.incomeRecContent.getNomenIn() != null) {
            // Если товар сопоставлен - сохраняем сразу
            proceedAddQtyInternal(addQty);
        }

        updateDisplayData();
        this.isOpenByScan = true;
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onCallbackPickBottlingDate(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        proceedWithPdf417AndBarcode(irc, addQty, barcode);
    }

    @Override
    public void doFinishTransferCallback(Context ctx, String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        prepareActWithData(wbRegId, irc, addQty, barcode);
    }
}
