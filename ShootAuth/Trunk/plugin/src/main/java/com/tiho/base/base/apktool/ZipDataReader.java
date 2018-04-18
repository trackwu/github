package com.tiho.base.base.apktool;

import java.util.Map;

public class ZipDataReader implements DataReader {

    private Map<String, String> mMap;

    ZipDataReader(Map<String, String> map) {
        mMap = map;
    }

    @Override
    public String getData(String key) {
        return mMap.get(key);
    }
}
