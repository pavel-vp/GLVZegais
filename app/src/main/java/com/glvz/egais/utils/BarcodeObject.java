package com.glvz.egais.utils;

import android.content.Context;
import android.util.Log;
import com.honeywell.aidc.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BarcodeObject implements BarcodeReader.BarcodeListener {

    private static BarcodeObject barcodeObject;

    public static BarcodeObject getInstance() {
        if (barcodeObject == null) {
            barcodeObject = new BarcodeObject();
        }
        return barcodeObject;
    }

    private static BarcodeReader barcodeReader;
    private static AidcManager manager;
    private static BarcodeReader.BarcodeListener currentListener;

    // Разбор сигаретного сканированного ШК с марки
    // В итоге вернется вырезанная часть, по правилам
    public static String extractSigaMark(String m) {
        try {
            if ("00".equals(m.substring(0, 2))) {
                // штучная марка
                //int pos = m.indexOf('\u001D');
                //if (pos >= 0) {
                //    return m.substring(0, 21);
                //}
                //return m;
                return m.substring(0, 25);
            }
            if ("01".equals(m.substring(0, 2)) || "02".equals(m.substring(0, 2))) {
                // групповая марка
                // индекс первого спецсимвола
                int pos = m.indexOf('\u001D', 24);
                // если после первого спецсимвола еще что-то есть
                if (pos < m.length()) {
                    String tPrice = m.substring(pos + 1, pos + 1 + 4);
                    // Если после первого спецсивола - тэг 8005 и цена - вырежем ровно 10 символов (тег и цена)
                    if ("8005".equals(tPrice)) {
                        return m.substring(0, pos + 11);
                    }
                    // иначе - вырежем все до этого спецсимвола
                    return m.substring(0, pos);
                }
                return m;
            }
        } catch (Exception e) {
            Log.e("BarCodeObject","Неверная марка!", e);
        }
        return null;
    }

    @Override
    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        if (currentListener != null) {
            currentListener.onBarcodeEvent(barcodeReadEvent);
        }
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    public enum BarCodeType {
        EAN8, EAN13, PDF417, DATAMATRIX, CODE128, GS1_DATAMATRIX_CIGA, UPCA, UNSUPPORTED
    }


    public interface CallbackAfterCreateBarcodeReader {
        void afterCreate();
    }

    public static void create(Context context) {
        // create the AidcManager providing a Context and a
        // CreatedCallback implementation.
        AidcManager.create(context, new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                linkToListener(getInstance());
            }
        });

    }

    public static void linkToListener(BarcodeReader.BarcodeListener listener) {
        if (barcodeReader != null) {

            // register bar code event listener
            barcodeReader.addBarcodeListener(listener);

            // set the trigger mode to client control
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                System.out.println("Failed to apply properties");
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATA_PROCESSOR_LAUNCH_BROWSER, false);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_CHECK_DIGIT_TRANSMIT_ENABLED , true);
            properties.put(BarcodeReader.PROPERTY_UPC_E_CHECK_DIGIT_TRANSMIT_ENABLED , true);
            properties.put(BarcodeReader.PROPERTY_EAN_8_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_EAN_8_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
            setUp();
        }
    }

    public static void unLinkFromListener(BarcodeReader.BarcodeListener listener) {
            if (barcodeReader != null) {
                tearDown();
                // unregister barcode event listener
                barcodeReader.removeBarcodeListener(listener);
            }
    }

    private static BarcodeReader getBarcodeObject() {
        return barcodeReader;
    }


    public static void setUp() {
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tearDown() {
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            barcodeReader.release();
        }
    }

    public static void delete() {
        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

    public static BarCodeType getBarCodeType(BarcodeReadEvent barcodeReadEvent) {
        // FIXME: пока непонятно как по-другому
        if (barcodeReadEvent.getCodeId().equals("D") || barcodeReadEvent.getCodeId().equals("E") )
            return BarCodeType.EAN8;
        if (barcodeReadEvent.getCodeId().equals("d") || barcodeReadEvent.getCodeId().equals("c") || barcodeReadEvent.getCodeId().equals("e") )
            return BarCodeType.EAN13;
        if (barcodeReadEvent.getCodeId().equals("r"))
            return BarCodeType.PDF417;
        if (barcodeReadEvent.getCodeId().equals("w")) {
            if (barcodeReadEvent.getAimId().equals("]d2")) {
                return BarCodeType.GS1_DATAMATRIX_CIGA;
            }
            return BarCodeType.DATAMATRIX;
        }
        if (barcodeReadEvent.getCodeId().equals("I") || barcodeReadEvent.getCodeId().equals("j") )
            return BarCodeType.CODE128;
        return BarCodeType.UNSUPPORTED;

    }

    public static String extractAlcode(String input)
    {
        String code = input.substring(7, 19);
        String codeTemplate = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        BigInteger result = BigInteger.ZERO;
        int pos = 0;
        for (int i = code.length() - 1; i >= 0; i--)
        {
            int idx = codeTemplate.indexOf(code.charAt(i));
            BigInteger t = BigInteger.valueOf(36);
            t = t.pow(pos);
            t = t.multiply( BigInteger.valueOf(idx) );
            result = result.add( t  );
            pos++;
        }
        return String.format("%019d", result);
    }

    public static String extractEanFromGS1DM(String input) {
        String code = "";

        // определим подтип маркировки
        if ("01".equals(input.substring(0, 2))) {
            // "01" стартовый тег групповой маркировки, содержит еан13 после тега и одного лидирующего нуля
            code = input.substring(3, 16);
        }
        else if ("00".equals(input.substring(0, 2))) {
            // еан8 by LAG 2022-01-18 разделил еан8 и еан13
            code = input.substring(6, 14);
        }
        else {
            // еан13 by LAG 2022-01-18 разделил еан8 и еан13
            code = input.substring(1, 14);
        }

        // EAN содержится в первом поле, дина фиксированная 14 символов с забивкой лидирующими нулями. Для блока это будет  EAN 13, для пачек  EAN 8
        //String code = input.substring(3, 16);
        //if (code.substring(0,5).equals("00000")) {
        //    code = code.substring(5);
        //}

        return code;
    }

    public static void setCurrentListener(BarcodeReader.BarcodeListener listener) {
        currentListener = listener;
    }


}
