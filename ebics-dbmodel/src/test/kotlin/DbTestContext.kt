import org.ebics.client.api.bank.BankRepository
import org.ebics.client.api.bank.BankService
import org.ebics.client.api.partner.PartnerRepository
import org.ebics.client.api.partner.PartnerService
import org.ebics.client.api.trace.FileService
import org.ebics.client.api.trace.IFileService
import org.ebics.client.api.trace.TraceRepository
import org.ebics.client.api.bankconnection.BankConnectionRepository
import org.ebics.client.api.bankconnection.BankConnectionServiceImpl
import org.ebics.client.api.bankconnection.cert.UserKeyStoreRepository
import org.ebics.client.api.bankconnection.cert.UserKeyStoreService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Lazy
@Configuration
@EnableJpaRepositories(basePackages = ["org.ebics.client.api.*"])
@EntityScan(basePackages = ["org.ebics.client.api.*"])
open class DbTestContext(
    @Autowired val bankRepository: BankRepository,
    @Autowired val partnerRepository: PartnerRepository,
    @Autowired val userKeyStoreRepository: UserKeyStoreRepository,
    @Autowired val userRepository: BankConnectionRepository,
    @Autowired val traceRepository: TraceRepository,
) {
    @Bean
    open fun bankService() = BankService(bankRepository)

    @Bean
    open fun partnerService() = PartnerService(partnerRepository, bankService())

    @Bean
    open fun userKeyStoreService() = UserKeyStoreService(userKeyStoreRepository)

    @Bean
    open fun userService() = BankConnectionServiceImpl(userRepository, partnerService(), userKeyStoreService())

    @Bean
    open fun fileService(): IFileService = FileService(traceRepository, 10)
}