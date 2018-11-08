package io.gridgo.connector.test;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.connector.support.exceptions.UnsupportedSchemeException;
import io.gridgo.dummy.test.DummyConnector;

public class ResolverUnitTest {
	
	@Test
	public void testResolver() {
		TestUriResolver resolver = new TestUriResolver(TestConnector.class);
		Properties props = resolver.testResolver("test://127.0.0.1:80/api", "test://{host}[:{port}][/{path}]");
		Assert.assertEquals("127.0.0.1", props.get("host"));
		Assert.assertEquals("80", props.get("port"));
		Assert.assertEquals("api", props.get("path"));

		props = resolver.testResolver("test://127.0.0.1:80", "test://{host}[:{port}][/{path}]");
		Assert.assertEquals("127.0.0.1", props.get("host"));
		Assert.assertEquals("80", props.get("port"));
		Assert.assertNull(props.get("path"));

		props = resolver.testResolver("test://127.0.0.1", "test://{host}[:{port}][/{path}]");
		Assert.assertEquals("127.0.0.1", props.get("host"));
		Assert.assertNull(props.get("port"));
		Assert.assertNull(props.get("path"));

		props = resolver.testResolver("test://127.0.0.1/api", "test://{host}[:{port}][/{path}]");
		Assert.assertEquals("127.0.0.1", props.get("host"));
		Assert.assertNull(props.get("port"));
		Assert.assertEquals("api", props.get("path"));
	}

	@Test
	public void testFactory() {
		try {
			new DefaultConnectorFactory().createConnector("dummy:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");
			Assert.fail("Must throw exception");
		} catch (UnsupportedSchemeException ex) {

		}
		Connector connector = new DefaultConnectorFactory().createConnector(
				"dummy:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2", new ClasspathConnectorResolver("io.gridgo.dummy"));
		Assert.assertNotNull(connector);
		Assert.assertNotNull(connector.getConnectorConfig());
		Assert.assertNotNull(connector.getConnectorConfig().getRemaining());
		Assert.assertNotNull(connector.getConnectorConfig().getParameters());
		Assert.assertTrue(connector instanceof DummyConnector);
		Assert.assertEquals("pull:tcp://127.0.0.1:8080", connector.getConnectorConfig().getRemaining());
		Assert.assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		Assert.assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		Assert.assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		Assert.assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		Assert.assertEquals("127.0.0.1", connector.getConnectorConfig().getPlaceholders().get("host"));
		Assert.assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));
	}

	@Test
	public void testSimple() {
		ConnectorResolver resolver = new ClasspathConnectorResolver();
		Connector connector = resolver.resolve("test:pull:tcp://127.0.0.1:8080?p1=v1&p2=v2");
		Assert.assertNotNull(connector);
		Assert.assertNotNull(connector.getConnectorConfig());
		Assert.assertNotNull(connector.getConnectorConfig().getRemaining());
		Assert.assertNotNull(connector.getConnectorConfig().getParameters());
		Assert.assertTrue(connector instanceof TestConnector);
		Assert.assertEquals("pull:tcp://127.0.0.1:8080", connector.getConnectorConfig().getRemaining());
		Assert.assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		Assert.assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		Assert.assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		Assert.assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		Assert.assertEquals("127.0.0.1", connector.getConnectorConfig().getPlaceholders().get("host"));
		Assert.assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));
	}

	@Test
	public void testIPv6() {
		ConnectorResolver resolver = new ClasspathConnectorResolver();
		Connector connector = resolver.resolve("test:pull:tcp://[2001:db8:1f70::999:de8:7648:6e8]:8080?p1=v1&p2=v2");
		Assert.assertNotNull(connector);
		Assert.assertNotNull(connector.getConnectorConfig());
		Assert.assertNotNull(connector.getConnectorConfig().getRemaining());
		Assert.assertNotNull(connector.getConnectorConfig().getParameters());
		Assert.assertTrue(connector instanceof TestConnector);
		Assert.assertEquals("pull:tcp://[2001:db8:1f70::999:de8:7648:6e8]:8080",
				connector.getConnectorConfig().getRemaining());
		Assert.assertEquals("v1", connector.getConnectorConfig().getParameters().get("p1"));
		Assert.assertEquals("v2", connector.getConnectorConfig().getParameters().get("p2"));
		Assert.assertEquals("pull", connector.getConnectorConfig().getPlaceholders().get("type"));
		Assert.assertEquals("tcp", connector.getConnectorConfig().getPlaceholders().get("transport"));
		Assert.assertEquals("[2001:db8:1f70::999:de8:7648:6e8]",
				connector.getConnectorConfig().getPlaceholders().get("host"));
		Assert.assertEquals("8080", connector.getConnectorConfig().getPlaceholders().get("port"));
	}
}
