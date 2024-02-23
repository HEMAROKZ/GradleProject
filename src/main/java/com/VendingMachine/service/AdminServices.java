package com.VendingMachine.service;

import com.VendingMachine.customeexception.ProductAlreadyExist;
import com.VendingMachine.customeexception.ProductIdNotFoundException;
import com.VendingMachine.customeexception.ProductUnavialableException;
import com.VendingMachine.dao.InitialBalanceDAO;
import com.VendingMachine.dao.InitialBalanceDAOImp;
import com.VendingMachine.dao.InventoryDAO;
import com.VendingMachine.dto.InventoryDTO;
import com.VendingMachine.model.InitialBalanceAndPurchaseHistory;
import com.VendingMachine.model.Inventry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServices {


    public InventoryDAO repository;
    public InitialBalanceDAO initialBalanceDAOImp;

    private static final Logger log = LoggerFactory.getLogger(AdminServices.class);

    @Autowired
    public AdminServices(InventoryDAO repository, InitialBalanceDAO initialBalanceDAOImp) {
        this.repository = repository;
        this.initialBalanceDAOImp = initialBalanceDAOImp;
    }

    public AdminServices() {

    }

public int saveInventory(final InventoryDTO inventry) {
    int productId = inventry.getProductId();

    if (productId != 0) {
        log.info("Inside update inventory: {}", inventry);
        log.warn("product id == " + productId);

        List<Inventry> allInventory = repository.findAll();
        log.info(allInventory + "allInventory==== ");

        boolean productExists = allInventory.stream()
                .anyMatch(existingProduct -> existingProduct.getProductId() == productId);

        if (!productExists) {
            return repository.save(inventry);
        } else {
            throw new ProductAlreadyExist("Product ID: " + productId + " already exists in the inventory. Try with a new product ID.");
        }
    } else {
        throw new ProductIdNotFoundException("Invalid product ID given");
    }
}



////////////////////////////////////////////////////////////
public int updateInventory(final Inventry inventry) {
    int productId = inventry.getProductId();

    if (productId != 0) {
        log.info("Inside update inventory: {}", inventry);
        log.warn("product id == " + productId);

        List<Inventry> allInventory = repository.findAll();
        log.info(allInventory + "allInventory==== ");

        boolean productExists = allInventory.stream()
                .anyMatch(existingProduct -> existingProduct.getProductId() == productId);

        if (productExists) {
            return repository.update(inventry);
        } else {
            throw new ProductUnavialableException("Invalid product ID provided for update: " + productId);
        }
    } else {
        throw new ProductIdNotFoundException("Invalid product ID given");
    }
}

    public int deleteProductById(int productId){
       List<Inventry> value= repository.findById(productId);
       log.info("value=="+value);
        if (value.isEmpty()) {
            throw new ProductUnavialableException("Invalid product ID provided for Delete: " + productId);
        }
        return repository.deleteById(productId);
    }
    public List<InitialBalanceAndPurchaseHistory> getListOfAllPurchaseHistory() {
        return allProductToPurchaseHistory(initialBalanceDAOImp.getAllPurchaseHistory());
    }

    public List<InitialBalanceAndPurchaseHistory> allProductToPurchaseHistory(final List<InitialBalanceAndPurchaseHistory> allInvProduct) {
        return allInvProduct.stream().map(this::productToUserProductHistory).collect(Collectors.toList());
    }
//
    public  InitialBalanceAndPurchaseHistory productToUserProductHistory(final InitialBalanceAndPurchaseHistory inventory) {
        return new InitialBalanceAndPurchaseHistory(
                inventory.getId(),
                inventory.getOrder_id(),
                inventory.getOrder_time(),
                inventory.getCustomerInputAmount(),
                inventory.getBalanceAmount(),
                inventory.getVendingMachineBalance()
        );
    }
}
