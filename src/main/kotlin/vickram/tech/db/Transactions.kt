package vickram.tech.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import vickram.tech.models.Transaction
import vickram.tech.utils.PGEnum
import vickram.tech.utils.TRANSACTION_STATUS
import java.util.*

object Transactions: UUIDTable("transactions") {
    val order = reference("order", Orders, onDelete = ReferenceOption.CASCADE)
    val transactionCode = varchar("transaction_code", 250)
    val transactionAmount = varchar("transaction_amount", 250)
    val transactionType = varchar("transaction_type", 250)
    val transactionTime = varchar("transaction_time", 250)
    val transactionStatus = customEnumeration(
        "transaction_status",
        "TRANSACTION_STATUS",
        { value -> TRANSACTION_STATUS.valueOf(value as String) },
        { PGEnum("TRANSACTION_STATUS", it) }
    )

    init {
        uniqueIndex(order, transactionCode)
    }
}

class TransactionEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object : UUIDEntityClass<TransactionEntity>(Transactions)

    var order by OrderEntity referencedOn Transactions.order
    var transactionCode by Transactions.transactionCode
    var transactionAmount by Transactions.transactionAmount
    var transactionType by Transactions.transactionType
    var transactionTime by Transactions.transactionTime
    var transactionStatus by Transactions.transactionStatus

    fun toTransaction() = Transaction(
        transactionId = id.value,
        order = order.id.value,
        transactionCode = transactionCode,
        transactionAmount = transactionAmount,
        transactionType = transactionType,
        transactionTime = transactionTime,
        transactionStatus = transactionStatus
    )
}
