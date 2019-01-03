package io.gridgo.connector.netty4.test;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.connector.netty4.test.support.AbstractNetty4UnitTest;

public class Netty4TcpUnitTest extends AbstractNetty4UnitTest {

    private static final String TRANSPORT = "tcp";

    @Test
    public void testCloseServer() throws InterruptedException, PromiseException {
        this.testCloseServer(TRANSPORT, null);
    }

    @Test
    public void testCloseSocketFromClient() throws InterruptedException, PromiseException {
        this.testCloseSocketFromClient(TRANSPORT, null);
    }

    @Test
    public void testCloseSocketFromServer() throws InterruptedException, PromiseException {
        this.testCloseSocketFromServer(TRANSPORT, null);
    }

    @Test
    public void testHandlerException() throws InterruptedException, PromiseException {
        this.testHandlerException(TRANSPORT, null);
    }

    @Test
    public void testTcpPingPong() throws InterruptedException, PromiseException {
        this.testPingPong(TRANSPORT, null);
    }
}
