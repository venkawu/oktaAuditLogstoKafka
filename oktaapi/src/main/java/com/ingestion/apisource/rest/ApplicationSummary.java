package com.ingestion.apisource.rest;

import java.util.ArrayList;
import java.util.List;


public class ApplicationSummary {

    private String uniqueId;
    private String name;
    private List<Attribute> attribute;

    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String value) {
        this.uniqueId = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public List<Attribute> getAttribute() {
        if (attribute == null) {
            attribute = new ArrayList<Attribute>();
        }
        return this.attribute;
    }

}
