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
}