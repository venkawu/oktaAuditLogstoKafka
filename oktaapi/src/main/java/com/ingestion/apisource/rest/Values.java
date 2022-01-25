package com.ingestion.apisource.rest;

import java.util.ArrayList;
import java.util.List;


public class Values {

    private List<Value> value;

    public List<Value> getValue() {
        if (value == null) {
            value = new ArrayList<Value>();
        }
        return this.value;
    }

}
