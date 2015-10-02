﻿// <copyright file="RemoteLocationContext.cs" company="WebDriver Committers">
// Licensed to the Software Freedom Conservancy (SFC) under one
// or more contributor license agreements. See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership. The SFC licenses this file
// to you under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// </copyright>

using System;
using System.Collections.Generic;
using OpenQA.Selenium.HTML5;

namespace OpenQA.Selenium.Remote.HTML5
{
    /// <summary>
    /// Defines the interface through which the user can manipulate browser location.
    /// </summary>
    public class RemoteLocationContext : ILocationContext
    {
        private RemoteWebDriver driver;

        /// <summary>
        /// Initializes a new instance of the <see cref="RemoteLocationContext"/> class.
        /// </summary>
        /// <param name="driver">The <see cref="RemoteWebDriver"/> for which the application cache will be managed.</param>
        public RemoteLocationContext(RemoteWebDriver driver)
        {
            this.driver = driver;
        }

        /// <summary>
        /// Return current browser location
        /// </summary>
        /// <returns>Current browser <see cref="Location">location</see></returns>
        public Location Location()
        {
            Response commandResponse = driver.InternalExecute(DriverCommand.GetLocation, null);
            Dictionary<string, object> location = commandResponse.Value as Dictionary<string, object>;
            if (location != null)
            {
                return new Location(Double.Parse(location["latitude"].ToString()), Double.Parse(location["longitude"].ToString()), Double.Parse(location["altitude"].ToString()));
            }
            else
            {
                return null;
            }
        }

        /// <summary>
        /// Sets current location to the given one
        /// </summary>
        /// <param name="location">location to set browser to</param>
        public void SetLocation(Location location)
        {
            Dictionary<string, object> loc = new Dictionary<string, object>();
            loc.Add("latitude", location.GetLatitude());
            loc.Add("longitude", location.GetLongitude());
            loc.Add("altitude", location.GetAltitude());

            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("location", loc);
            driver.InternalExecute(DriverCommand.SetLocation, parameters);
        }
    }
}
