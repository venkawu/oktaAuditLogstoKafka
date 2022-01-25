package com.ingestion.apisource.rest;



public class EAIAppSummaryRequest {

    private String empId;
    private String filter;
    
    
    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String value) {
        this.empId = value;
    }

    public String getFilter() {
        return filter;
    }


    public void setFilter(String value) {
        this.filter = value;
    }

}
