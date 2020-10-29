package com.foodbox.app.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Checksum {
    @SerializedName("CHECKSUMHASH")
    @Expose
    private String cHECKSUMHASH;
    @SerializedName("ORDER_ID")
    @Expose
    private String oRDERID;
    @SerializedName("payt_STATUS")
    @Expose
    private String paytSTATUS;

    public String getCHECKSUMHASH() {
        return cHECKSUMHASH;
    }

    public void setCHECKSUMHASH(String cHECKSUMHASH) {
        this.cHECKSUMHASH = cHECKSUMHASH;
    }

    public String getORDERID() {
        return oRDERID;
    }

    public void setORDERID(String oRDERID) {
        this.oRDERID = oRDERID;
    }

    public String getPaytSTATUS() {
        return paytSTATUS;
    }

    public void setPaytSTATUS(String paytSTATUS) {
        this.paytSTATUS = paytSTATUS;
    }


}