package com.glvz.egais.utils;

import android.content.Context;
import android.util.Log;
import com.honeywell.aidc.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BarcodeObject {
    private static BarcodeReader barcodeReader;
    private static AidcManager manager;

    public enum BarCodeType {
        EAN13, PDF417, DATAMATRIX, CODE128, UNSUPPORTED
    }


    public static void create(Context context) {
        // create the AidcManager providing a Context and a
        // CreatedCallback implementation.
        AidcManager.create(context, new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
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
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
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
        if (barcodeReadEvent.getCodeId().equals("d"))
            return BarCodeType.EAN13;
        if (barcodeReadEvent.getCodeId().equals("r"))
            return BarCodeType.PDF417;
        if (barcodeReadEvent.getCodeId().equals("w"))
            return BarCodeType.DATAMATRIX;
        if (barcodeReadEvent.getCodeId().equals("I") || barcodeReadEvent.getCodeId().equals("j") )
            return BarCodeType.CODE128;
        return BarCodeType.UNSUPPORTED;

    }

    public static String extractAlcode2(String input)
    {
        String code = input.substring(8, 20);
        String codeTemplate = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        BigInteger result = BigInteger.ZERO;
        int pos = 0;
        for (int i = code.length() - 1; i >= 0; i--)
        {
            int idx = codeTemplate.indexOf(code.charAt(i));
            BigInteger t = BigInteger.valueOf(36);
            result = result.add( t.pow(pos).multiply( BigInteger.valueOf(idx) )  );
            pos++;
        }
        return String.format("%019d", result);
    }

    public static String extractAlcode(String pdf417) {
        StringBuilder result = new StringBuilder();
        int x, y;
        BigInteger m;
        String code;
        code = pdf417.substring(8, 20);
//        byte[] bytes = new BigInteger(code, 36).toByteArray();
//        result.append(new String(bytes, StandardCharsets.UTF_8));


        String text = code;
        String codeTemplate = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        BigInteger num = BigInteger.ZERO;
        int j = code.length();
        for(int i = 0; i < j; i++){
            num = num.add( BigInteger.valueOf((long) (codeTemplate.indexOf(text.charAt(0))*Math.pow(codeTemplate.length(), i))));
            text = text.substring(1);
        }
        result.append(String.format("%019d", num));

/*
        result.append("0");
        for (x=1; x<=12; x++ ) {
            m = BigInteger.valueOf(1);
            for (y = 1; y <= 12 - x; y++) {
                m = m.multiply(BigInteger.valueOf(36));
            }
            char ch = code.charAt(x);
            int idx = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(ch) - 1;
            BigInteger chCode = m.multiply(BigInteger.valueOf(idx))
            char newCh = (char) chCode.;
            result.append(Character.toString ( newCh ));
        }
//        return String.format("%019s", result.toString());
*/
        return result.toString();

    }

}
