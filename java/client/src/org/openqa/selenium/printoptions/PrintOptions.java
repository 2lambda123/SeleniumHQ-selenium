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

package org.openqa.selenium.printoptions;

import java.util.HashMap;
import java.util.Map;

public class PrintOptions {

  public enum PrintOrientation {
    Portrait,
    Landscape
  }
  private PrintOrientation orientation = PrintOrientation.Portrait;
  private double scale = 1.0;
  private boolean background = false;
  private boolean shrinkToFit = true;
  private PageSize pageSize = new PageSize();
  private PageMargin pageMargin = new PageMargin();
  private String[] pageRanges;

  public PrintOrientation getOrientation() {
    return this.orientation;
  }

  public void setOrientation(PrintOrientation orientation) {
    this.orientation = orientation;
  }

  public String[] getPageRanges() {
    return this.pageRanges;
  }

  public void setPageRanges(String[] ranges) {
    this.pageRanges = ranges;
  }

  public void setBackground(boolean background) {
    this.background = background;
  }

  public boolean getBackground() {
    return this.background;
  }

  public void setScale(double scale) {
    if (scale < 0.1 || scale > 2) {
      throw new IllegalArgumentException("Scale value should be between 0.1 and 2");
    }
    this.scale = scale;
  }

  public double getScale() {
    return this.scale;
  }

  public boolean getShrinkToFit() {
    return this.shrinkToFit;
  }

  public void setShrinkToFit(boolean value) {
    this.shrinkToFit = value;
  }

  public void setPageSize(PageSize pageSize) {
    this.pageSize = pageSize;
  }

  public void setPageMargin(PageMargin margin) {
    this.pageMargin = margin;
  }

  public PageSize getPageSize() {
    return this.pageSize;
  }

  public PageMargin getPageMargin() {
    return this.pageMargin;
  }

  public Map<String, Object> to_json() {
    Map<String, Object> printOptions = new HashMap<>();

    printOptions.put("scale", this.scale);
    printOptions.put("background", this.background);
    printOptions.put("shrinkToFit", this.shrinkToFit);
    printOptions.put("page", this.pageSize.to_json());
    printOptions.put("margin", this.pageMargin.to_json());


    return printOptions;
  }
}
