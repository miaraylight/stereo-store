package org.yearup.data.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yearup.models.Product;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MySqlProductDaoTest extends BaseDaoTestClass
{
    private MySqlProductDao dao;

    @BeforeEach
    public void setup()
    {
        dao = new MySqlProductDao(dataSource);
    }

    @Test
    public void getById_shouldReturn_theCorrectProduct()
    {
        // arrange
        int productId = 1;
        Product expected = new Product()
        {{
            setProductId(1);
            setName("The Beatles - Abbey Road Vinyl");
            setPrice(new BigDecimal("29.99"));
            setCategoryId(1);
            setDescription("Classic Beatles album remastered on 180-gram vinyl.");
            setSubCategory("Rock");
            setStock(50);
            setFeatured(true);
            setImageUrl("abbey-road-vinyl.jpg");
        }};

        // act
        var actual = dao.getById(productId);

        // assert
        assertEquals(expected.getPrice(), actual.getPrice(), "Because I tried to get product 1 from the database.");
    }

    @Test
    void search_byCategoryId_returnsCorrectProducts() {
        // Arrange
        Integer categoryId = 1;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String subCategory = null;

        // Act
        List<Product> products = dao.search(categoryId, minPrice, maxPrice, subCategory);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(p -> p.getCategoryId() == 1));
    }

    @Test
    void search_byPriceRange_returnsCorrectProducts() {
        // Arrange
        Integer categoryId = null;
        BigDecimal minPrice = new BigDecimal("10.00");
        BigDecimal maxPrice = new BigDecimal("30.00");
        String subCategory = null;

        // Act
        List<Product> products = dao.search(categoryId, minPrice, maxPrice, subCategory);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(p ->
                p.getPrice().compareTo(minPrice) >= 0 && p.getPrice().compareTo(maxPrice) <= 0));
    }

    @Test
    void search_bySubCategory_returnsCorrectProducts() {
        // Arrange
        Integer categoryId = null;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String subCategory = "Rock";

        // Act
        List<Product> products = dao.search(categoryId, minPrice, maxPrice, subCategory);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(p -> subCategory.equals(p.getSubCategory())));
    }

    @Test
    void search_withAllFilters_returnsCorrectProducts() {
        // Arrange
        Integer categoryId = 1;
        BigDecimal minPrice = new BigDecimal("20.00");
        BigDecimal maxPrice = new BigDecimal("40.00");
        String subCategory = "Rock";

        // Act
        List<Product> products = dao.search(categoryId, minPrice, maxPrice, subCategory);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        assertTrue(products.stream().allMatch(p ->
                p.getCategoryId() == 1 &&
                        p.getPrice().compareTo(minPrice) >= 0 &&
                        p.getPrice().compareTo(maxPrice) <= 0 &&
                        subCategory.equals(p.getSubCategory())));
    }

    @Test
    void search_withNoFilters_returnsAllProducts() {
        // Arrange
        Integer categoryId = null;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        String subCategory = null;

        // Act
        List<Product> products = dao.search(categoryId, minPrice, maxPrice, subCategory);

        // Assert
        assertNotNull(products);
        assertFalse(products.isEmpty());
        // 63 is before adding new stuff
        assertEquals(63, products.size());
    }

    // update test
    @Test
    void update_shouldModifyExistingProduct() {
        // Arrange: get an existing product
        int productId = 1;
        Product original = dao.getById(productId);
        assertNotNull(original, "Original product should exist");

        // Create updated product
        Product updated = new Product();
        updated.setName("Updated Name");
        updated.setPrice(new BigDecimal("49.99"));
        updated.setCategoryId(original.getCategoryId());
        updated.setDescription("Updated description");
        updated.setSubCategory("Updated Subcategory");
        updated.setImageUrl("updated-image.jpg");
        updated.setStock(original.getStock() + 10);
        updated.setFeatured(!original.isFeatured());

        // Act
        dao.update(productId, updated);

        // Assert: fetch the product again and verify changes
        Product actual = dao.getById(productId);
        assertNotNull(actual, "Updated product should exist");
        assertEquals(updated.getName(), actual.getName());
        assertEquals(updated.getPrice(), actual.getPrice());
        assertEquals(updated.getCategoryId(), actual.getCategoryId());
        assertEquals(updated.getDescription(), actual.getDescription());
        assertEquals(updated.getSubCategory(), actual.getSubCategory());
        assertEquals(updated.getImageUrl(), actual.getImageUrl());
        assertEquals(updated.getStock(), actual.getStock());
        assertEquals(updated.isFeatured(), actual.isFeatured());
    }

    @Test
    void update_nonExistingProduct_shouldThrowException() {
        // Arrange
        int nonExistingId = 9999;
        Product product = new Product();
        product.setName("Non-existent");
        product.setPrice(new BigDecimal("9.99"));
        product.setCategoryId(1);
        product.setDescription("Test");
        product.setSubCategory("Test");
        product.setImageUrl("test.jpg");
        product.setStock(10);
        product.setFeatured(false);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            dao.update(nonExistingId, product);
        });

        assertTrue(exception.getMessage().contains("Updating product failed"));
    }
}