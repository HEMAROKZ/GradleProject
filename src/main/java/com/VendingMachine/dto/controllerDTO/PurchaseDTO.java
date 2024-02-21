package com.VendingMachine.dto.controllerDTO;


import java.util.List;

public class PurchaseDTO {
   private  List<Integer> productId;
    private  List<Integer> quantity;



    public PurchaseDTO(List<Integer> productId, List<Integer> quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    public PurchaseDTO(){}


    public List<Integer> getProductId() {
        return productId;
    }

    public void setProductId(List<Integer> productId) {
        this.productId = productId;
    }

    public List<Integer> getQuantity() {
        return quantity;
    }

    public void setQuantity(List<Integer> quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "PurchaseDTO{" +
                "productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
