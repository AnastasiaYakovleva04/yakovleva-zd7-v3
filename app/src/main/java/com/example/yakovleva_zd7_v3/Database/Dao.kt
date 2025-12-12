package com.example.yakovleva_zd7_v3

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    fun login(email: String, password: String): User?

    @Query("SELECT * FROM users WHERE role = 'Клиент'")
    fun getClients(): List<User>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): User?

    @Insert
    fun insert(user: User): Long

    @Update
    fun update(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE clientId = :clientId")
    fun getClientOrders(clientId: Int): List<Order>

    @Insert
    fun createOrder(order: Order): Long

    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<Order>

    @Query("SELECT * FROM orders WHERE status = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: String): List<Order>

    @Query("UPDATE orders SET status = :status, assignedWorkerId = :workerId WHERE id = :orderId")
    fun startProduction(orderId: Long, status: String, workerId: Int)

    @Query("UPDATE orders SET status = :status, completedAt = :timestamp WHERE id = :orderId")
    fun completeOrder(orderId: Long, status: String, timestamp: Long)

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderById(orderId: Long): Order?
}

@Dao
interface PartDao {
    @Query("SELECT * FROM parts WHERE supplierId = :supplierId")
    fun getPartsBySupplier(supplierId: Int): List<Part>

    @Insert
    fun insert(part: Part): Long

    @Query("SELECT * FROM parts WHERE id = :partId")
    fun getPartById(partId: Int): Part?

    @Query("SELECT * FROM parts")
    fun getAllParts(): List<Part>

    @Update
    fun update(part: Part)

    @Delete
    fun delete(part: Part)
}

@Dao
interface OrderItemDao {
    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getItemsForOrder(orderId: Long): List<OrderItem>

    @Insert
    fun insert(orderItem: OrderItem): Long

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    fun deleteByOrderId(orderId: Long)
}

@Dao
interface OrderPartDao {
    @Query("SELECT * FROM order_part WHERE orderId = :orderId")
    fun getPartsForOrder(orderId: Long): List<OrderPart>

    @Insert
    fun addPartToOrder(orderPart: OrderPart)

    @Query("UPDATE order_part SET used = true WHERE orderId = :orderId AND partId = :partId")
    fun markPartUsed(orderId: Long, partId: Int)

    @Query("SELECT * FROM order_part")
    fun getAllOrderParts(): List<OrderPart>

    @Query("SELECT * FROM order_part WHERE orderId = :orderId AND used = false")
    fun getUnusedPartsForOrder(orderId: Long): List<OrderPart>

    @Query("UPDATE order_part SET used = true WHERE orderId = :orderId")
    fun markAllPartsUsed(orderId: Long)

    @Query("DELETE FROM order_part WHERE orderId = :orderId AND partId = :partId")
    fun removePartFromOrder(orderId: Long, partId: Int)

    @Query("UPDATE order_part SET quantity = :quantity WHERE orderId = :orderId AND partId = :partId")
    fun updatePartQuantity(orderId: Long, partId: Int, quantity: Int)
}