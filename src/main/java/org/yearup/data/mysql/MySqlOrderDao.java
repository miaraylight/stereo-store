package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order checkout(int userId, Order order, ShoppingCart shoppingCart) {
        String sql = """
        INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS))
        {

            order.setDate(LocalDateTime.now());

            statement.setInt(1, userId);
            statement.setTimestamp(2, Timestamp.valueOf(order.getDate()));
            statement.setString(3, order.getAddress());
            statement.setString(4, order.getCity());
            statement.setString(5, order.getState());
            statement.setString(6, order.getZip());
            statement.setBigDecimal(7, order.getShippingAmount());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setOrderId(orderId);
                    return order;
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

    }

    public int createOrderLineItems(Product product, ShoppingCartItem item, int orderId) {
        String sql = """
        INSERT INTO order_line_items (order_id, product_id, sales_price, quantity, discount)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, product.getProductId());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, item.getQuantity());
            statement.setBigDecimal(5, item.getDiscountPercent());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Adding order line item failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int order_line_item_id = generatedKeys.getInt(1);
                    return order_line_item_id; // I'm done just id here
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error adding order line item", e);
        }
    }
}
