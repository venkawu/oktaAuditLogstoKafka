package com.ingestion.apisource.rest;

import com.ingestion.apisource.rest.LeadSummary;
import com.ingestion.apisource.rest.MaintainerSummary;
import com.ingestion.apisource.rest.ManagerSummary;
import com.ingestion.apisource.rest.OwnerSummary;

public class EAIAppSummaryResponse {

    private LeadSummary leadSummary;
    private MaintainerSummary maintainerSummary;
    private OwnerSummary ownerSummary;
    private ManagerSummary managerSummary;

    public LeadSummary getLeadSummary() {
        return leadSummary;
    }

    public void setLeadSummary(LeadSummary value) {
        this.leadSummary = value;
    }

    public MaintainerSummary getMaintainerSummary() {
        return maintainerSummary;
    }

    public void setMaintainerSummary(MaintainerSummary value) {
        this.maintainerSummary = value;
    }

    public OwnerSummary getOwnerSummary() {
        return ownerSummary;
    }

    public void setOwnerSummary(OwnerSummary value) {
        this.ownerSummary = value;
    }

    public ManagerSummary getManagerSummary() {
        return managerSummary;
    }

    public void setManagerSummary(ManagerSummary value) {
        this.managerSummary = value;
    }


}
