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

package org.openqa.grid.web.servlet;

import static org.openqa.selenium.json.Json.MAP_TYPE;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;

import org.openqa.grid.internal.GridRegistry;
import org.openqa.grid.internal.RemoteProxy;
import org.openqa.grid.internal.TestSlot;
import org.openqa.selenium.json.Json;
import org.openqa.selenium.json.JsonException;
import org.openqa.selenium.json.JsonInput;
import org.openqa.selenium.json.JsonOutput;
import org.openqa.selenium.remote.CapabilityType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collector;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.reducing;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * API to query the hub config remotely.
 *
 * use the API by sending a GET to grid/api/hub/
 * with the content of the request in JSON,specifying the
 * parameters you're interesting in, for instance, to get
 * the timeout of the hub and the registered servlets :
 *
 * {"configuration":
 *      [
 *      "timeout",
 *      "servlets"
 *      ]
 * }
 *
 * alternatively you can use a query string ?configuration=timeout,servlets
 *
 * if no param is specified, all params known to the hub are returned.
 *
 */
public class HubStatusServlet extends RegistryBasedServlet {

  private final Json json = new Json();

  public HubStatusServlet() {
    this(null);
  }

  public HubStatusServlet(GridRegistry registry) {
    super(registry);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    process(request, response, new HashMap<>());
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if (req.getInputStream() != null) {
      Map<String, Object> json = getRequestJSON(req);
      process(req, resp, json);
    } else {
      process(req, resp, new HashMap<>());
    }

  }

  protected void process(
      HttpServletRequest request,
      HttpServletResponse response,
      Map<String, Object> requestJson)
      throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.setStatus(200);
    try (Writer writer = response.getWriter();
         JsonOutput out = json.newOutput(writer)) {
      Map<String, Object> res = getResponse(request, requestJson);
      out.write(res);
    }
  }

  private Map<String, Object> getResponse(
      HttpServletRequest request,
      Map<String, Object> requestJSON) {
    Map<String, Object> res = new TreeMap<>();
    res.put("success", true);

    try {
        List<String> keysToReturn = null;

        if (request.getParameter("configuration") != null && !"".equals(request.getParameter("configuration"))) {
          keysToReturn = Arrays.asList(request.getParameter("configuration").split(","));
        } else if (requestJSON != null && requestJSON.containsKey("configuration")) {
          //noinspection unchecked
          keysToReturn = (List<String>) requestJSON.get("configuration");
        }

        GridRegistry registry = getRegistry();
        Map<String, Object> config = registry.getHub().getConfiguration().toJson();
        for (Map.Entry<String, Object> entry : config.entrySet()) {
          if (IsKeyPresentIn(keysToReturn, entry.getKey())) {
            res.put(entry.getKey(), entry.getValue());
          }
        }
        if (IsKeyPresentIn(keysToReturn, "newSessionRequestCount")) {
          res.put("newSessionRequestCount", registry.getNewSessionRequestCount());
        }

        if (IsKeyPresentIn(keysToReturn, "slotCounts")) {
          res.put("slotCounts", getSlotCounts());
        }
        if (keysToReturn != null && keysToReturn.contains("nodes")) {
          res.put("nodes", getNodesInfo());
        }
    } catch (Exception e) {
      res.remove("success");
      res.put("success", false);
      res.put("msg", e.getMessage());
    }
    return res;

  }

  private Map<String, Object> getSlotCounts() {
    int totalSlots = 0;
    int usedSlots = 0;

    for (RemoteProxy proxy : getRegistry().getAllProxies()) {
      totalSlots += Math.min(proxy.getMaxNumberOfConcurrentTestSessions(), proxy.getTestSlots().size());
      usedSlots += proxy.getTotalUsed();
    }

    return ImmutableSortedMap.of(
        "free", totalSlots - usedSlots,
        "total", totalSlots);
  }

  private Map<String, Object> getRequestJSON(HttpServletRequest request) throws IOException {
    Json json = new Json();
    try (BufferedReader rd = new BufferedReader(new InputStreamReader(request.getInputStream()));
         JsonInput jin = json.newInput(rd)) {
      return jin.read(MAP_TYPE);
    } catch (JsonException e) {
      throw new IOException(e);
    }
  }

  private static boolean IsKeyPresentIn(List<String> keys, String key) {
    return keys == null || keys.isEmpty() || keys.contains(key);
  }

  private List<Map<String, Object>> getNodesInfo() {
    List<RemoteProxy> proxies = getRegistry().getAllProxies().getSorted();
    return proxies.stream().map(this::getNodeInfo).collect(toList());
  }

  private Map<String, Object> getNodeInfo(RemoteProxy remoteProxy) {
    return ImmutableSortedMap.of(
        "Id", remoteProxy.getId(),
        "browsers", getInfoFromAllSlotsInNode(remoteProxy.getTestSlots())
    );
  }

  private List<Map<String, Object>> getInfoFromAllSlotsInNode(List<TestSlot> slots) {
    List<Map<String, Object>> browsers = Lists.newArrayList();
    Map<String, List<TestSlot>>
        slotsInfo = slots.stream().collect(groupingBy(HubStatusServlet::getBrowser));
    for (Map.Entry<String, List<TestSlot>> each : slotsInfo.entrySet()) {
      String key = each.getKey();
      Map<String, Object> value = getSlotInfoPerBrowserFlavor(each.getValue());
      browsers.add(ImmutableSortedMap.of("browser", key, "slots", value));
    }
    return browsers;
  }

  private Map<String, Object> getSlotInfoPerBrowserFlavor(List<TestSlot> slots) {
    Map<String, Integer> byStatus = slots.stream().collect(groupingBy(this::status, counting()));
    int busy = byStatus.computeIfAbsent("busy", status -> 0);
    int free = byStatus.computeIfAbsent("free", status -> 0);
    int total = busy + free;

    return ImmutableSortedMap.of("total", total, "busy", busy);
  }

  private String status(TestSlot slot) {
    if (slot.getSession() == null) {
      return "free";
    }
    return "busy";
  }

  private static String getBrowser(TestSlot slot) {
    return slot.getCapabilities().get(CapabilityType.BROWSER_NAME).toString();
  }

  private static <T> Collector<T, ?, Integer> counting() {
    return reducing(0, e -> 1, Integer::sum);
  }

}
