package io.gridgo.connector.mysql;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

import java.util.Optional;

@ConnectorEndpoint(scheme = "jdbc", syntax = "{db}/{host}/{post}/{user}/{password}/{schema}")
public class MySQLConnector extends AbstractConnector {

    protected void onInit() {
        var db = getPlaceholder("db");
        var host = getPlaceholder("host");
        var port = getPlaceholder("post");
        var userName = getPlaceholder("user");
        var password = getPlaceholder("password");
        var schema = getPlaceholder("schema");
        this.producer = Optional.of(new MySQLProducer(getContext(), getUrl(db, host, port,userName, password, schema),
                userName, password));
    }

    private String getUrl(String db, String host, String port, String user, String password, String schema){
        return "jdbc:"+ db +
                "://" + host +
                ":" + port +
                "/" + schema + "?" +
                "user=" + user + "&" +
                (password == null ? "": "password=" + password);
    }
}
