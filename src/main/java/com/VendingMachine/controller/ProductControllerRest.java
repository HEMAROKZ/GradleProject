package com.VendingMachine.controller;

import com.VendingMachine.dto.InventoryDTO;
import com.VendingMachine.dto.PurchaseInputDTO;
import com.VendingMachine.dto.PurchaseResult;
import com.VendingMachine.dto.controllerDTO.DenominationType;
import com.VendingMachine.dto.controllerDTO.MultiplePurchaseInputDTO;
import com.VendingMachine.dto.controllerDTO.PurchaseDTO;
import com.VendingMachine.dto.controllerDTO.PurchaseRequest;
import com.VendingMachine.model.Inventry;
import com.VendingMachine.service.DenominationService;
import com.VendingMachine.service.InventoryService;
import com.VendingMachine.util.DenominationConfig;
import com.VendingMachine.util.FileUtility;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin()
public class ProductControllerRest {

    private final InventoryService inventoryService;
    private final DenominationService denominationService;
    @Autowired
    FileUtility fileUtility;

    @Value("${bill.relativePath}")
    private   String relativePath;

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    public ProductControllerRest(final InventoryService inventoryService, final DenominationService denominationService) {
        this.inventoryService = inventoryService;
        this.denominationService = denominationService;

    }

    @GetMapping("/productRest/{id}")
    @Operation(summary = "CUSTOMER PROCESS--Get Inventory item by id")
    @ResponseBody
    public Inventry getProductById(@PathVariable final int id) {
        return inventoryService.getOnlyInventryProductById(id);
    }


    @GetMapping("/getAllInventoryRest")
    @Operation(summary = "CUSTOMER PROCESS--Get LIST OF ALL Inventory item")
    @ResponseBody
    public List<InventoryDTO> getListOfAllInventoryRest() {
        return inventoryService.getListOfAllInventory();
    }


    @PostMapping( "/multiplePurchaseOfProductFinalRest")
    @ResponseBody
    public ResponseEntity<String> addMultipledDenominationForProductRest(@RequestBody PurchaseRequest purchaseRequest) {
        PurchaseDTO purchaseDTO = purchaseRequest.getPurchaseDTO();
        DenominationConfig denominationConfig = purchaseRequest.getDenominationConfig();
        int billingCounter = purchaseRequest.getBillingCounter();
        MultiplePurchaseInputDTO multiplePurchaseDTO = inventoryService.constructMultiplePurchaseDTO(purchaseDTO);

        // Continue with the rest of your logic...

        List<PurchaseInputDTO> responseList = inventoryService.finalPurchaseRequest(
                multiplePurchaseDTO.getProductIds(),
                multiplePurchaseDTO.getQuantities(),
                multiplePurchaseDTO.getPrices(),
                multiplePurchaseDTO.getCountsOfProduct(),
                multiplePurchaseDTO.getNames()
        );
        Map<DenominationType, Integer> denominationMap = denominationConfig.getDenominationValues();

      //  List<PurchaseInputDTO> responseList = inventoryService.finalPurchaseRequest(multiplePurchaseDTO.getProductIds(), multiplePurchaseDTO.getQuantities(), multiplePurchaseDTO.getPrices(), multiplePurchaseDTO.getCountsOfProduct(), multiplePurchaseDTO.getNames());
//        List<PurchaseInputDTO> responseList = inventoryService.finalPurchaseRequest(multiplePurchaseInputDTO.getProductIds(), multiplePurchaseInputDTO.getQuantities(), multiplePurchaseInputDTO.getPrices(), multiplePurchaseInputDTO.getCountsOfProduct(), multiplePurchaseInputDTO.getNames());

        int totalCost = inventoryService.calculateTotalPrice(responseList);
        log.info("total cost of product" + totalCost);
        PurchaseResult result = inventoryService.multiplePurchaseProduct(responseList, totalCost, denominationMap, billingCounter);
        denominationService.addUpdateDenominationCounts(denominationMap, totalCost);

        // method to Generate bill content
        String billContent = FileUtility.generateBillContent(result);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String fileName = "Bill_" + now.format(formatter) + ".txt";
        // Save the bill as a text file in the specified directory
        FileUtility.generateBill(relativePath, fileName, billContent);

        return ResponseEntity.status(HttpStatus.OK)
                .body("Bill generated successfully. FileName: " + billContent); // You can modify this response as needed
    }
}


