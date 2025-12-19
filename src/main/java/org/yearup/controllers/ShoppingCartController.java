package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("cart")
@CrossOrigin
@PreAuthorize("hasRole('USER')")
public class ShoppingCartController {

    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;
    private final ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    @GetMapping
    public ShoppingCart getCart(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }

        var user = userDao.getByUserName(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        ShoppingCart shoppingCart = shoppingCartDao.getByUserId(user.getId());
        if (shoppingCart == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Shopping cart not found");
        }

        return shoppingCart;
    }

    @PostMapping("/products/{productId}")
    public ResponseEntity<ShoppingCart> addToCart(@PathVariable int productId, Principal principal) {
        var user = userDao.getByUserName(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        var product = productDao.getById(productId);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product with ID " + productId + " not found");
        }

        ShoppingCart cart = shoppingCartDao.getByUserId(user.getId());

        boolean isNewItem = !cart.contains(productId);

        cart = shoppingCartDao.addToCart(productId, user.getId());

        ShoppingCartItem cartItem = cart.get(productId);

        if (isNewItem) {
            return ResponseEntity.created(URI.create("/cart/products/" + productId)).body(cart);
        } else {
            return ResponseEntity.ok(cart);
        }
    }

    @PutMapping("/products/{productId}")
    public void update(@PathVariable int productId, Principal principal, @RequestBody Map<String, Integer> body) {

        int userId = userDao.getByUserName(principal.getName()).getId();
        int quantity = body.get("quantity");

        shoppingCartDao.updateCartItem(productId, userId, quantity);

    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCart(Principal principal) {
        int userId = userDao.getByUserName(principal.getName()).getId();
        shoppingCartDao.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
