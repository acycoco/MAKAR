package com.example.makar.data;

public class SubRouteItem {
    private SubRoute subRoute;
    private TransferInfo transferInfo;

    public SubRouteItem() {
    }

    public SubRouteItem(SubRoute subRoute) {
        this.subRoute = subRoute;
    }

    public SubRouteItem(SubRoute subRoute, TransferInfo transferInfo) {
        this.subRoute = subRoute;
        this.transferInfo = transferInfo;
    }

    public TransferInfo getTransferInfo() {
        return transferInfo;
    }

    public SubRoute getSubRoute() {
        return subRoute;
    }

    public void setTransferInfo(TransferInfo transferInfo) {
        this.transferInfo = transferInfo;
    }

    @Override
    public String toString() {
        return "SubRouteItem{" +
                "subRoute=" + subRoute +
                ", transferInfo=" + transferInfo +
                '}';
    }
}

