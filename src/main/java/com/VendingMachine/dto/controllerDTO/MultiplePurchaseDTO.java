package com.VendingMachine.dto.controllerDTO;


import java.util.List;

public class MultiplePurchaseDTO {

    private final List<Integer> productIds;
    private final List<Integer> quantities;
    private final List<Integer> prices;
    private final List<Integer> countsOfProduct;
    private final List<String> names;
    private final int totalCost;

    // Constructor to initialize the final fields
    public MultiplePurchaseDTO(
          List<Integer> productIds, List<Integer> quantities,
            List<Integer> prices, List<Integer> countsOfProduct, List<String> names, int totalCost) {

        this.productIds = productIds;
        this.quantities = quantities;
        this.prices = prices;
        this.countsOfProduct = countsOfProduct;
        this.names = names;
        this.totalCost = totalCost;
    }


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

    public int getTotalCost() {
        return totalCost;
    }
}

