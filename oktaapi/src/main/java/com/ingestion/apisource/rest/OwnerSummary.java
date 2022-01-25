package com.ingestion.apisource.rest;

import java.util.ArrayList;
import java.util.List;



public class OwnerSummary {

    private List<ApplicationSummary> applicationSummary;

    public List<ApplicationSummary> getApplicationSummary() {
        if (applicationSummary == null) {
            applicationSummary = new ArrayList<ApplicationSummary>();
        }
        return this.applicationSummary;
    }

}
