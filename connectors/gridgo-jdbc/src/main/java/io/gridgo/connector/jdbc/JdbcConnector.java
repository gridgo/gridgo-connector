package io.gridgo.connector.jdbc;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.framework.support.exceptions.BeanNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.ConnectionFactory;
import snaq.db.ConnectionPool;
import static io.gridgo.connector.jdbc.JdbcConstants.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@ConnectorEndpoint(scheme = "jdbc", syntax = "jdbcUri", raw = true)
public class JdbcConnector extends AbstractConnector {
    // Params of gridgo, exclude user and password
    private static HashSet<String> reserveParams = new HashSet<>(Arrays.asList("pool"));

    @Override
    protected void onInit() {
        var userName = getParam("user");
        var password = getParam("password");
        var connectionBean = getParam("pool");
        if (connectionBean == null) {
            var connectionFactory = initialDefaulConnectionFactory(userName, password);
            this.producer = Optional.of(new JdbcProducer(getContext(), connectionFactory));
            return;
        }
        try {
            var connectionFactory = getContext().getRegistry().lookupMandatory(connectionBean, ConnectionFactory.class);
            this.producer = Optional.of(new JdbcProducer(getContext(), connectionFactory));
        } catch (BeanNotFoundException ex) {
            log.error("Didn't find appropriate pool", ex);
            throw ex;
        }
    }

    private ConnectionFactory initialDefaulConnectionFactory(String userName, String password) {
        // exclude param of gridgo
        var params = getConnectorConfig().getParameters().entrySet().stream()//
                                         .filter(entry -> !reserveParams.contains(entry.getKey())) //
                                         .map(entry -> entry.getKey() + "=" + entry.getValue()) //
                                         .reduce((p1, p2) -> p1 + "&" + p2) //
                                         .orElse("");
        var jdbcUrl = getConnectorConfig().getNonQueryEndpoint() + (params.isEmpty() ? "" : "?" + params);
        var connectionPool = new ConnectionPool("local", DEFAULT_CONNECTION_POOL_MIN_POOL_SIZE,
                DEFAULT_CONNECTION_POOL_MAX_POOL_SIZE, DEFAULT_CONNECTION_POOL_MAX_SIZE,
                DEFAULT_CONNECTION_POOL_IDLE_TIMEOUT, jdbcUrl, userName, password);
        return connectionPool::getConnection;
    }

}
