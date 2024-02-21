package com.VendingMachine.dto.controllerDTO;
import java.util.List;

public class MultiplePurchaseInputDTO {
    private  List<Integer> productIds;
    private  List<Integer> quantities;
    private  List<Integer> prices;
    private  List<Integer> countsOfProduct;
    private  List<String> names;

    public MultiplePurchaseInputDTO(
            List<Integer> productIds,
            List<Integer> quantities,
            List<Integer> prices,
            List<Integer> countsOfProduct,
            List<String> names) {
        this.productIds = productIds;
        this.quantities = quantities;
        this.prices = prices;
        this.countsOfProduct = countsOfProduct;
        this.names = names;
    }

    public MultiplePurchaseInputDTO() {
    }
// Getters

    public List<Integer> getProductIds() {
        return productIds;
    }

    public List<Integer> getQuantities() {
        return quantities;
    }

    public List<Integer> getPrices() {
        return prices;
    }

    public List<Integer> getCountsOfProduct() {
        return countsOfProduct;
    }

    public List<String> getNames() {
        return names;
    }

    @Override
    public String toString() {
        return "MultiplePurchaseInputDTO{" +
                "productIds=" + productIds +
                ", quantities=" + quantities +
                ", prices=" + prices +
                ", countsOfProduct=" + countsOfProduct +
                ", names=" + names +
                '}';
    }
}
