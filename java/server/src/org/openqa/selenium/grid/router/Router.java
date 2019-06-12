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

import org.openqa.selenium.grid.distributor.Distributor;
import org.openqa.selenium.grid.sessionmap.SessionMap;
import org.openqa.selenium.grid.web.CommandHandler;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.http.Route;
import org.openqa.selenium.remote.tracing.DistributedTracer;

import java.io.IOException;
import java.util.function.Predicate;

import static org.openqa.selenium.remote.http.Route.combine;
import static org.openqa.selenium.remote.http.Route.get;
import static org.openqa.selenium.remote.http.Route.matching;

/**
 * A simple router that is aware of the selenium-protocol.
 */
public class Router implements Predicate<HttpRequest>, CommandHandler {

  private final Route routes;

  public Router(
      DistributedTracer tracer,
      HttpClient.Factory clientFactory,
      SessionMap sessions,
      Distributor distributor) {
    routes = combine(
        get("/status")
            .to(() -> new GridStatusHandler(new Json(), clientFactory, distributor)),
        matching(sessions).to(() -> sessions),
        matching(distributor).to(() -> distributor),
        matching(req -> req.getUri().startsWith("/session/"))
            .to(() -> new HandleSession(tracer, clientFactory, sessions)));
  }

  @Override
  public boolean test(HttpRequest req) {
    return routes.matches(req);
  }

  @Override
  public void execute(HttpRequest req, HttpResponse resp) throws IOException {
    copyResponse(routes.execute(req), resp);
  }
}
