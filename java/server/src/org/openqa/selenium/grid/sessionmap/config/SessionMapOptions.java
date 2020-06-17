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

package org.openqa.selenium.grid.sessionmap.config;

import org.openqa.selenium.grid.config.Config;
import org.openqa.selenium.grid.config.ConfigException;
import org.openqa.selenium.grid.sessionmap.SessionMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.logging.Logger;

public class SessionMapOptions {

  private static final String SESSIONS_SECTION = "sessions";

  private static final Logger LOG = Logger.getLogger(SessionMapOptions.class.getName());
  private static final String DEFAULT_SESSION_MAP = "org.openqa.selenium.grid.sessionmap.remote.RemoteSessionMap";
  private final Config config;

  public SessionMapOptions(Config config) {
    this.config = config;
  }

  public URI getSessionMapUri() {
    Optional<URI> host = config.get(SESSIONS_SECTION, "host").map(str -> {
      try {
        return new URI(str);
      } catch (URISyntaxException e) {
        throw new ConfigException("Session map server URI is not a valid URI: " + str);
      }
    });

    if (host.isPresent()) {
      return host.get();
    }

    Optional<Integer> port = config.getInt(SESSIONS_SECTION, "port");
    Optional<String> hostname = config.get(SESSIONS_SECTION, "hostname");

    if (!(port.isPresent() && hostname.isPresent())) {
      throw new ConfigException("Unable to determine host and port for the session map server");
    }

    try {
      return new URI(
          "http",
          null,
          hostname.get(),
          port.get(),
          "",
          null,
          null);
    } catch (URISyntaxException e) {
      throw new ConfigException(
          "Session map server uri configured through host (%s) and port (%d) is not a valid URI",
          hostname.get(),
          port.get());
    }
  }

  public SessionMap getSessionMap() {
<<<<<<< HEAD
    return  config.getClass(SESSIONS_SECTION, "implementation", SessionMap.class, DEFAULT_SESSION_MAP)
        .get()
        .cast(SessionMap.class);
=======
    return  (SessionMap) config.getClass(SESSIONS_SECTION, "implementation", SessionMap.class, DEFAULT_SESSION_MAP);
>>>>>>> upstream/master
  }
}
