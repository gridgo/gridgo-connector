package io.gridgo.connector.netty4.test;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.connector.netty4.test.support.AbstractNetty4UnitTest;

public class Netty4WebsocketUnitTest extends AbstractNetty4UnitTest {

    private static final String PATH = "test";
    private static final String TRANSPORT = "ws";

    @Test
    public void testCloseServer() throws InterruptedException, PromiseException {
        this.testCloseServer(TRANSPORT, PATH);
    }

    @Test
    public void testCloseSocketFromClient() throws InterruptedException, PromiseException {
        this.testCloseSocketFromClient(TRANSPORT, PATH);
    }

    @Test
    public void testCloseSocketFromServer() throws InterruptedException, PromiseException {
        this.testCloseSocketFromServer(TRANSPORT, PATH);
    }

    @Test
    public void testHandlerException() throws InterruptedException, PromiseException {
        this.testHandlerException(TRANSPORT, PATH);
    }

    @Test
    public void testWsPingPong() throws InterruptedException, PromiseException {
        this.testPingPong(TRANSPORT, PATH);
    }
}
