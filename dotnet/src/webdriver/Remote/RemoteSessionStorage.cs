﻿// <copyright file="RemoteSessionStorage.cs" company="WebDriver Committers">
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
using System.Collections.ObjectModel;
using OpenQA.Selenium.HTML5;
using System.Collections.Generic;

namespace OpenQA.Selenium.Remote.HTML5
{
    /// <summary>
    /// Defines the interface through which the user can manipulate session storage.
    /// </summary>
    public class RemoteSessionStorage : ISessionStorage
    {
        private RemoteWebDriver driver;

        /// <summary>
        /// Initializes a new instance of the <see cref="RemoteSessionStorage"/> class.
        /// </summary>
        /// <param name="driver"></param>
        public RemoteSessionStorage(RemoteWebDriver driver)
        {
            this.driver = driver;
        }

        /// <summary>
        /// Returns session storage value given a key.
        /// </summary>
        /// <param name="key"></param>
        /// <returns>A session storage <see cref="string"/> value given a key, if present, otherwise return null.</returns>
        public string GetItem(string key)
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("key", key);
            Response commandResponse = driver.InternalExecute(DriverCommand.GetLocalStorageItem, parameters);
            if (commandResponse.Value == null)
            {
                return null;
            }
            else
            {
                return commandResponse.Value.ToString();
            }
        }

        /// <summary>
        /// Returns a read-only list of session storage keys.
        /// </summary>
        /// <returns>A <see cref="ReadOnlyCollection{T}">read-only list</see> of session storage keys.</returns>
        public ReadOnlyCollection<string> KeySet()
        {
            List<string> result = new List<string>();
            Response commandResponse = driver.InternalExecute(DriverCommand.GetLocalStorageKeys, null);
            object[] keys = commandResponse.Value as object[];
            foreach (string key in keys)
            {
                result.Add(key);
            }
            return result.AsReadOnly();
        }

        /// <summary>
        /// Sets session storage entry using given key/value pair.
        /// </summary>
        /// <param name="key">Session storage key</param>
        /// <param name="value">Session storage value</param>
        public void SetItem(string key, string value)
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("key", key);
            parameters.Add("value", value);
            driver.InternalExecute(DriverCommand.SetLocalStorageItem, parameters);
        }

        /// <summary>
        /// Removes session storage entry for the given key.
        /// </summary>
        /// <param name="key">key to be removed from the list</param>
        /// <returns>Response value <see cref="string"/>for the given key.</returns>
        public string RemoveItem(string key)
        {
            Dictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("key", key);
            Response commandResponse = driver.InternalExecute(DriverCommand.RemoveLocalStorageItem, parameters);
            if (commandResponse.Value == null)
            {
                return null;
            }
            else
            {
                return commandResponse.Value.ToString();
            }
        }

        /// <summary>
        /// Removes all entries from the session storage.
        /// </summary>
        public void Clear()
        {
            driver.InternalExecute(DriverCommand.ClearLocalStorage, null);
        }

        /// <summary>
        /// Returns size of the session storage.
        /// </summary>
        /// <returns>Size <see cref="int"/>of the session storage.</returns>
        public int Size()
        {
            Response commandResponse = driver.InternalExecute(DriverCommand.GetLocalStorageSize, null);
            return Int32.Parse(commandResponse.Value.ToString());
        }
    }
}
