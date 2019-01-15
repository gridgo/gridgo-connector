package io.gridgo.connector.mysql;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.framework.support.exceptions.BeanNotFoundException;
import org.jdbi.v3.core.ConnectionFactory;
import snaq.db.ConnectionPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

@ConnectorEndpoint(scheme = "jdbc", syntax = "jdbcUri", raw = true)
public class JdbcConnector extends AbstractConnector {

    private static HashSet<String> reserveParams = new HashSet<>(Arrays.asList("pool"));

    protected void onInit() {
        var userName = getParam("user");
        var password = getParam("password");
        var connectionBean = getParam("pool");
        try {
            var connectionFactory = getContext().getRegistry().lookupMandatory(connectionBean, ConnectionFactory.class);
            this.producer = Optional.of(new JdbcProducer(getContext(), connectionFactory));
        } catch (BeanNotFoundException ex) {
            var params = getConnectorConfig().getParameters().entrySet().stream()//
                    .filter(entry -> !reserveParams.contains(entry.getKey()))//
                    .map(entry -> entry.getKey() + "=" + entry.getValue())//
                    .reduce((p1, p2) -> p1 + "&" + p2)//
                    .orElse("");
            var url = getConnectorConfig().getNonQueryEndpoint() + (params.isEmpty() ? "" : "?" + params);
            var connectionPool = new ConnectionPool("local", 5, 15, 0, 180, url, userName, password);
            this.producer = Optional.of(new JdbcProducer(getContext(), connectionPool::getConnection));
        }


    }

}
