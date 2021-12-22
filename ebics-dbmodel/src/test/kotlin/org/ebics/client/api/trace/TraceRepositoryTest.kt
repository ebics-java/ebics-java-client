package org.ebics.client.api.trace

import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.api.trace.orderType.EbicsMessage
import org.ebics.client.api.trace.orderType.EbicsService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.user.BankConnection
import org.ebics.client.api.user.User
import org.ebics.client.api.user.UserServiceImpl
import org.ebics.client.api.user.cert.UserKeyStoreService
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@AutoConfigurationPackage(basePackages = ["org.ebics.client.api.*"])
@ContextConfiguration(classes = [BankService::class, UserKeyStoreService::class, PartnerService::class, UserServiceImpl::class])
class TraceRepositoryTest(
    @Autowired private val userService: UserServiceImpl,
    @Autowired private val bankService: BankService,
    @Autowired private val traceRepository: TraceRepository,
) {
    private fun getMockBank(): Long {
        val bank = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        return bankService.createBank(bank)
    }

    private fun getMockUser(userId: String = "CHT10001", partnerId: String = "CH100001", bankId:Long = getMockBank()): User {
        val userInfo = BankConnection(EbicsVersion.H004, userId, "Jan", partnerId, bankId, false, false)
        val bcId = userService.createUserAndPartner(userInfo)
        return userService.getUserById(bcId)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreator() {
        val mockUser1 = getMockUser()

        traceRepository.save(TraceEntry(null, "test", mockUser1, "jan"))
        val result = traceRepository.findOne(creatorEquals("jan"))
        Assertions.assertTrue(result.isPresent)
        val result2 = traceRepository.findOne(creatorEquals("chosee"))
        Assertions.assertFalse(result2.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByUser() {
        val bankId = getMockBank()
        val mockUser1 = getMockUser("CHT001", "CH1", bankId)
        val mockUser2 = getMockUser("CHT002", "CH1", bankId)
        val mockUser3 = getMockUser("CHT002", "XXXXX", bankId)

        traceRepository.save(TraceEntry(null, "test", mockUser1, "jan"))
        val result = traceRepository.findOne(bankConnectionEquals(mockUser1))
        Assertions.assertTrue(result.isPresent)
        val result2 = traceRepository.findOne(bankConnectionEquals(mockUser2, false))
        Assertions.assertFalse(result2.isPresent)
        val result3 = traceRepository.findOne(bankConnectionEquals(mockUser2, true))
        Assertions.assertTrue(result3.isPresent)
        val result4 = traceRepository.findOne(bankConnectionEquals(mockUser3, true))
        Assertions.assertFalse(result4.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreatorAndOrderType() {
        val mockUser1 = getMockUser()
        traceRepository.save(
            TraceEntry(
                null,
                "test",
                mockUser1,
                "jan",
                orderType = OrderTypeDefinition(EbicsAdminOrderType.HTD)
            )
        )

        val negativeResult =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HAC)))
        Assertions.assertFalse(negativeResult.isPresent)

        val result =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD)))
        Assertions.assertTrue(result.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreatorAndOrderTypeExt() {
        val mockUser1 = getMockUser()
        traceRepository.save(
            TraceEntry(
                null, "test", mockUser1, "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XE2")
            )
        )

        val negativeResult =
            traceRepository.findOne(orderTypeEquals( OrderTypeDefinition(EbicsAdminOrderType.HAC)))
        Assertions.assertFalse(negativeResult.isPresent)

        val negativeResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XXX")))
        Assertions.assertFalse(negativeResult2.isPresent)

        val posResult1 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD)))
        Assertions.assertTrue(posResult1.isPresent)

        val posResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XE2")))
        Assertions.assertTrue(posResult2.isPresent)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun testTrRepoSearchByCreatorAndBtf() {
        val mockUser1 = getMockUser()
        val service = EbicsService("name", "s", "dd", message = EbicsMessage("name1", "ff", "001", "Zip"))
        traceRepository.save(
            TraceEntry(
                null, "test", mockUser1, "jan", orderType =
                OrderTypeDefinition(EbicsAdminOrderType.HTD, service)
            )
        )

        val negativeResult =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HAC)))
        Assertions.assertFalse(negativeResult.isPresent)

        val negativeResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, null, "XXX")))
        Assertions.assertFalse(negativeResult2.isPresent)

        val posResult1 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD)))
        Assertions.assertTrue(posResult1.isPresent)

        val posResult2 =
            traceRepository.findOne(orderTypeEquals(OrderTypeDefinition(EbicsAdminOrderType.HTD, service)))
        Assertions.assertTrue(posResult2.isPresent)
    }
}