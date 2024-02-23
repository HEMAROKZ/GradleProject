import com.VendingMachine.dao.DenominationDAO
import com.VendingMachine.dto.controllerDTO.DenominationType
import com.VendingMachine.model.Denomination
import com.VendingMachine.service.DenominationService
import spock.lang.Specification
import spock.lang.Subject

class DenominationSpec extends Specification{

    @Subject
    DenominationService denominationService = new DenominationService()

    def mockRepository = Mock(DenominationDAO)

    def setup() {
        denominationService.denominationRepository = mockRepository
    }

    def "updateDenominationCounts should update the counts for a denomination type"() {
        given:
        def count = 3
        def denominationType = DenominationType.TEN_RUPEE

        and:
        def existingDenomination = new Denomination(id: 1, denominationType: DenominationType.TEN_RUPEE, count: 5)
        mockRepository.getDenominationByDenominationType(denominationType) >> existingDenomination



        when:
        denominationService.updateDenominationCounts(count, denominationType)

        then:
        1 * mockRepository.getDenominationByDenominationType(DenominationType.TEN_RUPEE) >> existingDenomination
        1 * mockRepository.updateDenomination(existingDenomination) >> _
        existingDenomination.count == 2
    }

    def "getCustomDenominationsFromDatabase should return custom denominations from the database"() {
        given:
        def denominations = [
                new Denomination(id: 1, denominationType: DenominationType.FIFTY_RUPEE, count: 5),
                new Denomination(id: 2, denominationType: DenominationType.TWENTY_RUPEE, count: 10),
                new Denomination(id: 3, denominationType: DenominationType.TEN_RUPEE, count: 8)
        ]
        mockRepository.getAllDenominations() >> denominations

        when:
        def result = denominationService.getCustomDenominationsFromDatabase()

        then:
        result.size() == 3
        result[50] == 5
        result[20] == 10
        result[10] == 8
    }

    def "calculateCustomChangeDenominations should return the correct denomination map"() {
        given:
        def amount = 120
        def customDenominations = [50: 5, 20: 10, 10: 8]
        def resultAmount=[50: 2, 20: 1, 10: 0]
        when:
        def result = denominationService.calculateCustomChangeDenominations(amount, customDenominations)

        then:
        // Verify that the result is a map
        result instanceof Map

        // Assert specific values in the result map
        result[50] == 2 // Two notes of 50
        result[20] == 1 // One note of 20
        result[10] == 0 // Two notes of 10
        result==resultAmount;
        }


}
