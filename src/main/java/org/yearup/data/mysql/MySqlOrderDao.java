package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.*;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Order getByIdWithItems(int orderId, int userId) {
        String sql = """
            SELECT 
                o.order_id, o.user_id, o.date, o.address, o.city, o.state, o.zip, o.shipping_amount,
                oli.order_line_item_id, oli.product_id, oli.sales_price, oli.quantity, oli.discount,
                p.name AS product_name, p.category_id, p.description AS product_description,
                p.subcategory, p.image_url, p.stock, p.featured
            FROM orders o
            JOIN order_line_items oli ON o.order_id = oli.order_id
            JOIN products p ON oli.product_id = p.product_id
            WHERE o.order_id = ? AND o.user_id = ?;
        """;

        Order order = null;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (order == null) {
                        order = new Order();
                        order.setOrderId(rs.getInt("order_id"));
                        order.setUserId(rs.getInt("user_id"));
                        order.setDate(rs.getTimestamp("date").toLocalDateTime());
                        order.setAddress(rs.getString("address"));
                        order.setCity(rs.getString("city"));
                        order.setState(rs.getString("state"));
                        order.setZip(rs.getString("zip"));
                        order.setShippingAmount(rs.getBigDecimal("shipping_amount"));
                    }

                    Product product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setName(rs.getString("product_name"));
                    product.setCategoryId(rs.getInt("category_id"));
                    product.setDescription(rs.getString("product_description"));
                    product.setSubCategory(rs.getString("subcategory"));
                    product.setImageUrl(rs.getString("image_url"));
                    product.setStock(rs.getInt("stock"));
                    product.setFeatured(rs.getBoolean("featured"));

                    OrderLineItem lineItem = new OrderLineItem();
                    lineItem.setOrderLineItemId(rs.getInt("order_line_item_id"));
                    lineItem.setProduct(product);
                    lineItem.setQuantity(rs.getInt("quantity"));
                    lineItem.setSalesPrice(rs.getBigDecimal("sales_price"));
                    lineItem.setDiscount(rs.getBigDecimal("discount"));

                    order.addItem(lineItem);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching order with items", e);
        }

        return order;
    }

    @Override
    public int createOrder(Order order, int userId) {
        String sql = """
        INSERT INTO orders (user_id, date, address, city, state, zip, shipping_amount)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS))
        {
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
                    return orderId;
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
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
                    throw new SQLException("Creating order line item failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating order line item", e);
        }
    }
}
