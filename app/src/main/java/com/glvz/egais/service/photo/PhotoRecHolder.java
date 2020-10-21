package com.glvz.egais.service.photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.glvz.egais.R;
import com.glvz.egais.model.BaseRec;
import com.glvz.egais.model.BaseRecContent;
import com.glvz.egais.model.photo.PhotoRec;
import com.glvz.egais.service.DocRecHolder;
import com.glvz.egais.utils.StringUtils;

public class PhotoRecHolder extends DocRecHolder {

    CheckBox chkOtm;
    ImageView imPhotoMini;

    public PhotoRecHolder(View v) {
        super(v);
        //this.chkOtm = (CheckBox) v.findViewById(R.id.chkOtm);
        this.imPhotoMini = (ImageView) v.findViewById(R.id.imPhotoMini);
    }

    @Override
    public void setItem(BaseRec rec) {
//        super.setItem(rec);
        PhotoRec photoRec = (PhotoRec)rec;
        //this.tvComment.setText(writeoffRec.getComment());
        byte[] decodedString = Base64.decode(photoRec.getDataMini(), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        imPhotoMini.setImageBitmap(decodedByte);
        tvDatenakl.setText(photoRec.getDocIdForExport());
    }

}

