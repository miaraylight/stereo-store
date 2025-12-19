package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.services.OrderService;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("orders")
@CrossOrigin
@PreAuthorize("hasRole('USER')")
public class OrdersController {
    private final UserDao userDao;
    private final OrderService orderService;

    @Autowired
    public OrdersController(UserDao userDao, OrderService orderService) {
        this.userDao = userDao;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Void> checkout(@RequestBody Order order, Principal principal) {
        int userId = userDao.getByUserName(principal.getName()).getId();
        Order createdOrder = orderService.checkout(userId, order);

        return ResponseEntity
                .created(URI.create("/orders/" + createdOrder.getOrderId()))
                .build();
    }
}
