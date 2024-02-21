
import com.VendingMachine.customeexception.ProductAlreadyExist
import com.VendingMachine.customeexception.ProductIdNotFoundException
import com.VendingMachine.customeexception.ProductUnavialableException
import com.VendingMachine.dao.InitialBalanceDAOImp
import com.VendingMachine.dao.InventoryDAOImp
import com.VendingMachine.dto.InventoryDTO
import com.VendingMachine.model.InitialBalanceAndPurchaseHistory
import com.VendingMachine.model.Inventry
import com.VendingMachine.service.AdminServices
import org.mockito.Mock
import spock.lang.Specification
import spock.lang.Subject
import java.time.LocalDateTime;


@Subject(AdminServices)
class AdminSpec extends Specification {

    @Mock
    AdminServices adminServices = new AdminServices()

    def mockInventoryRepository = Mock(InventoryDAOImp)

    def mockInitialBalRepository = Mock(InitialBalanceDAOImp)




    def setup() {
        adminServices.repository = mockInventoryRepository
        adminServices.initialBalanceDAOImp=mockInitialBalRepository
    }

    ////////////////////////////////delete inventory////////////////////////////////

    def "deleteProductById should return 1 for a valid product ID"() {
        given:
        int productId = 1

        // Mocking the repository
        mockInventoryRepository. findById(1) >> [new Inventry(1, "Product A", 10, 25)]
        mockInventoryRepository. deleteById(1) >> 1



        when:
        int result = adminServices.deleteProductById(productId)

        then:
        result == 1
    }

/////////////////////////////////////save method test case with error testing //////////////////////////////////////////////////


    def "saveInventory should save new inventory if product ID is valid and not already in the inventory"() {
        given:
        InventoryDTO inventoryDTO = InventoryDTO.builder()
                .withProductId(1)
                .withName("frooti")
                .withProductPrice(23)
                .withProductInventoryCount(11)
                .build()
        println (inventoryDTO)
        // Mocking the repository behavior for an empty existing inventory
        mockInventoryRepository .findAll() >> []

        // Mocking the save operation
        mockInventoryRepository.save(inventoryDTO) >> 1

        when:
        int result = adminServices.saveInventory(inventoryDTO)

        then:
        result == 1
    }



    def "saveInventory should throw ProductAlreadyExist if product ID already exists in the inventory"() {
        given:
        InventoryDTO inventoryDTO =  InventoryDTO.builder().withProductId(1)
                .withName("frooti")
                .withProductPrice(23)
                .withProductInventoryCount(11)
                .build()
        // Mocking the repository behavior for an existing inventory with the same product ID
        mockInventoryRepository.findAll() >> [new Inventry(productId: 1, name: "frooti", productPrice: 23, productInventoryCount: 11)]

        when:
        adminServices.saveInventory(inventoryDTO)

        then:
        thrown(ProductAlreadyExist)
    }

    def "saveInventory should throw ProductIdNotFoundException if product ID is 0"() {
        given:
        InventoryDTO inventoryDTO = InventoryDTO.builder().withProductId(0)
                .withName("frooti")
                .withProductPrice(23)
                .withProductInventoryCount(11)
                .build()
        when:
        adminServices.saveInventory(inventoryDTO)

        then:
        thrown(ProductIdNotFoundException)
    }
/////////////////////////////////////UPDATE method test case with error testing //////////////////////////////////////////////////


    def "updateInventory should update inventory if product ID exists"() {
        given:
        Inventry inventory = new Inventry(productId: 1, name: "Product A", productPrice: 10, productInventoryCount: 25)

        // Mocking the repository behavior for an  inventory populated above
        mockInventoryRepository.findAll() >> [inventory]
        mockInventoryRepository.update(inventory) >> 1

        when:
        int result = adminServices.updateInventory(inventory)

        then:
        result == 1
    }

    def "updateInventory should throw Product Unavailable Exception if product ID doesn't exist"() {
        given:
        Inventry inventory = new Inventry(productId: 1, name: "Product A", productPrice: 10, productInventoryCount: 25)

        // Mocking the repository behavior for an empty existing inventory
        mockInventoryRepository.findAll() >> []

        when:
        adminServices.updateInventory(inventory)

        then:
        thrown(ProductUnavialableException)
    }

    def "updateInventory should throw ProductIdNotFoundException if product ID is 0"() {
        given:
        Inventry inventory = new Inventry(productId: 0, name: "Product A", productPrice: 10, productInventoryCount: 25)

        when:
        adminServices.updateInventory(inventory)

        then:
        thrown(ProductIdNotFoundException)
    }

    ////////////////////////PurchaseHistory/////////////////////


    def "getListOfAllPurchaseHistory should return a list of InitialBalanceAndPurchaseHistory"() {
        given:
        def mockPurchaseHistory = [
                new InitialBalanceAndPurchaseHistory(id: 1, order_id: 101, order_time: LocalDateTime.now(), customerInputAmount: 50, balanceAmount: 20, vendingMachineBalance: 30),
                new InitialBalanceAndPurchaseHistory(id: 2, order_id: 102, order_time: LocalDateTime.now(), customerInputAmount: 60, balanceAmount: 25, vendingMachineBalance: 35)
        ]

        // Mocking the InitialBalanceDAO behavior for fetching all purchase history
        mockInitialBalRepository.getAllPurchaseHistory() >> mockPurchaseHistory

        when:
        def result = adminServices.getListOfAllPurchaseHistory()

        then:
        1 * mockInitialBalRepository.getAllPurchaseHistory() >> mockPurchaseHistory

        and:
        result.size() == 2
        result[0].id == 1
        result[0].order_id == 101
        result[0].customerInputAmount == 50
        result[0].balanceAmount == 20
        result[0].vendingMachineBalance == 30


        and:
        result[1].id == 2
        result[1].order_id == 102
        result[1].customerInputAmount == 60
        result[1].balanceAmount == 25
        result[1].vendingMachineBalance == 35
    }


}
