package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;

import javax.sql.DataSource;

@Component
public class MySqlOrderDao extends MySqlDaoBase implements OrderDao {
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void checkout(int userId) {

    }
}
