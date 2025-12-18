package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

public interface OrderDao {
    Order getByIdWithItems(int orderId);
    int createOrder(Order order, int uderId);
    int createOrderLineItems(Product product, ShoppingCartItem item, int orderId);
}
