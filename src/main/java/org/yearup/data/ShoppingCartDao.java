package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    ShoppingCart addToCart(int productId, int userId);
    void updateCartItem(int productId, int userId, int quantity);
    void clearCart(int userId);
}
