package com.ingestion.apisource.rest;



public class Attribute {

    private String attributeName;
    private Values values;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String value) {
        this.attributeName = value;
    }

    public Values getValues() {
        return values;
    }

    public void setValues(Values value) {
        this.values = value;
    }

}
