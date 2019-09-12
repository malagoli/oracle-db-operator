package com.oracle;

import io.radanalytics.operator.common.EntityInfo;

public class OracleService extends EntityInfo {

    public String storage;



    public String tempStorage;






    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getTempStorage() {
        return tempStorage;
    }

    public void setTempStorage(String tempStorage) {
        this.tempStorage = tempStorage;
    }

}
