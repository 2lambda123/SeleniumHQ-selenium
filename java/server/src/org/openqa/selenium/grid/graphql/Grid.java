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

package org.openqa.selenium.grid.graphql;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.openqa.selenium.grid.data.DistributorStatus;
import org.openqa.selenium.grid.distributor.Distributor;
import org.openqa.selenium.grid.sessionmap.SessionMap;
import org.openqa.selenium.internal.Require;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

public class Grid {

  private final URI uri;
  private final Supplier<DistributorStatus> distributorStatus;
  private int sessionCount;

  public Grid(Distributor distributor, URI uri, SessionMap sessions) {
    Require.nonNull("Distributor", distributor);
    this.uri = Require.nonNull("Grid's public URI", uri);
    this.sessionCount = sessions.getCount();

    this.distributorStatus = Suppliers.memoize(distributor::getStatus);
  }

  public URI getUri() {
    return uri;
  }

  public List<Node> getNodes() {
    return distributorStatus.get().getNodes().stream()
      .map(summary -> new Node(summary.getNodeId(),
                               summary.getUri(),
                               summary.isUp(),
                               summary.getMaxSessionCount(),
                               summary.getStereotypes()))
      .collect(ImmutableList.toImmutableList());
  }

//  public List<Session> getSessions() {
//    return distributorStatus.get().get
//        .map(summary -> new Session(summary.getNodeId())
//        .collect(ImmutableList.toImmutableList());
////    allSessions.getAllSessions().forEach(s -> value.add(
////        ImmutableMap.of("id", s.getId().toString(), "capabilities", s.getCapabilities())));
//  }

  public int getTotalSlots() {
    return distributorStatus.get().getNodes().stream()
      .map(summary -> {
        int slotCount = summary.getStereotypes().values().stream().mapToInt(i -> i).sum();
        return Math.min(summary.getMaxSessionCount(), slotCount);
      })
      .mapToInt(i -> i)
      .sum();
  }

  public int getUsedSlots() {
    return distributorStatus.get().getNodes().stream()
      .map(summary -> summary.getUsedStereotypes().values().stream().mapToInt(i -> i).sum())
      .mapToInt(i -> i)
      .sum();
  }
}
