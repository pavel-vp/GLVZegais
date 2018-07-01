package com.glvz.egais.dao;

import com.glvz.egais.integration.model.NomenIn;
import com.glvz.egais.integration.model.PostIn;

public interface Dictionary {

    NomenIn findNomenByBarcode(String s);

    PostIn findPostById(String s);

    NomenIn findNomenById(String id1c);
}
