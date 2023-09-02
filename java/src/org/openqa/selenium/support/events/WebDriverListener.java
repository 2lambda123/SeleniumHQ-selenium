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

package org.openqa.selenium.support.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.openqa.selenium.Alert;
import org.openqa.selenium.Beta;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.ScriptKey;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Sequence;

/**
 * Classes that implement this interface are intended to be used with {@link EventFiringDecorator},
 * read documentation for this class to find detailed usage description.
 *
 * <p>This interface provides empty default implementation for all methods that do nothing.
 */
@Beta
public interface WebDriverListener {

  // Global

  /**
   * This method is called before the execution of any method on the 'target' object.
   * It provides a hook for performing actions or logging before any method call.
   *
   * @param target The original object on which methods will be invoked.
   * @param method The method that will be called on the 'target' object.
   * @param args   The arguments that will be passed to the method.
   */
  default void beforeAnyCall(Object target, Method method, Object[] args) {}

  /**
   * This method is called after the execution of any instance methods on the 'target' object.
   * It provides a hook for performing actions or logging after any instance method call.
   *
   * @param target The original object on which instance methods were invoked.
   * @param method The method that was called on the 'target' object.
   * @param args   The arguments passed to the method.
   * @param res    The result returned by the method.
   */
  default void afterAnyCall(Object target, Method method, Object[] args, Object result) {}

  /**
   * Notifies registered listeners about an error that occurred during the execution of a decorated method.
   *
   * @param target The original object on which the decorated method was invoked.
   * @param method The method that encountered an error.
   * @param args   The arguments passed to the method.
   * @param e      The InvocationTargetException containing the error details.
   */
  default void onError(Object target, Method method, Object[] args, InvocationTargetException e) {}

  // WebDriver

  /**
   * This method will be called before any method of a {@link WebDriver} instance is
   * called.
   *
   * @param driver - decorated WebDriver instance
   * @param method - method that will be called
   * @param args - arguments for the method
   */
  default void beforeAnyWebDriverCall(WebDriver driver, Method method, Object[] args) {}

  /**
   * This method will be called after any method of a {@link WebDriver} instance is
   * called.
   *
   * @param driver - decorated WebDriver instance
   * @param method - method that was called
   * @param args - arguments for the method
   * @param result - result of the method call
   */
  default void afterAnyWebDriverCall(
      WebDriver driver, Method method, Object[] args, Object result) {}

  /**
   * This method will be called before {@link WebDriver#get(String)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param url - url to navigate to
   */
  default void beforeGet(WebDriver driver, String url) {}

  /**
   * This method will be called after {@link WebDriver#get(String)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param url - url navigated to
   */
  default void afterGet(WebDriver driver, String url) {}

  /**
   * This method will be called before {@link WebDriver#getCurrentUrl()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeGetCurrentUrl(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#getCurrentUrl()} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param result - url of the current page
   */
  default void afterGetCurrentUrl(WebDriver driver, String result) {}

  /**
   * This method will be called before {@link WebDriver#getTitle()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeGetTitle(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#getTitle()} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param result - title of the current page
   */
  default void afterGetTitle(WebDriver driver, String result) {}

  /**
   * This method will be called before {@link WebDriver#findElement(By)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param locator - locator used to find the element
   */
  default void beforeFindElement(WebDriver driver, By locator) {}

  /**
   * This method will be called after {@link WebDriver#findElement(By)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param locator - locator used to find the element
   * @param result - found WebElement
   */
  default void afterFindElement(WebDriver driver, By locator, WebElement result) {}

  /**
   * This method will be called before {@link WebDriver#findElements(By)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param locator - locator used to find the elements
   */
  default void beforeFindElements(WebDriver driver, By locator) {}

  /**
   * This method will be called after {@link WebDriver#findElements(By)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param locator - locator used to find the elements
   * @param result - list of found WebElements
   */
  default void afterFindElements(WebDriver driver, By locator, List<WebElement> result) {}

  /**
   * This method will be called before {@link WebDriver#getPageSource()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeGetPageSource(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#getPageSource()} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param result - page source of the current page
   */
  default void afterGetPageSource(WebDriver driver, String result) {}

  /**
   * This method will be called before {@link WebDriver#close()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeClose(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#close()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void afterClose(WebDriver driver) {}

  /**
   * This method will be called before {@link WebDriver#quit()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeQuit(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#quit()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void afterQuit(WebDriver driver) {}

  /**
   * This method will be called before {@link WebDriver#getWindowHandles()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeGetWindowHandles(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#getWindowHandles()} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param result - set of window handles
   */
  default void afterGetWindowHandles(WebDriver driver, Set<String> result) {}

  /**
   * This method will be called before {@link WebDriver#getWindowHandle()} is called.
   *
   * @param driver - decorated WebDriver instance
   */
  default void beforeGetWindowHandle(WebDriver driver) {}

  /**
   * This method will be called after {@link WebDriver#getWindowHandle()} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param result - window handle of the current window
   */
  default void afterGetWindowHandle(WebDriver driver, String result) {}

  /**
   * This method will be called before {@link JavascriptExecutor#executeScript(ScriptKey, Object...)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param script - script to be executed
   * @param args - arguments to the script
   */
  default void beforeExecuteScript(WebDriver driver, String script, Object[] args) {}

  /**
   * This method will be called after {@link JavascriptExecutor#executeScript(ScriptKey, Object...)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param script - script to be executed
   * @param args - arguments to the script
   * @param result - result of the script execution
   */
  default void afterExecuteScript(WebDriver driver, String script, Object[] args, Object result) {}

  /**
   * This method will be called before {@link JavascriptExecutor#executeAsyncScript(String, Object...)}is called.
   *
   * @param driver - decorated WebDriver instance
   * @param script - script to be executed
   * @param args - arguments to the script
   */
  default void beforeExecuteAsyncScript(WebDriver driver, String script, Object[] args) {}

  /**
   * This method will be called after {@link JavascriptExecutor#executeAsyncScript(String, Object...)} is called.
   *
   * @param driver - decorated WebDriver instance
   * @param script - script to be executed
   * @param args - arguments to the script
   * @param result - result of the script execution
   */
  default void afterExecuteAsyncScript(
      WebDriver driver, String script, Object[] args, Object result) {}

  /**
   * This method will be called before {@link Actions#perform()}  } is called.
   *
   * @param driver - decorated WebDriver instance
   * @param actions - sequence of actions to be performed
   */
  default void beforePerform(WebDriver driver, Collection<Sequence> actions) {}

  /**
   * This method will be called after {@link Actions#perform()}  } is called.
   *
   * @param driver - decorated WebDriver instance
   * @param actions - sequence of actions to be performed
   */
  default void afterPerform(WebDriver driver, Collection<Sequence> actions) {}

  // WebElement

  /**
   * This method will be called before any method of a {@link WebElement} instance is
   * called.
   *
   * @param element - decorated WebElement instance
   * @param method - method that will be called
   * @param args - arguments for the method
   */
  default void beforeAnyWebElementCall(WebElement element, Method method, Object[] args) {}

  /**
   * This method will be called after any method of a {@link WebElement} instance is
   * called.
   *
   * @param element - decorated WebElement instance
   * @param method - method that was called
   * @param args - arguments for the method
   * @param result - result of the method call
   */
  default void afterAnyWebElementCall(
      WebElement element, Method method, Object[] args, Object result) {}

  /**
   * This action will be performed each time before {@link WebElement#click()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeClick(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#click()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void afterClick(WebElement element) {}

  /**
   * This action will be performed each time before {@link WebElement#submit()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeSubmit(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#submit()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void afterSubmit(WebElement element) {}

  /**
   * This action will be performed each time before {@link WebElement#sendKeys(CharSequence...)} is called.
   *
   * @param element - decorated WebElement instance
   * @param keysToSend - keys to send
   */
  default void beforeSendKeys(WebElement element, CharSequence... keysToSend) {}

  default void afterSendKeys(WebElement element, CharSequence... keysToSend) {}

  /**
   * This action will be performed each time before {@link WebElement#clear()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeClear(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#clear()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void afterClear(WebElement element) {}

  /**
   * This action will be performed each time before {@link WebElement#getTagName()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeGetTagName(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#getTagName()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (tag name of the element)
   */
  default void afterGetTagName(WebElement element, String result) {}

  /**
   * This action will be performed each time before {@link WebElement#getAttribute(String)} is called.
   *
   * @param element - decorated WebElement instance
   * @param name - name of the attribute
   */
  default void beforeGetAttribute(WebElement element, String name) {}

  /**
   * This action will be performed each time after {@link WebElement#getAttribute(String)} is called.
   *
   * @param element - decorated WebElement instance
   * @param name - name of the attribute
   * @param result - result of the method call (value of the attribute)
   */
  default void afterGetAttribute(WebElement element, String name, String result) {}

  /**
   * This action will be performed each time before {@link WebElement#isSelected()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeIsSelected(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#isSelected()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (true if the element is selected)
   */
  default void afterIsSelected(WebElement element, boolean result) {}

  /**
   * This action will be performed each time before {@link WebElement#isEnabled()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeIsEnabled(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#isEnabled()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (true if the element is enabled)
   */
  default void afterIsEnabled(WebElement element, boolean result) {}

  /**
   * This action will be performed each time before {@link WebElement#getText()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeGetText(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#getText()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (text of the element)
   */
  default void afterGetText(WebElement element, String result) {}

  /**
   * This action will be performed each time before {@link WebElement#findElement(By)} is called.
   *
   * @param element - decorated WebElement instance
   * @param locator - locator used to find the elements
   */
  default void beforeFindElement(WebElement element, By locator) {}

  /**
   * This action will be performed each time after {@link WebElement#findElement(By)} is called.
   *
   * @param element - decorated WebElement instance
   * @param locator - locator used to find the elements
   * @param result - found WebElement (can be null)
   */
  default void afterFindElement(WebElement element, By locator, WebElement result) {}

  /**
   * This action will be performed each time before {@link WebElement#findElements(By)} is called.
   *
   * @param element - decorated WebElement instance
   * @param locator - locator used to find the elements
   */
  default void beforeFindElements(WebElement element, By locator) {}

  /**
   * This action will be performed each time after {@link WebElement#findElements(By)} is called.
   *
   * @param element - decorated WebElement instance
   * @param locator - locator used to find the elements
   * @param result - list of found WebElements (can be empty)
   */
  default void afterFindElements(WebElement element, By locator, List<WebElement> result) {}

  /**
   * This action will be performed each time before {@link WebElement#isDisplayed()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeIsDisplayed(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#isDisplayed()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (true if the element is displayed)
   */
  default void afterIsDisplayed(WebElement element, boolean result) {}

  /**
   * This action will be performed each time before {@link WebElement#getLocation()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeGetLocation(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#getLocation()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (coordinates of the top-left corner of the element)
   */
  default void afterGetLocation(WebElement element, Point result) {}

  /**
   * This action will be performed each time before {@link WebElement#getSize()} is called.
   *
   * @param element - decorated WebElement instance
   */
  default void beforeGetSize(WebElement element) {}

  /**
   * This action will be performed each time after {@link WebElement#getSize()} is called.
   *
   * @param element - decorated WebElement instance
   * @param result - result of the method call (size of the element)
   */
  default void afterGetSize(WebElement element, Dimension result) {}

  /**
   * This action will be performed each time before {@link WebElement#getCssValue(String)} is called.
   *
   * @param element - decorated WebElement instance
   * @param propertyName - name of the css property
   */
  default void beforeGetCssValue(WebElement element, String propertyName) {}

  /**
   * This action will be performed each time after {@link WebElement#getCssValue(String)}  is called.
   *
   * @param element - decorated WebElement instance
   * @param propertyName - name of the css property
   * @param result - result of the method call (value of the css property)
   */
  default void afterGetCssValue(WebElement element, String propertyName, String result) {}

  // Navigation

  default void beforeAnyNavigationCall(
      WebDriver.Navigation navigation, Method method, Object[] args) {}

  default void afterAnyNavigationCall(
      WebDriver.Navigation navigation, Method method, Object[] args, Object result) {}

  default void beforeTo(WebDriver.Navigation navigation, String url) {}

  default void afterTo(WebDriver.Navigation navigation, String url) {}

  default void beforeTo(WebDriver.Navigation navigation, URL url) {}

  default void afterTo(WebDriver.Navigation navigation, URL url) {}

  default void beforeBack(WebDriver.Navigation navigation) {}

  default void afterBack(WebDriver.Navigation navigation) {}

  default void beforeForward(WebDriver.Navigation navigation) {}

  default void afterForward(WebDriver.Navigation navigation) {}

  default void beforeRefresh(WebDriver.Navigation navigation) {}

  default void afterRefresh(WebDriver.Navigation navigation) {}

  // Alert

  default void beforeAnyAlertCall(Alert alert, Method method, Object[] args) {}

  default void afterAnyAlertCall(Alert alert, Method method, Object[] args, Object result) {}

  /**
   * This action will be performed each time before {@link Alert#accept()}
   *
   * @param Alert alert
   */
  default void beforeAccept(Alert alert) {}

  /**
   * This action will be performed each time after {@link Alert#accept()}
   *
   * @param Alert alert
   */
  default void afterAccept(Alert alert) {}

  /**
   * This action will be performed each time before {@link Alert#dismiss()}
   *
   * @param Alert alert
   */
  default void beforeDismiss(Alert alert) {}

  /**
   * This action will be performed each time after {@link Alert#dismiss()}
   *
   * @param Alert alert
   */
  default void afterDismiss(Alert alert) {}

  /**
   * This action will be performed each time before {@link Alert#sendKeys(String)}
   *
   * @param Alert alert
   */
  default void beforeGetText(Alert alert) {}

  /**
   * This action will be performed each time after {@link Alert#getText()}
   *
   * @param Alert alert
   */
  default void afterGetText(Alert alert, String result) {}

  /**
   * This action will be performed each time before {@link Alert#sendKeys(String)}
   *
   * @param Alert alert
   * @param String text
   */
  default void beforeSendKeys(Alert alert, String text) {}

  /**
   * This action will be performed each time after {@link Alert#sendKeys(String)}
   *
   * @param Alert alert
   * @param String text
   */
  default void afterSendKeys(Alert alert, String text) {}

  // Options

  default void beforeAnyOptionsCall(WebDriver.Options options, Method method, Object[] args) {}

  default void afterAnyOptionsCall(
      WebDriver.Options options, Method method, Object[] args, Object result) {}


  default void beforeAddCookie(WebDriver.Options options, Cookie cookie) {}

  default void afterAddCookie(WebDriver.Options options, Cookie cookie) {}

  default void beforeDeleteCookieNamed(WebDriver.Options options, String name) {}

  default void afterDeleteCookieNamed(WebDriver.Options options, String name) {}

  default void beforeDeleteCookie(WebDriver.Options options, Cookie cookie) {}

  default void afterDeleteCookie(WebDriver.Options options, Cookie cookie) {}

  default void beforeDeleteAllCookies(WebDriver.Options options) {}

  default void afterDeleteAllCookies(WebDriver.Options options) {}

  default void beforeGetCookies(WebDriver.Options options) {}

  default void afterGetCookies(WebDriver.Options options, Set<Cookie> result) {}

  default void beforeGetCookieNamed(WebDriver.Options options, String name) {}

  default void afterGetCookieNamed(WebDriver.Options options, String name, Cookie result) {}

  // Timeouts

  default void beforeAnyTimeoutsCall(WebDriver.Timeouts timeouts, Method method, Object[] args) {}

  default void afterAnyTimeoutsCall(
      WebDriver.Timeouts timeouts, Method method, Object[] args, Object result) {}

  default void beforeImplicitlyWait(WebDriver.Timeouts timeouts, Duration duration) {}

  default void afterImplicitlyWait(WebDriver.Timeouts timeouts, Duration duration) {}

  default void beforeSetScriptTimeout(WebDriver.Timeouts timeouts, Duration duration) {}

  default void afterSetScriptTimeout(WebDriver.Timeouts timeouts, Duration duration) {}

  default void beforePageLoadTimeout(WebDriver.Timeouts timeouts, Duration duration) {}

  default void afterPageLoadTimeout(WebDriver.Timeouts timeouts, Duration duration) {}

  // Window

  default void beforeAnyWindowCall(WebDriver.Window window, Method method, Object[] args) {}

  default void afterAnyWindowCall(
      WebDriver.Window window, Method method, Object[] args, Object result) {}

  default void beforeGetSize(WebDriver.Window window) {}

  default void afterGetSize(WebDriver.Window window, Dimension result) {}

  default void beforeSetSize(WebDriver.Window window, Dimension size) {}

  default void afterSetSize(WebDriver.Window window, Dimension size) {}

  default void beforeGetPosition(WebDriver.Window window) {}

  default void afterGetPosition(WebDriver.Window window, Point result) {}

  default void beforeSetPosition(WebDriver.Window window, Point position) {}

  default void afterSetPosition(WebDriver.Window window, Point position) {}

  default void beforeMaximize(WebDriver.Window window) {}

  default void afterMaximize(WebDriver.Window window) {}

  default void beforeFullscreen(WebDriver.Window window) {}

  default void afterFullscreen(WebDriver.Window window) {}
}
