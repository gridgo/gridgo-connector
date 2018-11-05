package io.gridgo.connector.test;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;

public class ResolverUnitTest {

	@Test
	public void testSimple() {
		ConnectorResolver resolver = new ClasspathConnectorResolver();
		Connector connector = resolver.resolve("test:pull:tcp://localhost:8080?p1=v1&p2=v2");
		Assert.assertNotNull(connector);
		Assert.assertTrue(connector instanceof TestConnector);
		Assert.assertNotNull(connector.getConnectorConfig());
		Assert.assertNotNull(connector.getConnectorConfig().getRemaining());
		Assert.assertEquals("pull:tcp://localhost:8080", connector.getConnectorConfig().getRemaining());
		Assert.assertNotNull(connector.getConnectorConfig().getParameters());
		Assert.assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		Assert.assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		Assert.assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		Assert.assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		Assert.assertEquals("localhost", connector.getConnectorConfig().getPlaceholders().get("host"));
		Assert.assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));
	}
}
