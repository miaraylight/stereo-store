package org.yearup.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Order;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.time.LocalDateTime;

@Service
public class OrderService {
    private final OrderDao orderDao;
    private final ShoppingCartDao shoppingCartDao;
    @Autowired
    public OrderService(OrderDao orderDao, ShoppingCartDao shoppingCartDao) {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    @Transactional
    public Order checkout(int userId, Order order) {
        // 1 get cart for this user
        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

        if (shoppingCart.getItems().isEmpty()) {
            throw new RuntimeException("Shopping cart is empty."); // in case checkout btn will be still there lol
        }

        order.setDate(LocalDateTime.now());

        // 2 create order with data from body adding current time
        int orderId = orderDao.createOrder(order, userId);

        // 3 loop and create order item for products in cart
        for (ShoppingCartItem item: shoppingCart.getItems().values()) {
            orderDao.createOrderLineItems(item.getProduct(), item, orderId);
        }

        // clear the cart
        shoppingCartDao.clearCart(userId);

        return orderDao.getByIdWithItems(orderId, userId);
    }
}
