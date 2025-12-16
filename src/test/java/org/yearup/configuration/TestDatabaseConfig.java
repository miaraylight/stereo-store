package org.yearup.configuration;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class TestDatabaseConfig
{
    private final String serverUrl;
    private final String testDb;
    private final String username;
    private final String password;

    @Autowired
    public TestDatabaseConfig(@Value("${spring.datasource.url}") String serverUrl,
                              @Value("${spring.datasource.username}") String username,
                              @Value("${spring.datasource.password}") String password,
                              @Value("${spring.datasource.testdb}") String testDb)
    {
        this.serverUrl = serverUrl;
        this.testDb = testDb;
        this.username = username;
        this.password = password;
    }

//    @PostConstruct // moved it into dataSource creation
//    public void setup() {
//
//        try(Connection connection = DriverManager.getConnection(serverUrl + "/sys", username, password);
//            Statement statement = connection.createStatement();
//        )
//        {
//            statement.execute("DROP DATABASE IF EXISTS " + testDb + ";");
//            statement.execute("CREATE DATABASE " + testDb + ";");
//        }
//        catch (SQLException ignored) {}
//    }

    @PreDestroy
    public void cleanup() {

        try(Connection connection = DriverManager.getConnection(serverUrl + "/sys", username, password);
            Statement statement = connection.createStatement();
        )
        {
            statement.execute("DROP DATABASE IF EXISTS " + testDb + ";");
        }
        catch (SQLException ignored){}

    }


    @Bean
    public DataSource dataSource() throws SQLException, IOException
    {
        // 1. Create the database first using a connection to a non-specific schema (like /sys)
        try(Connection connection = DriverManager.getConnection(serverUrl + "/sys", username, password);
            Statement statement = connection.createStatement();
        )
        {
            // Dropping and recreating ensures a clean state for testing
            statement.execute("DROP DATABASE IF EXISTS " + testDb + ";");
            statement.execute("CREATE DATABASE " + testDb + ";");
        }
        catch (SQLException ignored) {}

        SingleConnectionDataSource dataSource = new SingleConnectionDataSource();
        dataSource.setUrl(String.format(serverUrl + "/" + testDb));
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setAutoCommit(false);
        dataSource.setSuppressClose(true);

        ScriptRunner runner = new ScriptRunner(dataSource.getConnection());
        Reader reader = new BufferedReader(new FileReader((new ClassPathResource("test-data.sql")).getFile().getAbsolutePath()));
        runner.runScript(reader);
        dataSource.getConnection().commit();

        return dataSource;
    }
}
