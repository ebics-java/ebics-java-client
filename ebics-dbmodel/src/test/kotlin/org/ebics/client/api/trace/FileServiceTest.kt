package org.ebics.client.api.trace

import DbTestContext
import org.ebics.client.api.bank.BankData
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.trace.orderType.OrderTypeDefinition
import org.ebics.client.api.bankconnection.BankConnection
import org.ebics.client.api.bankconnection.BankConnectionEntity
import org.ebics.client.api.bankconnection.BankConnectionServiceImpl
import org.ebics.client.model.EbicsVersion
import org.ebics.client.order.EbicsAdminOrderType
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL

@ExtendWith(SpringExtension::class)
@DataJpaTest
@ContextConfiguration(classes = [DbTestContext::class])
class FileServiceTest(
    @Autowired private val IFileService: IFileService,
    @Autowired private val userService: BankConnectionServiceImpl,
    @Autowired private val bankService: BankService,
) {
    private fun getMockUser(): BankConnectionEntity {
        val bank = BankData(URL("https://ebics.ubs.com/ebicsweb/ebicsweb"), "EBXUBSCH", "UBS-PROD-CH")
        val bankId = bankService.createBank(bank)
        val userInfo = BankConnection(EbicsVersion.H004, "CHT10001", "Jan", "CH100001", bankId, false, false)
        val userId = userService.createUserAndPartner(userInfo)
        return userService.getUserById(userId)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun getLastHtdFile_when_empty_then_exception() {
        Assertions.assertThrows(Exception::class.java) {
            IFileService.getLastDownloadedFile(OrderTypeDefinition(EbicsAdminOrderType.HTD), getMockUser(), ebicsVersion = EbicsVersion.H004)
        }
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun addHtdFile_then_notTrowException() {
        IFileService.addTextFile(getMockUser(), OrderTypeDefinition(EbicsAdminOrderType.HTD), "htdFileContent", "sessId1", "ONHX", EbicsVersion.H005, true)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun addAndGetLastWithAdminOt_then_returnTheAddedFile() {
        val mockUser1 = getMockUser()
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.HTD), "htdFileContent", "sessId1", "ONHX", EbicsVersion.H005, false)
        val fileContent = IFileService.getLastDownloadedFile(OrderTypeDefinition(EbicsAdminOrderType.HTD), mockUser1, EbicsVersion.H005)
        Assertions.assertEquals("htdFileContent", fileContent.messageBody)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun addAndGetLastWithBusinessOt_then_returnTheAddedFile() {
        val mockUser1 = getMockUser()
        val ot = OrderTypeDefinition(EbicsAdminOrderType.UPL, null, "XE2")
        IFileService.addTextFile(mockUser1, ot, "htdFileContent", "sessId1", "ONHX", EbicsVersion.H005, false)
        val fileContent = IFileService.getLastDownloadedFile(ot, mockUser1, EbicsVersion.H005)
        Assertions.assertEquals("htdFileContent", fileContent.messageBody)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun add3AndGetLast_then_returnTheLastAddedFile() {
        val mockUser1 = getMockUser()
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.HTD), "htdFileContent", "sessId1", "ONHX", EbicsVersion.H005, false)
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.HTD), "htdFileContent2", "sessId1", "ONHX", EbicsVersion.H005, false)
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.HTD), "htdFileContent3XXX", "sessId1", "ONHX", EbicsVersion.H005, false)
        val fileContent = IFileService.getLastDownloadedFile(OrderTypeDefinition(EbicsAdminOrderType.HTD), mockUser1, EbicsVersion.H005)
        Assertions.assertEquals("htdFileContent3XXX", fileContent.messageBody)
    }

    @Test
    @WithMockUser(username = "jan", roles = ["USER"])
    fun add3WithDifferentOrderTypesAndGetLast_then_returnFirstItemForGivenOt() {
        val mockUser1 = getMockUser()
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.HTD), "htdFileContent", "sessId1", "ONHX", EbicsVersion.H005, false)
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.H3K), "htdFileContent-H3K", "sessId1", "ONHX", EbicsVersion.H005, false)
        IFileService.addTextFile(mockUser1, OrderTypeDefinition(EbicsAdminOrderType.HAC), "htdFileContent3XXX", "sessId1", "ONHX", EbicsVersion.H005, false)
        val fileContent = IFileService.getLastDownloadedFile(OrderTypeDefinition(EbicsAdminOrderType.H3K), mockUser1, EbicsVersion.H005)
        Assertions.assertEquals("htdFileContent-H3K", fileContent.messageBody)
    }
}