// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The SFC licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.openqa.selenium.grid.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;

import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.ImmutableCapabilities;
import org.openqa.selenium.events.EventBus;
import org.openqa.selenium.events.local.GuavaEventBus;
import org.openqa.selenium.grid.config.MapConfig;
import org.openqa.selenium.grid.data.Session;
import org.openqa.selenium.grid.distributor.Distributor;
import org.openqa.selenium.grid.distributor.local.LocalDistributor;
import org.openqa.selenium.grid.node.local.LocalNode;
import org.openqa.selenium.grid.security.Secret;
import org.openqa.selenium.grid.server.BaseServerOptions;
import org.openqa.selenium.grid.server.Server;
import org.openqa.selenium.grid.sessionmap.SessionMap;
import org.openqa.selenium.grid.sessionmap.local.LocalSessionMap;
import org.openqa.selenium.grid.sessionqueue.NewSessionQueuer;
import org.openqa.selenium.grid.sessionqueue.config.NewSessionQueueOptions;
import org.openqa.selenium.grid.sessionqueue.local.LocalNewSessionQueue;
import org.openqa.selenium.grid.sessionqueue.local.LocalNewSessionQueuer;
import org.openqa.selenium.grid.testing.TestSessionFactory;
import org.openqa.selenium.grid.web.CombinedHandler;
import org.openqa.selenium.grid.web.RoutableHttpClientFactory;
import org.openqa.selenium.net.PortProber;
import org.openqa.selenium.netty.server.NettyServer;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpHandler;
import org.openqa.selenium.remote.tracing.DefaultTestTracer;
import org.openqa.selenium.remote.tracing.Tracer;

public class JmxTest {

  private static final Capabilities CAPS = new ImmutableCapabilities("browserName", "cheese");
  private Server<?> server;
  URI nodeUri;

  @Before
  public void setup() throws URISyntaxException, MalformedURLException {
    Tracer tracer = DefaultTestTracer.createTracer();
    EventBus bus = new GuavaEventBus();

    nodeUri = new URI("http://localhost:4444");
    CombinedHandler handler = new CombinedHandler();
    HttpClient.Factory clientFactory = new RoutableHttpClientFactory(
      nodeUri.toURL(),
      handler,
      HttpClient.Factory.createDefault());

    NewSessionQueueOptions queueOptions =
      new NewSessionQueueOptions(
        new MapConfig(
          ImmutableMap.of("sessionqueue", ImmutableMap.of(
            "session-retry-interval", 2,
            "session-request-timeout", 2))));

    SessionMap sessions = new LocalSessionMap(tracer, bus);
    handler.addHandler(sessions);

    LocalNewSessionQueue localNewSessionQueue = new LocalNewSessionQueue(
      tracer,
      bus,
      queueOptions.getSessionRequestRetryInterval(),
      queueOptions.getSessionRequestTimeout());
    NewSessionQueuer queuer = new LocalNewSessionQueuer(tracer, bus, localNewSessionQueue);
    handler.addHandler((queuer));

    Secret secret = new Secret("cheese");

    Distributor distributor = new LocalDistributor(
      tracer,
      bus,
      clientFactory,
      sessions,
      queuer,
      secret);
    handler.addHandler(distributor);

    LocalNode localNode = LocalNode.builder(tracer, bus, nodeUri, nodeUri, secret)
      .add(CAPS, new TestSessionFactory((id, caps) -> new Session(
        id,
        nodeUri,
        new ImmutableCapabilities(),
        caps,
        Instant.now()))).build();
    handler.addHandler(localNode);
    distributor.add(localNode);

    Router router = new Router(tracer, clientFactory, sessions, queuer, distributor);

    server = createServer(router);
    server.start();
  }

  @After
  public void stopServer() {
    server.stop();
  }

  private static Server<?> createServer(HttpHandler handler) {
    return new NettyServer(
      new BaseServerOptions(
        new MapConfig(
          ImmutableMap.of("server", ImmutableMap.of("port", PortProber.findFreePort())))),
      handler);
  }

  @Test
  public void shouldBeAbleToRegisterBaseServerConfig() {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName("org.seleniumhq.grid:type=Config,name=BaseServerConfig");
      MBeanInfo info = beanServer.getMBeanInfo(name);
      assertThat(info).isNotNull();

      MBeanAttributeInfo[] attributeInfoArray = info.getAttributes();
      assertThat(attributeInfoArray).hasSize(3);

      String urlValue = (String) beanServer.getAttribute(name, "URL");
      assertThat(urlValue).isEqualTo(server.getUrl().toString());

    } catch (InstanceNotFoundException | IntrospectionException | ReflectionException
      | MalformedObjectNameException e) {
      fail("Could not find the registered MBean");
    } catch (MBeanException e) {
      fail("MBeanServer exception");
    } catch (AttributeNotFoundException e) {
      fail("Could not find the registered MBean's attribute");
    }
  }

  @Test
  public void shouldBeAbleToRegisterNode() {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName("org.seleniumhq.grid:type=Node,name=LocalNode");
      MBeanInfo info = beanServer.getMBeanInfo(name);
      assertThat(info).isNotNull();

      MBeanAttributeInfo[] attributeInfo = info.getAttributes();
      assertThat(attributeInfo).hasSize(9);

      String currentSessions = (String) beanServer.getAttribute(name, "CurrentSessions");
      assertThat(Integer.parseInt(currentSessions)).isZero();

      String maxSessions = (String) beanServer.getAttribute(name, "MaxSessions");
      assertThat(Integer.parseInt(maxSessions)).isEqualTo(1);

      String status = (String) beanServer.getAttribute(name, "Status");
      assertThat(status).isEqualTo("UP");

      String totalSlots = (String) beanServer.getAttribute(name, "TotalSlots");
      assertThat(Integer.parseInt(totalSlots)).isEqualTo(1);

      String usedSlots = (String) beanServer.getAttribute(name, "UsedSlots");
      assertThat(Integer.parseInt(usedSlots)).isZero();

      String load = (String) beanServer.getAttribute(name, "Load");
      assertThat(Float.parseFloat(load)).isEqualTo(0.0f);

      String remoteNodeUri = (String) beanServer.getAttribute(name, "RemoteNodeUri");
      assertThat(remoteNodeUri).isEqualTo(nodeUri.toString());

      String gridUri = (String) beanServer.getAttribute(name, "GridUri");
      assertThat(gridUri).isEqualTo(nodeUri.toString());

    } catch (InstanceNotFoundException | IntrospectionException | ReflectionException
      | MalformedObjectNameException e) {
      fail("Could not find the registered MBean");
    } catch (MBeanException e) {
      fail("MBeanServer exception");
    } catch (AttributeNotFoundException e) {
      fail("Could not find the registered MBean's attribute");
    }
  }

  @Test
  public void shouldBeAbleToRegisterSessionQueuerServerConfig() {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName(
        "org.seleniumhq.grid:type=Config,name=NewSessionQueueConfig");
      MBeanInfo info = beanServer.getMBeanInfo(name);
      assertThat(info).isNotNull();

      MBeanAttributeInfo[] attributeInfoArray = info.getAttributes();
      assertThat(attributeInfoArray).hasSize(2);

      String requestTimeout = (String) beanServer.getAttribute(name, "RequestTimeoutSeconds");
      assertThat(Long.parseLong(requestTimeout)).isEqualTo(2L);

      String retryInterval = (String) beanServer.getAttribute(name, "RetryIntervalSeconds");
      assertThat(Long.parseLong(retryInterval)).isEqualTo(2L);
    } catch (InstanceNotFoundException | IntrospectionException | ReflectionException
      | MalformedObjectNameException e) {
      fail("Could not find the registered MBean");
    } catch (MBeanException e) {
      fail("MBeanServer exception");
    } catch (AttributeNotFoundException e) {
      fail("Could not find the registered MBean's attribute");
    }
  }

  @Test
  public void shouldBeAbleToRegisterSessionQueue() {
    MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName name = new ObjectName("org.seleniumhq.grid:type=SessionQueue," +
        "name=LocalSessionQueue");
      MBeanInfo info = beanServer.getMBeanInfo(name);
      assertThat(info).isNotNull();

      MBeanAttributeInfo[] attributeInfoArray = info.getAttributes();
      assertThat(attributeInfoArray).hasSize(1);

      String size = (String) beanServer.getAttribute(name, "NewSessionQueueSize");
      assertThat(Integer.parseInt(size)).isZero();
    } catch (InstanceNotFoundException | IntrospectionException | ReflectionException
      | MalformedObjectNameException e) {
      fail("Could not find the registered MBean");
    } catch (MBeanException e) {
      fail("MBeanServer exception");
    } catch (AttributeNotFoundException e) {
      fail("Could not find the registered MBean's attribute");
    }
  }
}

