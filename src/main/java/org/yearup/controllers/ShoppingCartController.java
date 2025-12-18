package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("cart")
@CrossOrigin
@PreAuthorize("hasRole('USER')")
public class ShoppingCartController
{

    private final ShoppingCartDao shoppingCartDao;
    private final UserDao userDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
    }

    @GetMapping
    public ShoppingCart getCart(Principal principal)
    {
        try
        {
            int userId = userDao.getByUserName(principal.getName()).getId();

            return shoppingCartDao.getByUserId(userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }


    @PostMapping("/products/{productId}")
    public ShoppingCart addToCart(@PathVariable int productId, Principal principal) {
        try
        {
            int userId = userDao.getByUserName(principal.getName()).getId();
            return shoppingCartDao.addToCart(productId, userId);
        }
        catch(Exception e)
        {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }

    @PutMapping("/products/{productId}")
    public void update(@PathVariable int productId, Principal principal, @RequestBody Map<String, Integer> body) {
        try{
            int userId = userDao.getByUserName(principal.getName()).getId();
            int quantity = body.get("quantity");

            shoppingCartDao.updateCartItem(productId, userId, quantity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @DeleteMapping
    public void deleteCart(Principal principal)
    {
        int userId = userDao.getByUserName(principal.getName()).getId();
        shoppingCartDao.clearCart(userId);
    }

}
