package com.glvz.egais.ui.doc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CheckBox;
import android.widget.ListView;
import com.glvz.egais.MainApp;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.service.DocArrayAdapter;
import com.glvz.egais.service.DocContentArrayAdapter;
import com.glvz.egais.utils.BarcodeObject;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.List;

public abstract class ActBaseDocRec extends Activity implements BarcodeReader.BarcodeListener {

    public final static String REC_DOCID = "DOCID";
    public final static String RECCONTENT_POSITION = "POSITION";
    public static final String RECCONTENT_ADDQTY = "ADDQTY";
    public static final String RECCONTENT_LASTMARK = "LASTMARK";
    public static final String RECCONTENT_ISBOXSCANNED = "ISBOXSCANNED";
    public static final String RECCONTENT_ISOPENBYSCAN = "ISOPENBYSCAN";

    protected DocArrayAdapter.DocRecHolder docRecHolder;
    protected CheckBox cbFilter;
    protected ListView lvContent;
    protected List<BaseRecContent> list = new ArrayList<>();
    protected DocContentArrayAdapter adapter;
    protected static final Handler handler = new Handler(MainApp.getContext().getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRec();
        setResources();
        updateData();
    }

    abstract protected void initRec();
    abstract protected void setResources();

    abstract protected void updateData();

    abstract protected void pickRec(Context ctx, String docId, BaseRecContent req, int addQty, String barcode, boolean isBoxScanned, boolean isOpenByScan);

    protected void syncDoc() {
        //- выполняется проверка подключенного WiFi, при наличии JSON-файл выгружается по FTP с записью в журнал.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DaoMem.getDaoMem().syncWiFiFtpShared();
                    DaoMem.getDaoMem().initDictionary();
                    DaoMem.getDaoMem().syncWiFiFtpShopDocs();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        BarcodeObject.setCurrentListener(this);
        updateData();
    }

    @Override
    public void onPause() {
        super.onPause();
        BarcodeObject.setCurrentListener(null);
    }


    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {

    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }
}
