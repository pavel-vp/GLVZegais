package com.glvz.egais.service;

import android.support.annotation.Nullable;
import android.util.Log;
import com.glvz.egais.integration.model.CommandIn;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandCall {
    private final List<PropertyInfo> propertyInfoList = new ArrayList<>();
    private final String URL;
    private final String user;
    private final String password;
    private final String operation;
    private final String ns;
    private final String serviceName;

    public CommandCall(CommandIn commandIn, String barcode, String shopId, String userId) {
        this.URL = commandIn.getUrl();
        this.user = commandIn.getUser();
        this.password = commandIn.getPass();
        this.operation = commandIn.getOperation();
        this.ns = commandIn.getNs();
        this.serviceName = commandIn.getServiceName();
        for (String param : commandIn.getParams()) {
            PropertyInfo propertyInfo = new PropertyInfo();
            if (CommandIn.PARAM_BARCODE.equals(param)) {
                propertyInfo.setName(param);
                propertyInfo.setValue(barcode);
            }
            if (CommandIn.PARAM_USERID.equals(param)) {
                propertyInfo.setName(param);
                propertyInfo.setValue(userId);
            }
            if (CommandIn.PARAM_SHOPID.equals(param)) {
                propertyInfo.setName(param);
                propertyInfo.setValue(shopId);
            }
            propertyInfoList.add(propertyInfo);
        }
    }

    public void call(final CommandFinishCallback commandFinishCallback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String res = callWS();
                if (res != null) {
                    commandFinishCallback.finishCommand(res);
                } else {
                    commandFinishCallback.finishCommand("Произошла ошибка вызова сервиса 1с!");
                }
            }
        }).start();
    }

    private String callWS() {
        //String pSOAP_ACTION = "http://www.webserviceX.NET/GetCitiesByCountry";
        String pMETHOD_NAME = operation; //"GetCitiesByCountry";
        String pSERVICE = serviceName;
        String pNAMESPACE = ns; //"http://www.webserviceX.NET";
        String pURL = URL; //"http://www.webservicex.com/globalweather.asmx?WSDL";

        String pSOAP_ACTION = pNAMESPACE + "#" + pSERVICE + ":" + pMETHOD_NAME ;// NS + "#AcceptingOrders:" + method
        String result="invalid";
        try
        {
            SoapObject request = new SoapObject(pNAMESPACE, pMETHOD_NAME);
            for (PropertyInfo propertyInfo : propertyInfoList) {
                request.addProperty(propertyInfo);
            }
            //Request.addProperty("CountryName", "India");

            SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            soapEnvelope.dotNet = true;
            soapEnvelope.setAddAdornments(false);
            soapEnvelope.skipNullProperties=true;
            soapEnvelope.implicitTypes=true;
            soapEnvelope.setOutputSoapObject(request);
            HttpTransportSE transport = new HttpTransportSE(pURL);
            transport.debug=true;
            //авторизация
            List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
            headerList.add(new HeaderProperty("Authorization", "Basic "+org.kobjects.base64.Base64.encode((user + ":" + password).getBytes())));

            try {
                transport.call(pSOAP_ACTION, soapEnvelope, headerList);
            } catch (Throwable t) {
                Log.e("KSOAP2", "transport.requestDump: " + transport.requestDump, t);
                Log.e("KSOAP2", "transport.responseDump: " + transport.responseDump, t);
            }
            SoapPrimitive resultString;
            resultString = (SoapPrimitive) soapEnvelope.getResponse();
            result = resultString .toString() ;
            return result ;
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            return e.getMessage();
        }
    }


    @Nullable
    private SoapObject getData() {
        String errorText = "";

        SoapObject request = new SoapObject(ns, operation);
        for (PropertyInfo propertyInfo : propertyInfoList) {
            request.addProperty(propertyInfo);
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        androidHttpTransport.debug = true;
        androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        //авторизация
        List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
        headerList.add(new HeaderProperty("Authorization", "Basic "+org.kobjects.base64.Base64.encode((user + ":" + password).getBytes())));

        //выполнение запроса к веб-сервису
        try {

            androidHttpTransport.call(ns+"#"+serviceName+":"+operation, envelope, headerList);

        } catch (Throwable e) {
            errorText = "CONNECTION_ERROR";
            Log.e("KSOAP", e.getMessage(),e);
            return null;
        }

        Object result = envelope.bodyIn;
        if (result != null) {
            try {
                return (SoapObject) result;
            }catch (Exception e){
                Log.e("KSOAP", result.toString());
            }
        }

        return null;
    }
}
