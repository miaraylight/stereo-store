package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories;";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet row = statement.executeQuery();
        )
        {

            while(row.next()) {
                Category category = mapRow(row);
                categories.add(category);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Error fetching categories", e);
        }
        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            try (ResultSet row = statement.executeQuery()) {
                if (row.next()) {
                    return mapRow(row);
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching category by id", e);
        }
    }

    @Override
    public Category create(Category category)
    {
        String sql = "INSERT INTO categories(name, description) VALUES (?, ?);";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }

            // Retrieve the generated category_id
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int categoryId = generatedKeys.getInt(1);
                    category.setCategoryId(categoryId);
                    return category; // return the object with its ID
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error creating category", e);
        }
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Set parameters
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Updating category failed, no rows affected. Category ID: " + categoryId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error updating category with ID " + categoryId, e);
        }
    }

    @Override
    public void delete(int categoryId)
    {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Deleting category failed, no rows affected. Category ID: " + categoryId);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting category with ID " + categoryId, e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
