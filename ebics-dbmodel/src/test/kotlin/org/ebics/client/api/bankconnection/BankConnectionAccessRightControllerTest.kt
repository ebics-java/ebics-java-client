package org.ebics.client.api.bankconnection

import org.ebics.client.api.bankconnection.permission.BankConnectionAccessRightsController
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
class BankConnectionAccessRightControllerTest {


    @Test
    @WithMockUser(username = "admin", roles = ["ADMIN", "USER"])
    fun testBankConnectionAdminPermission() {
        val bcPermissions = object : BankConnectionAccessRightsController {
            override fun getCreatorName(): String = "xxx"
            override fun isGuestAccess(): Boolean = false
            override fun getObjectName(): String = "Test bank connection created by ${getCreatorName()}, guest access=${isGuestAccess()}"
        }
        bcPermissions.checkReadAccess()
        bcPermissions.checkWriteAccess()
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkUseAccess()
        }
    }

    @Test
    @WithMockUser(username = "user_xxx", roles = ["USER"])
    fun testBankConnectionReadOwnerUserPermission() {
        val bcPermissions = object : BankConnectionAccessRightsController {
            override fun getCreatorName(): String = "user_xxx"
            override fun isGuestAccess(): Boolean = false
            override fun getObjectName(): String = "Test bank connection created by ${getCreatorName()}, guest access=${isGuestAccess()}"
        }
        bcPermissions.checkReadAccess()
        bcPermissions.checkWriteAccess()
        bcPermissions.checkUseAccess()
    }

    @Test
    @WithMockUser(username = "user_abc", roles = ["USER"])
    fun testBankConnectionReadOtherUserPermission() {
        val bcPermissions = object : BankConnectionAccessRightsController {
            override fun getCreatorName(): String = "other_user"
            override fun isGuestAccess(): Boolean = false
            override fun getObjectName(): String = "Test bank connection created by ${getCreatorName()}, guest access=${isGuestAccess()}"
        }
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkReadAccess()
        }
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkWriteAccess()
        }
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkUseAccess()
        }
    }

    @Test
    @WithMockUser(username = "abc", roles = ["GUEST"])
    fun testBankConnectionReadUserGuestPermission() {
        val bcPermissions = object : BankConnectionAccessRightsController {
            override fun getCreatorName(): String = "xxx"
            override fun isGuestAccess(): Boolean = true
            override fun getObjectName(): String = "Test bank connection created by ${getCreatorName()}, guest access=${isGuestAccess()}"
        }
        bcPermissions.checkReadAccess()
        bcPermissions.checkUseAccess()
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkWriteAccess()
        }
    }

    @Test
    @WithMockUser(username = "abc", roles = ["WRONG_ROLE_XXX"])
    fun testBankConnectionReadUserWithoutAnyRolePermission() {
        val bcPermissions = object : BankConnectionAccessRightsController {
            override fun getCreatorName(): String = "xxx"
            override fun isGuestAccess(): Boolean = true
            override fun getObjectName(): String = "Test bank connection created by ${getCreatorName()}, guest access=${isGuestAccess()}"
        }
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkReadAccess()
        }
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkUseAccess()
        }
        Assertions.assertThrows(IllegalAccessException::class.java) {
            bcPermissions.checkWriteAccess()
        }
    }
}