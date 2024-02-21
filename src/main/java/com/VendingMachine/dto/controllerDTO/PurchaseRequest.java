package com.VendingMachine.dto.controllerDTO;


import com.VendingMachine.util.DenominationConfig;

public class PurchaseRequest {
   // private MultiplePurchaseDTO multiplePurchaseDTO;

    private DenominationConfig denominationConfig;
    private int billingCounter;
    private PurchaseDTO purchaseDTO;



    public PurchaseDTO getPurchaseDTO() {
        return purchaseDTO;
    }

    public void setPurchaseDTO(PurchaseDTO purchaseDTO) {
        this.purchaseDTO = purchaseDTO;
    }
//
//    public MultiplePurchaseDTO getMultiplePurchaseDTO() {
//        return multiplePurchaseDTO;
//    }
//
//    public void setMultiplePurchaseDTO(MultiplePurchaseDTO multiplePurchaseDTO) {
//        this.multiplePurchaseDTO = multiplePurchaseDTO;
//    }

    public DenominationConfig getDenominationConfig() {
        return denominationConfig;
    }

    public void setDenominationConfig(DenominationConfig denominationConfig) {
        this.denominationConfig = denominationConfig;
    }

    public int getBillingCounter() {
        return billingCounter;
    }

    public void setBillingCounter(int billingCounter) {
        this.billingCounter = billingCounter;
    }

    @Override
    public String toString() {
        return "PurchaseRequest{" +
                "MultiplePurchaseInputDTO=" +   purchaseDTO +
                ", denominationConfig=" + denominationConfig +
                ", billingCounter=" + billingCounter +
                '}';
    }
}
