package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();
        String sql = """
                SELECT *
                FROM shopping_cart
                JOIN products ON shopping_cart.product_id = products.product_id
                WHERE shopping_cart.user_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)
             )
        {
            statement.setInt(1, userId);

            try( ResultSet row = statement.executeQuery()) {
                while (row.next()) {
                    ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
                    Product product = mapRowProduct(row);

                    shoppingCartItem.setProduct(product);
                    shoppingCartItem.setQuantity(row.getInt("quantity"));

                    shoppingCart.add(shoppingCartItem);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching shopping cart for user ", e);
        }
        return shoppingCart;
    }

    @Override
    public ShoppingCart addToCart(int productId, int userId) {
        String sql = """
        INSERT INTO shopping_cart (user_id, product_id, quantity)
        VALUES (?, ?, 1)
        ON DUPLICATE KEY UPDATE
            quantity = quantity + 1
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Adding product failed, no rows affected.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error adding product to cart", e);
        }
        return getByUserId(userId);
    }

    @Override
    public void updateCartItem(int productId, int userId, int quantity) {
        String sql = """
                UPDATE shopping_cart
                SET quantity = ?
                WHERE user_id = ? AND product_id = ?;
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating cart item failed, no rows affected.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error updating product with ID " + productId, e);
        }
    }

    @Override
    public void clearCart(int userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            int rowsAffected = statement.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " items from cart.");

        } catch (SQLException e) {
            throw new RuntimeException("Error clearing shopping cart for user ", e);
        }

    }

    protected static Product mapRowProduct(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String subCategory = row.getString("subcategory");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, subCategory, stock, isFeatured, imageUrl);
    }
}
