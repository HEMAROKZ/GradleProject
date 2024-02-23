import com.VendingMachine.dto.InventoryDTO
import com.VendingMachine.service.AdminServices
import com.VendingMachine.service.InventoryService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.lang.Subject

@Subject(AdminServices)
class AdminServicesIntegrationSpec extends Specification {

    @Subject
    AdminServices adminServices


    private static final Logger log = LoggerFactory.getLogger(AdminServicesIntegrationSpec.class)

    def "saveInventory should save new inventory if product ID is valid and not already in the inventory"() {
        given:
        InventoryDTO inventoryDTO = InventoryDTO.builder()
                .withProductId(1)
                .withName("frooti")
                .withProductPrice(23)
                .withProductInventoryCount(11)
                .build()
log.info(inventoryDTO as String)
        when:
        int result = adminServices.saveInventory(inventoryDTO)

        then:
        result == 1
    }
}
