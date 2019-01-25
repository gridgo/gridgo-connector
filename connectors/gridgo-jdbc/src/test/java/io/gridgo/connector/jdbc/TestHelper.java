package io.gridgo.connector.jdbc;

import io.gridgo.connector.jdbc.support.Helper;
import org.junit.Assert;
import org.junit.Test;

public class TestHelper {

    @Test
    public void testGetOperationNormalCase() {
        String sqlStatement = "Select * from table";
        String operation = Helper.getOperation(sqlStatement);
        Assert.assertEquals(JdbcConstants.OPERATION_SELECT, operation);
    }

    @Test
    public void testGetOperationSpaceAtFirst() {
        String sqlStatement = "  Select   * from table";
        String operation = Helper.getOperation(sqlStatement);
        Assert.assertEquals(JdbcConstants.OPERATION_SELECT, operation);
    }

}
