package vickram.tech.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import vickram.tech.db.Categories
import vickram.tech.db.Products
import vickram.tech.db.Users

fun Application.configureDatabases() {
    val config = environment.config
    Database.connect(
        datasource = hikari(
            url = config.property("db.url").getString(),
            password = config.property("db.password").getString(),
            driver = config.property("db.driver").getString(),
            user = config.property("db.user").getString(),
        )
    )
    transaction {
        SchemaUtils.create(Users, Categories, Products)
    }

}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

private fun hikari(
    url: String,
    password: String,
    driver: String,
    user: String
): HikariDataSource {
    val config = HikariConfig().apply {
        this.jdbcUrl = url
        this.username = user
        this.driverClassName = driver
        this.password = password
        this.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    }
    return HikariDataSource(config)
}

