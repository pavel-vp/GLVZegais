package com.glvz.egais.utils;

import android.content.Context;
import android.util.Log;
import com.honeywell.aidc.*;

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

}
