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
package org.openqa.selenium.devtools.page.model;

import org.openqa.selenium.Beta;
import org.openqa.selenium.devtools.IO.model.StreamHandle;
import org.openqa.selenium.json.JsonInput;

import java.util.Objects;

public class PrintToPDF {

  /**
   * Base64-encoded pdf data. Empty if |returnAsStream| is specified.
   */
  private final String data;
  /**
   * A handle of the stream that holds resulting PDF data.
   */
  @Beta
  private final StreamHandle stream;

  public PrintToPDF(String data, StreamHandle stream) {
    this.data = Objects.requireNonNull(data, "data is missing for PrintToPDF ");
    this.stream = stream;
  }

  private static PrintToPDF fromJson(JsonInput input) {
    String data = input.nextString();
    StreamHandle stream = null;
    while (input.hasNext()) {
      switch (input.nextName()) {
        case "stream":
          stream = input.read(StreamHandle.class);
          break;
        default:
          input.skipValue();
          break;
      }
    }
    return new PrintToPDF(data, stream);
  }
}
