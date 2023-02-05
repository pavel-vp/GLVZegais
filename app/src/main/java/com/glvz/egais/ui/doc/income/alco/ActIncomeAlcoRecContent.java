package com.glvz.egais.ui.doc.income.alco;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.daodb.DaoDbDoc;
import com.glvz.egais.integration.model.doc.income.IncomeContentBoxTreeIn;
import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.model.BaseRecContentMark;
import com.glvz.egais.model.BaseRecContentPositionType;
import com.glvz.egais.model.BaseRecContentStatus;
import com.glvz.egais.model.income.*;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.service.PickBottliingDateCallback;
import com.glvz.egais.service.TransferCallback;
import com.glvz.egais.service.income.alco.IncomeAlcoRecContentHolder;
import com.glvz.egais.utils.BarcodeObject;
import com.glvz.egais.utils.MessageUtils;
import com.glvz.egais.utils.StringUtils;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.List;

import static com.glvz.egais.utils.BarcodeObject.BarCodeType.DATAMATRIX;
import static com.glvz.egais.utils.BarcodeObject.BarCodeType.PDF417;

public class ActIncomeAlcoRecContent extends Activity implements BarcodeReader.BarcodeListener, PickBottliingDateCallback, TransferCallback {

    private static final int CHANGENOMEN_REQUESTCODE = 1;
    public static final String NEWBARCODE = "barcode";

    private String lastMark;
    private IncomeRec incomeRec;
    private IncomeRecContent incomeRecContent;
    private IncomeAlcoRecContentHolder incomeRecContentHolder;

    TextView tvAction;
    EditText etQtyAccepted;
    LinearLayout llAccepted;
    Button btnAdd;
    Button btnManualChange;
    private boolean isBoxScanned = false;
    private boolean isOpenByScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomealcoreccontent);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setResources();

        Bundle extras = getIntent().getExtras();
        String wbRegId = extras.getString(ActIncomeAlcoRec.REC_DOCID);
        IncomeRec ir = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        String position = extras.getString(ActIncomeAlcoRec.RECCONTENT_POSITION);
        IncomeRecContent irc = (IncomeRecContent) DaoMem.getDaoMem().getRecContentByPosition(ir, position);
        String barcode = extras.getString(ActIncomeAlcoRec.RECCONTENT_LASTMARK);
        int addQty = extras.getInt(ActIncomeAlcoRec.RECCONTENT_ADDQTY);
        this.isBoxScanned = extras.getBoolean(ActIncomeAlcoRec.RECCONTENT_ISBOXSCANNED);
        this.isOpenByScan = extras.getBoolean(ActIncomeAlcoRec.RECCONTENT_ISOPENBYSCAN);

        prepareActWithData(wbRegId, irc, addQty, barcode);


    }

    private void prepareActWithData(String wbRegId, IncomeRecContent irc, int addQty, String barcode) {
        this.incomeRec = DaoMem.getDaoMem().getMapIncomeRec().get(wbRegId);
        this.incomeRecContent = irc;
        this.lastMark = barcode;

        boolean checkMark = addQty > 0 && checkQtyOnLastMark();
        if (this.incomeRecContent.getNomenIn() != null)  {
            if (this.incomeRecContent.getContentIn().getQtyDirectInput() == 1) {
                addQty = 0;
                MessageUtils.playSound(R.raw.enter_qty);
            }
        }
        if (checkMark && this.incomeRecContent.getNomenIn() != null  ) {
            // Если товар сопоставлен - сохраняем сразу
            proceedAddQtyInternal(addQty);
        }
        updateDisplayData();
        if (this.isOpenByScan && this.incomeRecContent.getNomenIn() == null) {
            MessageUtils.playSound(R.raw.scan_ean);
        }
    }

    private boolean checkQtyOnLastMark() {
        if (this.lastMark != null) {
            // Проверить: [количество по ТТН] > [Принятое количество]
            if (incomeRecContent.getQtyAccepted() !=null &&
                    incomeRecContent.getContentIn().getQty().compareTo(incomeRecContent.getQtyAccepted()) <= 0 &&
                    incomeRecContent.getContentIn().getQtyDirectInput() == 0) {
                if (!this.isBoxScanned) {
                    String alcocode = BarcodeObject.extractAlcode(this.lastMark);
                    MessageUtils.showModalMessage(this, "Внимание!","По позиции номер: %s, алкокод: %s, (%s) уже принято полное количество %s. Сканированная бутылка лишняя, принимать нельзя. Верните поставщику!",
                            incomeRecContent.getPosition(),
                            alcocode,
                            incomeRecContent.getContentIn().getName(),
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
                BaseRecContentMark ircm = DaoMem.getDaoMem().findIncomeRecContentMarkByMarkScanned(this.incomeRec, mb.icm.getMark());
                //Если добавляемой марки еще нет в списке принятых: добавить ее в список принятых, признак сканирования установить в значение уровня вложенности упаковки (см. пред. пункт), принятое количество увеличить на 1 шт.
                if (ircm == null) {
                    // В этот момент ШК товара уже должен быть, можем добавлять марку
                    incomeRecContent.getBaseRecContentMarkList().add(new BaseRecContentMark(mb.icm.getMark(), mb.level, this.lastMark));
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
                this.incomeRecContent.getBaseRecContentMarkList().add(new BaseRecContentMark(this.lastMark, BaseRecContentMark.MARK_SCANNED_AS_MARK, this.lastMark));
            }
        }
        if (addQty != 0) {
            incomeRecContent.setQtyAccepted(incomeRecContent.getQtyAccepted() == null ? addQty : incomeRecContent.getQtyAccepted() + addQty);
        }
        if (incomeRecContent.getQtyAccepted() == null) {
            incomeRecContent.setStatus(BaseRecContentStatus.NOT_ENTERED);
        } else {
            if (incomeRecContent.getQtyAccepted().equals(Double.valueOf(0))) {
                incomeRecContent.setStatus(BaseRecContentStatus.REJECTED);
            } else {
                if (incomeRecContent.getQtyAccepted().compareTo(incomeRecContent.getContentIn().getQty()) == 0 && incomeRecContent.getNomenIn() != null) {
                    incomeRecContent.setStatus(BaseRecContentStatus.DONE);
                } else {
                    incomeRecContent.setStatus(BaseRecContentStatus.IN_PROGRESS);
                }
            }
        }
        DaoDbDoc.getDaoDbDoc().saveDbDocRecContent(incomeRec, incomeRecContent);
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
        BarcodeObject.setCurrentListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.setCurrentListener(null);
    }

    private void setResources() {
        View container = findViewById(R.id.inclRecIncomeAlcoContent);
        incomeRecContentHolder = new IncomeAlcoRecContentHolder(container);


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
                    if ((currQty + addQty) > incomeRecContent.getContentIn().getQty()) {
                        MessageUtils.showToastMessage("Принятое количество не может быть больше количества по ТТН");
                    } else {
                        proceedAddQtyInternal(addQty);
                        // После успешного вычисления - возврат в форму “Приход ЕГАИС”
                        ActIncomeAlcoRecContent.this.finish();
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
                MessageUtils.ShowModalAndConfirm(ActIncomeAlcoRecContent.this, "Внимание!", "Очистить данные по позиции?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                incomeRecContent.setQtyAccepted(null);
                                DaoDbDoc.getDaoDbDoc().writeLocalDataRecContent_ClearAllMarks(incomeRec.getDocId(), incomeRecContent);
                                incomeRecContent.getBaseRecContentMarkList().clear();
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
        btnManualChange = (Button) findViewById(R.id.btnManualChange);
        btnManualChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent();
                in.setClass(ActIncomeAlcoRecContent.this, ActIncomeAlcoRecContentChangeNomen.class);
                in.putExtra(ActIncomeAlcoRec.REC_DOCID, incomeRec.getDocId());
                in.putExtra(ActIncomeAlcoRec.RECCONTENT_POSITION, incomeRecContent.getPosition());
                ActIncomeAlcoRecContent.this.startActivityForResult(in, CHANGENOMEN_REQUESTCODE);
            }
        });

    }

    private void updateDisplayData() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (incomeRecContent.getPositionType() == BaseRecContentPositionType.MARKED) {
                    etQtyAccepted.setEnabled(false);
                    llAccepted.setVisibility(View.GONE);
                } else {
                    etQtyAccepted.setEnabled(true);
                    llAccepted.setVisibility(View.VISIBLE);
                }
                //Для немаркированной продукции кнопка доступна только если ранее была определена номенклатура 1С (по ШК) или если позиция является разливным пивом (Емкость (ЕГАИС) = 0).
                // Для маркированной продукции - кнопка не доступна.
                if (incomeRecContent.getPositionType() == BaseRecContentPositionType.NONMARKED_LIQUID ||
                        (incomeRecContent.getPositionType() == BaseRecContentPositionType.NONMARKED &&
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
                            if (incomeRecContent.getContentIn().getQtyDirectInput() == 1) {
                                tvAction.setText("Введите принимаемое количество");
                                etQtyAccepted.setEnabled(true);
                                llAccepted.setVisibility(View.VISIBLE);
                                btnAdd.setEnabled(true);
                            } else {
                                tvAction.setText("Сканируйте марки со всех бутылок этой позиции");
                            }
                        }
                }
                int countToAddInFuture = 0;
                if (ActIncomeAlcoRecContent.this.lastMark != null) {
                    // Посчитать количество добавляемое, в случае успеха сканирования ШК ЕАН (предварительное добавленное колво)
                    countToAddInFuture = DaoMem.getDaoMem().calculateQtyToAdd(ActIncomeAlcoRecContent.this.incomeRec, ActIncomeAlcoRecContent.this.incomeRecContent, ActIncomeAlcoRecContent.this.lastMark);
                }
                incomeRecContentHolder.setItem(incomeRecContent, countToAddInFuture, DocContentArrayAdapter.RECCONTENT_MODE);
                if (incomeRecContent.getNomenIn() == null) {
                    btnManualChange.setEnabled(false);
                } else {
                    btnManualChange.setEnabled(true);
                }
            }
        });
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        int addQty = this.lastMark != null ? 1 : 0;
        if (this.isBoxScanned && this.lastMark !=null) {
            addQty = DaoMem.getDaoMem().calculateQtyToAdd(this.incomeRec, this.incomeRecContent, this.lastMark);
        }
        // Определить тип ШК
        BarcodeObject.BarCodeType barCodeType = BarcodeObject.getBarCodeType(barcodeReadEvent);
        IncomeRecContent incomeRecContentLocal;
        // В документах с CheckMark = “DataMatrixPDF417” событие сканирования PDF417 обрабатывается аналогично сканированию DataMatrix.
        if (barCodeType == PDF417 && "DataMatrixPDF417".equals(incomeRec.getIncomeIn().getCheckMark())) {
            barCodeType = DATAMATRIX;
        }
        switch (barCodeType) {
            case EAN8:
            case EAN13:
                if (!this.isOpenByScan && incomeRecContent.getPositionType() == BaseRecContentPositionType.MARKED) {
                    MessageUtils.showModalMessage(this,"ВНИМАНИЕ!","Устанавливать связь со ШК можно только после сканирования марки");
                    break;
                }
                //Сканирование ШК номенклатуры (EAN):
                // Проверить наличие ШК в справочнике номенклатуры 1С.
                final NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcodeAlco(barcodeReadEvent.getBarcodeData());
                //Если в номенклатуре нет такого ШК - запрет приемки: звуковой сигнал и сообщение “Штрихкод [указать номер] отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Верните все бутылки этой позиции поставщику”. Запретить ввод значения в поле “Принимаемое количество”
                if (nomenIn == null) {
                    MessageUtils.showModalMessage(this, "ВНИМАНИЕ!","Штрихкод "+barcodeReadEvent.getBarcodeData()+" отсутствует в номенклатуре 1С. Прием этой позиции запрещен. Сообщите категорийному менеджеру или верните все бутылки этой позиции поставщику.");
                    MessageUtils.playSound(R.raw.no_ean);
                    incomeRecContent.setNomenIn(null, null);
                    this.lastMark= null;
                    this.isBoxScanned = false;
                    proceedAddQtyInternal(0);
                } else {
                    //При сопоставлении с номенклатурой 1С проверять соответствие емкостей ЕГАИС и 1С
                    if (!StringUtils.formatQty(incomeRecContent.getContentIn().getCapacity()).equals(StringUtils.formatQty(nomenIn.getCapacity()))) {
                        MessageUtils.showModalMessage(this, "ВНИМАНИЕ!","Емкость номенклатуры 1С не соответствует ЕГАИС.\n" +
                                "Номенклатура:\n" +
                                        "Наименование: %s\n" +
                                        "Код: %s\n" +
                                "Штрихкод: %s\n" +
                                "Емкость 1С: %s\n" +
                                "Емкость ЕГАИС: %s",
                                nomenIn.getName(),
                                nomenIn.getId(), barcodeReadEvent.getBarcodeData(),
                                StringUtils.formatQty(nomenIn.getCapacity()),
                                StringUtils.formatQty(incomeRecContent.getContentIn().getCapacity())
                                );
                        break;
                    } else {
                        // Если новый товар 1с отличается от того что был - запросить подтверждение
                        if (incomeRecContent.getNomenIn() != null && !incomeRecContent.getNomenIn().getId().equals(nomenIn.getId())) {
                            final int[] finalAddQty = {addQty};
                            MessageUtils.ShowModalAndConfirm(this, "ВНИМАНИЕ!", "Сопоставить с товаром?\n " +
                                            "ЕГАИС:\n" +
                                            "Наименование: %s\n" +
                                            "Емкость: %s\n" +
                                            "Крепость: %s\n" +
                                            "1C:\n" +
                                            "Наименование: %s\n" +
                                            "Емкость: %s\n" +
                                            "Крепость: %s",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            incomeRecContent.setNomenIn(nomenIn, barcodeReadEvent.getBarcodeData());
                                            if (incomeRecContent.getContentIn().getQtyDirectInput() == 1) {
                                                MessageUtils.playSound(R.raw.enter_qty);
                                                finalAddQty[0] = 0;
                                            }
                                            proceedAddQtyInternal(finalAddQty[0]);
                                            updateDisplayData();
                                        }
                                    },
                                    incomeRecContent.getContentIn().getName(),
                                    StringUtils.formatQty(incomeRecContent.getContentIn().getCapacity()),
                                    StringUtils.formatQty(incomeRecContent.getContentIn().getAlcVolume()),
                                            nomenIn.getName(),
                                            StringUtils.formatQty(nomenIn.getCapacity()),
                                            StringUtils.formatQty(nomenIn.getAlcVolume())
                            );
                        } else {
                            // Если ШК товара найден в номенклатуре 1С - заполнить все надписи формы из номенклатуры 1С (код, наименование, ….)
                            incomeRecContent.setNomenIn(nomenIn, barcodeReadEvent.getBarcodeData());
                            if (incomeRecContent.getContentIn().getQtyDirectInput() == 1) {
                                MessageUtils.playSound(R.raw.enter_qty);
                                addQty = 0;
                            }
                            proceedAddQtyInternal(addQty);
                        }
                    }
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
                ActIncomeAlcoRec.ActionOnScanPDF417Wrapper actionOnScanPDF417Wrapper = ActIncomeAlcoRec.proceedPdf417(this, incomeRec, barcodeReadEvent.getBarcodeData(), this);
                if (actionOnScanPDF417Wrapper != null) {

                    if (actionOnScanPDF417Wrapper.ircList.size() == 1) {
                        proceedWithPdf417AndBarcode(actionOnScanPDF417Wrapper.ircList.get(0), actionOnScanPDF417Wrapper.addQty, actionOnScanPDF417Wrapper.addQty == 0 ? null : barcodeReadEvent.getBarcodeData());
                    } else {
                        // тоже выбор дат....
                        ActIncomeAlcoRec.pickBottlingDate(this, incomeRec.getDocId(), actionOnScanPDF417Wrapper.ircList, barcodeReadEvent.getBarcodeData(), this);
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
                ActIncomeAlcoRec.ActionOnScanDataMatrixWrapper actionOnScanDataMatrixWrapper = ActIncomeAlcoRec.proceedDataMatrix(this, incomeRec, barcodeReadEvent.getBarcodeData());
                if (actionOnScanDataMatrixWrapper != null) {
                    this.incomeRecContent = actionOnScanDataMatrixWrapper.irc;
                    if (actionOnScanDataMatrixWrapper.addQty > 0) {
                        this.lastMark = barcodeReadEvent.getBarcodeData();
                    }
                    boolean resCheck = actionOnScanDataMatrixWrapper.addQty > 0 && checkQtyOnLastMark();
                    if (resCheck && this.incomeRecContent.getNomenIn() != null) {
                        // Если товар сопоставлен - сохраняем сразу
                        proceedAddQtyInternal(actionOnScanDataMatrixWrapper.addQty);
                    }

                    updateDisplayData();
                    this.isOpenByScan = true;
                    if (this.incomeRecContent.getNomenIn() == null) {
                        MessageUtils.playSound(R.raw.scan_ean);
                    }
                }

                break;
            case CODE128:
                if (this.lastMark != null && incomeRecContent.getNomenIn() == null) {
                    MessageUtils.showToastMessage("Марка уже сканирована, сканируйте ШК бутылки!");
                    break;
                }
                // Проверить сканирован ли этот ШК коробки уже
                BaseRecContentMark scannedMark = DaoMem.getDaoMem().findIncomeRecContentScannedMarkBox(incomeRec, barcodeReadEvent.getBarcodeData());
                if (scannedMark != null) {
                    MessageUtils.showToastMessage("Марка коробки уже сканирована!");
                }

                // без сохранения предыдущего состояния - та же обработка что и в картчоке накладной
                incomeRecContentLocal = ActIncomeAlcoRec.proceedCode128(this, incomeRec, barcodeReadEvent.getBarcodeData());
                if (incomeRecContentLocal != null) {
                    addQty = DaoMem.getDaoMem().calculateQtyToAdd(incomeRec, incomeRecContentLocal, barcodeReadEvent.getBarcodeData());

                    this.isBoxScanned = true;
                    this.isOpenByScan = true;
                    this.incomeRecContent = incomeRecContentLocal;
                    this.lastMark = barcodeReadEvent.getBarcodeData();
                    boolean resCheck = checkQtyOnLastMark();
                    if (resCheck && this.incomeRecContent.getNomenIn() != null) {
                        // Если товар сопоставлен - сохраняем сразу
                        proceedAddQtyInternal(addQty);
                    }
                    updateDisplayData();
                    if (this.incomeRecContent.getNomenIn() == null) {
                        MessageUtils.playSound(R.raw.scan_ean);
                    }
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
            if (this.incomeRecContent.getContentIn().getQtyDirectInput() == 1) {
                addQty = 0;
            }
            proceedAddQtyInternal(addQty);
        }

        updateDisplayData();
        this.isOpenByScan = true;
        if (this.incomeRecContent.getNomenIn() == null) {
            MessageUtils.playSound(R.raw.scan_ean);
        } else {
            if (this.incomeRecContent.getContentIn().getQtyDirectInput() == 1) {
                MessageUtils.playSound(R.raw.enter_qty);
            }
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGENOMEN_REQUESTCODE) {
            if (resultCode == RESULT_OK) {
                String newBarCode = (String) data.getExtras().get(NEWBARCODE);
                final NomenIn nomenIn = DaoMem.getDaoMem().getDictionary().findNomenByBarcodeAlco(newBarCode);
                incomeRecContent.setNomenIn(nomenIn, newBarCode);
                DaoDbDoc.getDaoDbDoc().saveDbDocRecContent(incomeRec, incomeRecContent);
                updateDisplayData();
            }
        }
    }
}
