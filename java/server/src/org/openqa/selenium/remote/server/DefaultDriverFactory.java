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

package org.openqa.selenium.remote.server;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DefaultDriverFactory implements DriverFactory {

  private static final Logger LOG = Logger.getLogger(DefaultDriverFactory.class.getName());

  private static List<DriverProvider> defaultDriverProviders =
      new ImmutableList.Builder<DriverProvider>()
          .add(new FirefoxDriverProvider())
          .add(new DefaultDriverProvider(DesiredCapabilities.chrome(),
                                         "org.openqa.selenium.chrome.ChromeDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.internetExplorer(),
                                         "org.openqa.selenium.ie.InternetExplorerDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.edge(),
                                         "org.openqa.selenium.edge.EdgeDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.opera(),
                                         "com.opera.core.systems.OperaDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.operaBlink(),
                                         "org.openqa.selenium.opera.OperaDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.safari(),
                                         "org.openqa.selenium.safari.SafariDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.phantomjs(),
                                         "org.openqa.selenium.phantomjs.PhantomJSDriver"))
          .add(new DefaultDriverProvider(DesiredCapabilities.htmlUnit(),
                                         "org.openqa.selenium.htmlunit.HtmlUnitDriver"))
          .build();

  private Map<Capabilities, DriverProvider> capabilitiesToDriverProvider =
      new ConcurrentHashMap<>();

  public DefaultDriverFactory(Platform runningOn) {
    registerDefaults(runningOn);
    registerServiceLoaders(runningOn);
  }

  public void registerDriverProvider(DriverProvider driverProvider) {
    if (driverProvider.canCreateDriverInstances()) {
      capabilitiesToDriverProvider.put(driverProvider.getProvidedCapabilities(), driverProvider);
    } else {
      LOG.info(String.format("Driver provider %s is not registered", driverProvider));
    }
  }

  @VisibleForTesting
  DriverProvider getProviderMatching(Capabilities desired) {
    // We won't be able to make a match if no drivers have been registered.
    checkState(!capabilitiesToDriverProvider.isEmpty(),
               "No drivers have been registered, will be unable to match %s", desired);
    Capabilities bestMatchingCapabilities =
        CapabilitiesComparator.getBestMatch(desired, capabilitiesToDriverProvider.keySet());
    return capabilitiesToDriverProvider.get(bestMatchingCapabilities);
  }

  public WebDriver newInstance(Capabilities capabilities) {
    DriverProvider provider = getProviderMatching(capabilities);
    if (provider.canCreateDriverInstanceFor(capabilities)) {
      return provider.newInstance(capabilities);
    }
    throw new WebDriverException(String.format(
      "The best matching driver provider %s can't create a new driver instance for %s",
      provider, capabilities));
  }

  public boolean hasMappingFor(Capabilities capabilities) {
    return capabilitiesToDriverProvider.containsKey(capabilities);
  }

  private void registerDefaults(Platform current) {
    for (DriverProvider provider : defaultDriverProviders) {
      registerDriverProvider(current, provider);
    }
  }

  private void registerServiceLoaders(Platform current) {
    for (DriverProvider provider : ServiceLoader.load(DriverProvider.class)) {
      registerDriverProvider(current, provider);
    }
  }

  private void registerDriverProvider(Platform current, DriverProvider provider) {
    Capabilities caps = provider.getProvidedCapabilities();
    if (!platformMatches(current, caps)) {
      LOG.info(String.format(
          "Driver provider %s registration is skipped:%n" +
          " registration capabilities %s does not match the current platform %s",
          provider, caps, current));
      return;
    }

    if (!provider.canCreateDriverInstances()) {
      LOG.info(String.format(
          "Driver provider %s registration is skipped:%n" +
          "Unable to create new instances on this machine.",
          provider));
    }

    registerDriverProvider(provider);
  }

  private boolean platformMatches(Platform current, Capabilities caps) {
    return caps.getPlatform() == null
           || caps.getPlatform() == Platform.ANY
           || current.is(caps.getPlatform());
  }
}
