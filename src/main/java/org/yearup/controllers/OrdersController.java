package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.OrderDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.ShoppingCart;

import java.security.Principal;

@RestController
@RequestMapping("orders")
@CrossOrigin
@PreAuthorize("hasRole('USER')")
public class OrdersController {
    private final OrderDao orderDao;
    private final UserDao userDao;
    private final ShoppingCartDao shoppingCartDao;

    public OrdersController(OrderDao orderDao, UserDao userDao, ShoppingCartDao shoppingCartDao) {
        this.orderDao = orderDao;
        this.userDao = userDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    @Autowired


    @PostMapping
    public void checkout(@RequestBody Order order, Principal principal) {
        int userId = userDao.getByUserName(principal.getName()).getId();
        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(userId);

        orderDao.checkout(userId, order, shoppingCart);

    }
}
