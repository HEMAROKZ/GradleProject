import com.VendingMachine.customeexception.ProductIdNotFoundException
import com.VendingMachine.dao.InventoryDAO
import com.VendingMachine.dto.controllerDTO.MultiplePurchaseInputDTO
import com.VendingMachine.dto.controllerDTO.PurchaseDTO
import com.VendingMachine.model.Inventry
import com.VendingMachine.service.InventoryService
import spock.lang.Specification
import spock.lang.Subject

class InventoryServiceSpec extends Specification {

    @Subject
    InventoryService inventoryService = new InventoryService( )

    def mockRepository = Mock(InventoryDAO)

    def setup() {
        // TO Initialize the mock repository in the service
        inventoryService.repository = mockRepository
    }

    def "getListOFAllInventory should return a list of InventoryDTOs"() {
        given:

        def inventoryList = [
                new Inventry(productId: 1, name: "Product1", productPrice: 10, productInventoryCount: 5),
                new Inventry(productId: 2, name: "Product2", productPrice: 15, productInventoryCount: 8)
        ]
        mockRepository.findAll() >> inventoryList

        when:
        def result = inventoryService.getListOfAllInventory()

        then:
        1 * mockRepository.findAll() >> inventoryList

        and:
        result.size() == 2
        result[0].productId == 1
        result[0].name == "Product1"
        result[0].productPrice == 10
        result[0].productInventoryCount == 5

        and:
        result[1].productId == 2
        result[1].name == "Product2"
        result[1].productPrice == 15
        result[1].productInventoryCount == 8
    }
//////////////////////////////InventoryById//////////////////////////

    def "getInventoryById should return a of InventoryDTOs"() {
        given:
        int productId=1;

        def inventory =
                new Inventry(productId: 1, name: "Product1", productPrice: 10, productInventoryCount: 5)

        mockRepository.findById(productId) >> [inventory]

        when:
        Inventry result = inventoryService.getInventryProductById(productId)

        then:
        result == inventory
    }


    def "should handle ItemNotFoundException when item is not found"() {
        given:

        mockRepository.findById(12) >> []

        when:
        def result
        try {
            result = inventoryService.getOnlyInventryProductById(12)
        } catch (ProductIdNotFoundException  e) {
                result = e.message
        }


        then:
        result == "product id not found"
    }

    ////////////////////////////////////////////////////////

    def "constructMultiplePurchaseDTO should return a MultiplePurchaseInputDTO with valid input"() {
        given:
        PurchaseDTO purchaseDTO = new PurchaseDTO(
                productId: [1,2],
                quantity: [3,4]
        )

            mockRepository.findAll() >> [
                    new Inventry(productId: 1, name: "Product1", productPrice: 10, productInventoryCount: 5),
                    new Inventry(productId: 2, name: "Product2", productPrice: 15, productInventoryCount: 8)
            ]

        when:
        def result = inventoryService.constructMultiplePurchaseDTO(purchaseDTO)

        then:
        result instanceof MultiplePurchaseInputDTO
        result.productIds == [1, 2]
        result.quantities == [3, 4]
        result.prices == [10, 15]
        result.countsOfProduct == [5, 8]
        result.names == ["Product1", "Product2"]

    }

}
