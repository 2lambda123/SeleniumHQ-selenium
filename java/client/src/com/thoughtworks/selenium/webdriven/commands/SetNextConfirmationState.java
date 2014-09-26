/*
Copyright 2010 Selenium committers

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package com.thoughtworks.selenium.webdriven.commands;

import com.thoughtworks.selenium.webdriven.SeleneseCommand;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class SetNextConfirmationState extends SeleneseCommand<Void> {
  private final boolean result;

  public SetNextConfirmationState(boolean result) {
    this.result = result;
  }

  @Override
  protected Void handleSeleneseCommand(WebDriver driver, String locator, String value) {
    ((JavascriptExecutor) driver).executeScript(
        "var canUseLocalStorage = false; " +
        "try { canUseLocalStorage = !!window.localStorage; } catch(ex) { /* probe failed */ } " +
        "var canUseJSON = false; " +
        "try { canUseJSON = !!JSON; } catch(ex) { /* probe failed */ } " +
        "if (canUseLocalStorage && canUseJSON) { " +
        "  window.localStorage.setItem('__webdriverNextConfirm', JSON.stringify(arguments[0])); " +
        "} else { " +
        "  window.__webdriverNextConfirm = arguments[0];" +
        "}"
        , result);
    return null;
  }
}
