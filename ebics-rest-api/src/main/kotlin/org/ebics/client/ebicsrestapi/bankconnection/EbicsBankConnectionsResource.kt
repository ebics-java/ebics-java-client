package org.ebics.client.ebicsrestapi.bankconnection

import org.ebics.client.api.user.permission.BankConnectionAccessType
import org.ebics.client.api.user.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("bankconnections")
@CrossOrigin(origins = ["http://localhost:8081"])
class EbicsBankConnectionsResource (
    private val userService: UserServiceImpl)
{
    @GetMapping("")
    fun listBankConnections(@RequestParam(required = false) permission: BankConnectionAccessType = BankConnectionAccessType.READ): List<User> =
        userService.findUsers(permission)

    @GetMapping("{userId}")
    fun getBankConnectionById(@PathVariable userId: Long): User = userService.getUserById(userId)

    @DeleteMapping("{userId}")
    fun deleteBankConnectionById(@PathVariable userId: Long) = userService.deleteUser(userId)

    @PostMapping("")
    fun createBankConnection(@RequestBody bankConnection: BankConnection):Long =
        userService.createUserAndPartner(bankConnection)

    @PutMapping("{userId}")
    fun updateBankConnection(@PathVariable userId:Long, @RequestBody bankConnection: BankConnection) =
        userService.updateUserAndPartner(userId, bankConnection)

    @PostMapping("{userId}/resetStatus")
    fun resetBankConnectionStatus(@PathVariable userId: Long) = userService.resetStatus(userId)
}