package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("products")
@CrossOrigin
public class ProductsController {
    private final ProductDao productDao;

    @Autowired
    public ProductsController(ProductDao productDao) {
        this.productDao = productDao;
    }

    @GetMapping("")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<Product>> search(@RequestParam(name = "cat", required = false) Integer categoryId,
                                                @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
                                                @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
                                                @RequestParam(name = "subCategory", required = false) String subCategory
    ) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) { // in case font is dumb
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "minPrice cannot be greater than maxPrice"
            );
        }

        List<Product> products = productDao.search(categoryId, minPrice, maxPrice, subCategory);

        if (products.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204
        }

        return ResponseEntity.ok(products);
    }

    @GetMapping("{id}")
    @PreAuthorize("permitAll()")
    public Product getById(@PathVariable int id) {
        var product = productDao.getById(id);

        if (product == null)
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product with ID " + id + " not found");

        return product;
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product saved = productDao.create(product);

        return ResponseEntity
                .created(URI.create("/products/" + saved.getProductId()))
                .body(saved);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void updateProduct(@PathVariable int id, @RequestBody Product product) {

        productDao.update(id, product);

    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable int id) {
        var product = productDao.getById(id);

        if (product == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product with ID " + id + " not found"
            );
        }

        productDao.delete(id);
        return ResponseEntity.noContent().build(); // 204

    }
}
