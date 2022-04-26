# frozen_string_literal: true

# Licensed to the Software Freedom Conservancy (SFC) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The SFC licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

require File.expand_path('../spec_helper', __dir__)

def self.from(id, encoded_body, params)
  new(
    id: id,
    code: params['responseStatusCode'],
    body: (Base64.strict_decode64(encoded_body) if encoded_body),
    headers: (params['responseHeaders'] ||  []).each_with_object({}) do |header, hash|
      hash[header['name']] = header['value']
    end
  )
end

module Selenium
  module WebDriver
    class DevTools
      describe Response do
        describe '.from' do
          it 'should set the headers correctly' do
            params = {'responseHeaders' => [{'name' => 'Connection', 'value' => 'Keep-Alive'}]}
            response = Response.from(1, nil, params)
            expect(response.headers).to eq({'Connection' => 'Keep-Alive'})
          end
          
          it 'should not raise error on empty responseHeaders' do
            expect { 
              Response.from(1, nil, {})
            }.not_to raise_error
          end
        end
      end # Request
    end # DevTools
  end # WebDriver
end # Selenium
