package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCartItem;

public interface OrderDao {
    Order getByIdWithItems(int orderId, int userId);
    int createOrder(Order order, int userId);
    int createOrderLineItems(Product product, ShoppingCartItem item, int orderId);
}
