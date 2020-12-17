package com.glvz.egais.service;

import android.support.annotation.Nullable;
import android.util.Log;
import com.glvz.egais.integration.model.CommandIn;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.ksoap2.serialization.MarshalHashtable.NAMESPACE;

public class CommandCall {
    private final List<PropertyInfo> propertyInfoList = new ArrayList<>();
    private final String URL;
    private final String user;
    private final String password;
    private final String operation;

    public CommandCall(CommandIn commandIn, String barcode, String shopId, String userId) {
        this.URL = commandIn.getUrl();
        this.user = commandIn.getUser();
        this.password = commandIn.getPass();
        this.operation = commandIn.getOperation();
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
                SoapObject res = getData();
                if (res != null) {
                    commandFinishCallback.finishCommand(res.getInnerText().toString());
                } else {
                    commandFinishCallback.finishCommand("Произошла ошибка вызова сервиса 1с!");
                }
            }
        }).start();
    }

    @Nullable
    private SoapObject getData() {
        String errorText = "";
        SoapObject request = new SoapObject(NAMESPACE, "get");
        for (PropertyInfo propertyInfo : propertyInfoList) {
            request.addProperty(propertyInfo);
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
        androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

        //авторизация
        List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
        headerList.add(new HeaderProperty("Authorization", "Basic "+org.kobjects.base64.Base64.encode(password.getBytes())));

        //выполнение запроса к веб-сервису
        try {

            androidHttpTransport.call(operation, envelope, headerList);

        } catch (IOException e) {
            errorText = "CONNECTION_ERROR";
            return null;
        } catch (XmlPullParserException e) {
            errorText = "XML_ERROR";
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
