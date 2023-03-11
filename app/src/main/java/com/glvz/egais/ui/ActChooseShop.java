package com.glvz.egais.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.glvz.egais.MainApp;
import com.glvz.egais.R;
import com.glvz.egais.dao.DaoMem;
import com.glvz.egais.integration.model.ShopIn;
import com.glvz.egais.integration.sdcard.Integration;
import com.glvz.egais.integration.sdcard.IntegrationSDCard;
import com.glvz.egais.utils.MessageUtils;

import java.io.IOException;
import java.util.List;

public class ActChooseShop extends Activity {

    private TextView tvShopChosen;
    private TextView tvUser;
    private Button buttonLoadDocs;
    private Button buttonExportDb;
    private Button buttonMainMenu;
    Integration integrationFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooseshop);
        setResources();
        updateData();
        // Если магазин у пользователя один - то он должен быть уже выбран - пропускаем эту форму
        if (DaoMem.getDaoMem().getUserIn().getUsersPodrs() != null && DaoMem.getDaoMem().getUserIn().getUsersPodrs().length == 1) {
            proceedMainMenu();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //BarcodeObject.delete();
    }


    private void setResources() {

        tvShopChosen = (TextView) findViewById(R.id.tvShopChosen);
        tvUser = (TextView) findViewById(R.id.tvUser);
        tvUser.setText(DaoMem.getDaoMem().getUserIn().getName());

        Button buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<ShopIn> shopInList = DaoMem.getDaoMem().getDictionary().getShopListByUser(DaoMem.getDaoMem().getUserIn());

                final CharSequence[] items = new CharSequence[shopInList.size()];
                int i = 0;
                for (ShopIn shopIn : shopInList) {
                    items[i] = shopIn.getName();
                    i++;
                }

                final int[] choice = {0};
                new AlertDialog.Builder(ActChooseShop.this)
                        .setTitle("Выберите магазин")
                        .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                choice[0] = which;
                            }
                        })
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShopIn shopIn = shopInList.get(choice[0]);
                                // запомнить выбор
                                DaoMem.getDaoMem().setShopId(shopIn.getId());
                                ActChooseShop.this.updateData();
                            }
                        }).show();

            }
        });

        buttonLoadDocs = (Button) findViewById(R.id.buttonLoadDocs);
        buttonLoadDocs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DaoMem.getDaoMem().initDocuments(true);
                clearAllShared();
            }
        });

        buttonExportDb = (Button) findViewById(R.id.buttonExportDb);
        buttonExportDb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    DaoMem.getDaoMem().exportDb();
                    MessageUtils.showToastMessage("БД выгружена");
                } catch (IOException e) {
                    e.printStackTrace();
                    MessageUtils.showToastMessage("Ошибка:" + e.getMessage());
                }
            }
        });

        buttonMainMenu = (Button) findViewById(R.id.buttonMainMenu);
        buttonMainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedMainMenu();
            }
        });

        Button buttonExit = (Button) findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActChooseShop.this.finish();
            }
        });
    }

    private void clearAllShared(){
        MessageUtils.ShowModalAndConfirm(this, "Внимание!", "Подтвердите удаление ВСЕХ данных введенных в ТСД. Операция необратима\n\n После выполнения необходимо перезапустить приложение.",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DaoMem.getDaoMem().clearAllSharedPreferences();
                        MessageUtils.showToastMessage("Все данные приложения удалены");
                    }
                });
    }

    private void proceedMainMenu() {
        Intent intent = new Intent();
        intent.setClass(ActChooseShop.this, ActMainMenu.class);
        startActivity(intent);
        // ActChooseShop.this.finish();
    }

    private void updateData() {
        tvShopChosen.setText(DaoMem.getDaoMem().getShopInName());
        if (DaoMem.getDaoMem().getShopId() == null) {
            buttonLoadDocs.setEnabled(false);
            buttonExportDb.setEnabled(false);
            buttonMainMenu.setEnabled(false);
        } else {
            if ((DaoMem.getDaoMem().getUserIn().getId().equals("290a6e3a974-4e9c-11e8-83d3-2c59e542040c")) ||   //Ложкин
                    (DaoMem.getDaoMem().getUserIn().getId().equals("2907657a845-597d-11eb-9cde-2c59e542040c"))) //Марков
            {
                buttonLoadDocs.setEnabled(true);
            }else{
                buttonLoadDocs.setEnabled(false);}

            buttonMainMenu.setEnabled(true);
            buttonExportDb.setEnabled(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


}
