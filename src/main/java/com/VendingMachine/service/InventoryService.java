package com.VendingMachine.service;

import com.VendingMachine.customeexception.*;
import com.VendingMachine.dao.InitialBalanceDAO;
import com.VendingMachine.dao.InventoryDAO;
import com.VendingMachine.dao.InventoryDAOImp;
import com.VendingMachine.dto.InventoryDTO;
import com.VendingMachine.dto.PurchaseInputDTO;
import com.VendingMachine.dto.PurchaseResult;
import com.VendingMachine.dto.TotalCostResult;
import com.VendingMachine.dto.controllerDTO.DenominationType;
import com.VendingMachine.dto.controllerDTO.MultiplePurchaseInputDTO;
import com.VendingMachine.dto.controllerDTO.PurchaseDTO;
import com.VendingMachine.model.InitialBalanceAndPurchaseHistory;
import com.VendingMachine.model.Inventry;
import com.VendingMachine.model.OrderLine;
import com.VendingMachine.util.TransactionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class InventoryService {

    public InventoryDAOImp repository;
    public  InitialBalanceDAO initialBalanceDAOImp;
    private  DenominationService denominationService;


    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
@Autowired
    public InventoryService(InventoryDAOImp repository, InitialBalanceDAO initialBalanceDAOImp, DenominationService denominationService) {
        this.repository = repository;
        this.initialBalanceDAOImp = initialBalanceDAOImp;
        this.denominationService = denominationService;
    }

    public InventoryService() {

    }


    ///////////////////////////////////// here used constructor injection instead of autowired injection


    public List<InventoryDTO> getListOfAllInventory() {
        return allProductToUserInventory(repository.findAll());
    }


    ///////////////////////////////////////////////////
    public List<InventoryDTO> allProductToUserInventory(List<Inventry> allInvProduct) {
        return allInvProduct.stream()
                .map(this::productToUserProduct)
                .collect(Collectors.toList());
    }
//////////////////////////////////////////////////////
    public InventoryDTO getProductById(int productId) {
        List<Inventry> product = repository.findById(productId);
        Inventry selectedProduct = product.stream()
                .findFirst()
                .orElseThrow(() -> new ProductIdNotFoundException("Invalid product id given in URL....!!!!"));

        if (selectedProduct.getProductInventoryCount() < 1) {
            throw new ProductUnavialableException(selectedProduct.getName() + " is Out of Stock..!!");
        }

        return productToUserProduct(selectedProduct);
    }
    public InventoryDTO productToUserProduct(Inventry inventory) {
        return new InventoryDTO(inventory.getProductId(), inventory.getName(), inventory.getProductPrice(), inventory.getProductInventoryCount());
    }
//////////////////////////////////////////////////////
    public Inventry getInventryProductById(int productId) {
        if (repository.findById(productId).isEmpty()) {  ///check this  for null pointer exception
            throw new ProductIdNotFoundException("invalid product id given in url ....!!!!");
        } else if (repository.findById(productId).get(0).getProductInventoryCount() < 1) {
            throw new ProductUnavialableException(repository.findById(productId).get(0).getName() + " is Out of Stock..!!");
        }
        return repository.findById(productId).get(0);
    }

    public Inventry getOnlyInventryProductById(int productId) {
        try {
            return repository.findById(productId).get(0);

        }catch (IndexOutOfBoundsException  |    ProductIdNotFoundException | NullPointerException e ){
            throw new ProductIdNotFoundException("product id not found");
        }
    }

    ////////////////////////

//will return individualCost for the product that we opt for purchase
    public PurchaseResult multiplePurchaseProduct(final List<PurchaseInputDTO> purchaseInputDTO, int totalCost, Map<DenominationType, Integer> inputDenominationMap, int billingCounter) {

        var change=0;

        var inputAmount = denominationService.totalDenominationAmount(inputDenominationMap, totalCost);
        var vendingMachineBalance = initialBalanceDAOImp.getChange().getVendingMachineBalance();

        if(inputAmount>=totalCost ){
            change = inputAmount - totalCost;
        }

        else
        {
            throw new InsufficientInputCashException("total denomination value is less than the total cost of the product you r trying to purchase");
        }

        if (vendingMachineBalance< change) {
            throw new InsufficientInitialBalanceException("Sorry No Change!!");
        }

        var newInitialBalance = vendingMachineBalance - change;

        Map<Integer, Integer> customDenominations = denominationService.getCustomDenominationsFromDatabase();

        Map<Integer, Integer> denominationMap = denominationService.calculateCustomChangeDenominations(change, customDenominations);

        if (!denominationService.isExactChangeAvailable(change, denominationMap)) {
            throw new NoExactChangeException("No exact change available. Please provide the exact amount.");
        }

        denominationMap.forEach((denomination, count) -> {
            if (count > 0) {
                log.info(denomination + " Rupees: " + count + " notes/coins");
            }
        });

        List<Map<String, Object>> resultList = new ArrayList<>();
        int transactionId = TransactionIdGenerator.generateTransactionId();
      //  StringBuilder productIdsBuilder = new StringBuilder();
        /////////////
        var line_numberCount=0;
        ///////////////
        for (PurchaseInputDTO purchaseInputDTOList : purchaseInputDTO) {
            var productId = purchaseInputDTOList.getProductId();
            var price = purchaseInputDTOList.getPrice();
            var countOfProduct = purchaseInputDTOList.getCountOfProduct(); // inventory product count
            var quantity = purchaseInputDTOList.getQuantity();
            var name = purchaseInputDTOList.getName();
            var line_number=++line_numberCount;
            OrderLine orderLine=new OrderLine(transactionId,line_number,productId );
            repository.save_orderDetails(orderLine);

            // Calling update stock method from the InventoryDAOImplementation class
            repository.updatedStock(productId, countOfProduct - quantity);
            // Add the result for each item to the list
            Map<String, Object> result = new HashMap<>();
            result.put("name", name);
            result.put("price", price);
            result.put("quantity", quantity);
            resultList.add(result);
        }
        log.info("resultlist ================================" +resultList);
        //Create table order_line_merged if it doesn't exist
       repository. createOrderLineMergedTable();

        // Merge rows in order_line and insert into order_line_merged
        repository.mergeOrderLineRows();

        //  Delete rows with order_id zero if they exist
        repository.deleteRowsWithOrderIdZero();

        InitialBalanceAndPurchaseHistory currentTransaction = new InitialBalanceAndPurchaseHistory(0, transactionId,  LocalDateTime.now(), inputAmount, change, newInitialBalance);
//        OrderLine orderLine=new OrderLine(transactionId,billingCounter, productIdsBuilder);
        initialBalanceDAOImp.saveTransaction(currentTransaction, inputAmount);
        // Update denomination counts in the database
        denominationService.updateDenominationCounts(denominationMap); ////////working on this method
//         repository.save_orderDetails(orderLine);
        return new PurchaseResult(change, denominationMap, resultList);
    }


    //method to calculate the total price of purchased products from the list
    public int calculateTotalPrice(final List<PurchaseInputDTO> purchaseInputDTO) {
        int totalPrice = 0;
        for (PurchaseInputDTO inputDTO : purchaseInputDTO) {
            totalPrice += inputDTO.getPrice() * inputDTO.getQuantity();
        }
        return totalPrice;
    }
    public TotalCostResult processPurchaseRequest(List<Integer> productIds,
                                                  List<Integer> quantities,
                                                  List<Integer> prices,
                                                  List<Integer> countsOfProduct,
                                                  List<String> names) {
         AtomicInteger totalCost = new AtomicInteger();

          List<PurchaseInputDTO> responseList = IntStream.range(0, quantities.size())
            .filter(i -> {
                Integer quantity = quantities.get(i);
                return quantity != null && quantity > 0;
            })
            .mapToObj(i -> {
                int productId = productIds.get(i);
                int price = prices.get(i);
                int countOfProduct = countsOfProduct.get(i);
                String name = names.get(i);

                PurchaseInputDTO purchaseInputDTO = PurchaseInputDTO.builder()
                        .withProductId(productId)
                        .withPrice(price)
                        .withCountOfProduct(countOfProduct)
                        .withQuantity(quantities.get(i))
                        .withName(name)
                        .build();

                int individualCost = productCostCalculation(purchaseInputDTO);
                totalCost.addAndGet(individualCost);
                return purchaseInputDTO;
            })
            .collect(Collectors.toList());

          return new TotalCostResult(responseList, totalCost.get());
}


    public  List<PurchaseInputDTO> finalPurchaseRequest(List<Integer> productIds,
                                                          List<Integer> quantities,
                                                          List<Integer> prices,
                                                          List<Integer> countsOfProduct,
                                                          List<String> names) {

        List<PurchaseInputDTO> responseList = IntStream.range(0, productIds.size())
                .mapToObj(i -> PurchaseInputDTO.builder()
                        .withProductId(productIds.get(i))
                        .withPrice(prices.get(i))
                        .withCountOfProduct(countsOfProduct.get(i))
                        .withQuantity(quantities.get(i))
                        .withName(names.get(i))
                        .build())
                .collect(Collectors.toList());
        log.info("++++++++++++responseList++++++++++++++++++++== "+responseList);
        return responseList;
    }

    public int productCostCalculation(final PurchaseInputDTO purchaseInputDTOList) {
        return purchaseInputDTOList.getPrice() * purchaseInputDTOList.getQuantity();
    }


    public MultiplePurchaseInputDTO constructMultiplePurchaseDTO(PurchaseDTO purchaseDTO) {
        // Validate the existence of purchaseDTO
        if (purchaseDTO == null) {
            throw new ProductUnavialableException("no product Id or quantity details were provided");
        }

        List<Integer> productIds = purchaseDTO.getProductId();
        List<Integer> quantities = purchaseDTO.getQuantity();
        log.info("productIds================ " + productIds);
        log.info("quantities================ " + quantities);

        // Validate the existence of productIds and quantities
        if (productIds == null || quantities == null || productIds.size() != quantities.size()) {
            throw new ProductUnavialableException("INVALID product Id or quantity details were provided");
        }

        // Initialize lists to store data for MultiplePurchaseDTO
        List<Integer> productPrices = new ArrayList<>();
        List<Integer> countsOfProduct = new ArrayList<>();
        List<String> names = new ArrayList<>();

        // Fetch additional data for each productId from the inventoryService
        for (int i = 0; i < productIds.size(); i++) {
            int productId = productIds.get(i);
            log.info("productId inside loop ===== " + productId);
            int quantity = quantities.get(i);

            List<Inventry> inventoryList = repository.findAll();

            // Assuming you have a method to find Inventry by productId from the list
            Optional<Inventry> optionalInventry = inventoryList.stream()
                    .filter(inventry -> inventry.getProductId() == productId)
                    .findFirst();

            // Validate the existence of Inventry
            if (optionalInventry.isPresent()) {
                Inventry inventoryDTO = optionalInventry.get();

                // Populate lists with additional data
                productPrices.add(inventoryDTO.getProductPrice());
                countsOfProduct.add(inventoryDTO.getProductInventoryCount());
                names.add(inventoryDTO.getName());
            } else {
                throw new ProductUnavialableException("PRODUCT IS NOT AVIALIABLE");
            }
        }
        return new MultiplePurchaseInputDTO(productIds, quantities, productPrices, countsOfProduct, names);
    }



}
