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

package org.openqa.selenium.grid.data;

import org.openqa.selenium.events.Event;
import org.openqa.selenium.events.EventListener;
import org.openqa.selenium.events.EventName;
import org.openqa.selenium.internal.Require;
import org.openqa.selenium.json.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Consumer;

public class NodesRemovedEvent extends Event {

  private static final Type NODEID_SET_TYPE = new TypeToken<Set<NodeId>>() {}.getType();
  private static final EventName NODE_REMOVED_EVENT = new EventName("nodes-removed");

  public NodesRemovedEvent(Set<NodeId> nodes) {
    super(NODE_REMOVED_EVENT, nodes);
  }

  public static EventListener<Set<NodeId>> listener(Consumer<Set<NodeId>> handler) {
    Require.nonNull("Handler", handler);

    return new EventListener<>(NODE_REMOVED_EVENT, NODEID_SET_TYPE , handler);
  }
}
