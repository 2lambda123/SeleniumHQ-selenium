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
package org.openqa.selenium;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Small test for name extraction
 */
@RunWith(JUnit4.class)
public class WebDriverExceptionTest {
  @Test
  public void testExtractsADriverName() {
    StackTraceElement[] stackTrace = new StackTraceElement[2];
    stackTrace[0] = new StackTraceElement("SomeClass", "someMethod", "SomeClass.java", 5);
    stackTrace[1] = new StackTraceElement("TestDriver", "someMethod", "TestDriver.java", 5);

    String gotName = WebDriverException.getDriverName(stackTrace);

    assertEquals("TestDriver", gotName);
  }

  @Test
  public void testExtractsMostSpecificDriverName() {
    StackTraceElement[] stackTrace = new StackTraceElement[3];
    stackTrace[0] = new StackTraceElement("SomeClass", "someMethod", "SomeClass.java", 5);
    stackTrace[1] =
        new StackTraceElement("RemoteWebDriver", "someMethod", "RemoteWebDriver.java", 5);
    stackTrace[2] = new StackTraceElement("FirefoxDriver", "someMethod", "FirefoxDriver.java", 5);

    String gotName = WebDriverException.getDriverName(stackTrace);

    assertEquals("FirefoxDriver", gotName);

  }

  @Test
  public void testDefaultsToUnknownDriverName() {
    StackTraceElement[] stackTrace = new StackTraceElement[2];
    stackTrace[0] = new StackTraceElement("SomeClass", "someMethod", "SomeClass.java", 5);
    stackTrace[1] = new StackTraceElement("SomeOtherClass", "someMethod", "SomeOtherClass.java", 5);

    String gotName = WebDriverException.getDriverName(stackTrace);

    assertEquals("unknown", gotName);
  }

  @Test
  public void systemInformationIncludesHostIpOperatingSystemAndJavaVersion() {
    String systemInformation = new WebDriverException().getSystemInformation();
    assertTrue("Actual value: " + systemInformation + " does not matches expected pattern", systemInformation.matches(
      "System info: host: '.+', ip: '.+', os\\.name: '.+', os\\.arch: '.+', os\\.version: '.+', java\\.version: '.+'"));
  }
}
