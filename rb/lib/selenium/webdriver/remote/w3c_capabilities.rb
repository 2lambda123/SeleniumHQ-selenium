# encoding: utf-8
#
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

module Selenium
  module WebDriver
    module Remote
      #
      # Specification of the desired and/or actual capabilities of the browser that the
      # server is being asked to create.
      #

      # TODO - uncomment when Mozilla fixes this:
      # https://bugzilla.mozilla.org/show_bug.cgi?id=1326397
      class W3CCapabilities
        KNOWN = [:browser_name,
                 :browser_version,
                 :platform_name,
                 :platform_version,
                 :accept_insecure_certs,
                 :page_load_strategy,
                 :proxy,
                 :remote_session_id,
                 :accessibility_checks,
                 :rotatable,
                 :device,
                 :implicit_timeout,
                 :page_load_timeout,
                 :script_timeout].freeze

        KNOWN.each do |key|
          define_method key do
            @capabilities.fetch(key)
          end

          define_method "#{key}=" do |value|
            @capabilities[key] = value
          end
        end

        #
        # Backward compatibility
        #

        alias_method :version, :browser_version
        alias_method :version=, :browser_version=
        alias_method :platform, :platform_name
        alias_method :platform=, :platform_name=

        #
        # Convenience methods for the common choices.
        #

        class << self
          def edge(opts = {})
            new({browser_name: 'MicrosoftEdge',
                 platform_name: :windows}.merge(opts))
          end

          def firefox(opts = {})
            define_method(:firefox_options) { @capabilities[:firefox_options] ||= {} }
            define_method("firefox_options=") { |value| @capabilities[:firefox_options] = value }
            define_method(:firefox_profile) { firefox_options['profile'] }
            define_method("firefox_profile=") { |value| firefox_options['profile'] = value.as_json['zip'] }
            alias_method :profile=, :firefox_profile=
            alias_method :options=, :firefox_options=

            opts[:browser_version] = opts.delete(:version) if opts.key?(:version)
            opts[:platform_name] = opts.delete(:platform) if opts.key?(:platform)
            timeouts = {}
            timeouts['implicit'] = opts.delete(:implicit_timeout) if opts.key?(:implicit_timeout)
            timeouts['page load'] = opts.delete(:page_load_timeout) if opts.key?(:page_load_timeout)
            timeouts['script'] = opts.delete(:script_timeout) if opts.key?(:script_timeout)
            opts[:timeouts] = timeouts unless timeouts.empty?
            new({browser_name: 'firefox', marionette: true}.merge(opts))
          end

          alias_method :ff, :firefox

          def w3c?(opts = {})
            opts[:marionette] != false &&
                (!opts[:desired_capabilities] || opts[:desired_capabilities][:marionette] != false)
          end

          #
          # @api private
          #

          def json_create(data)
            data = data.dup

            caps = new
            caps.browser_name = data.delete('browserName')
            caps.browser_version = data.delete('browserVersion') if data.key?('browserVersion')
            caps.platform_name = data.delete('platformName') if data.key?('platformName')
            caps.platform_version = data.delete('platformVersion') if data.key?('platformVersion')
            caps.accept_insecure_certs = data.delete('acceptInsecureCerts') if data.key?('acceptInsecureCerts')
            caps.page_load_strategy = data.delete('pageLoadStrategy') if data.key?('pageLoadStrategy')
            timeouts = data.delete('timeouts') if data.key?('timeouts')
            caps.implicit_timeout = timeouts['implicit'] if timeouts
            caps.page_load_timeout = timeouts['page load'] if timeouts
            caps.script_timeout = timeouts['script'] if timeouts

            proxy = data.delete('proxy')
            caps.proxy = Proxy.json_create(proxy) unless proxy.nil? || proxy.empty?

            # Remote Server Specific
            caps[:remote_session_id] = data.delete('webdriver.remote.sessionid') if data.key?('webdriver.remote.sessionid')

            # Marionette Specific
            caps[:accessibility_checks] = data.delete('moz:accessibilityChecks') if data.key?('moz:accessibilityChecks')
            caps[:firefox_profile] = data.delete('moz:profile') if data.key?('moz:profile')
            caps.firefox_options = data.delete('moz:firefoxOptions') if data.key?('moz:firefoxOptions')
            caps[:rotatable] = data.delete('rotatable') if data.key?('rotatable')
            caps[:device] = data.delete('device') if data.key?('device')
            caps[:marionette] = data.delete('marionette') if data.key?('marionette')

            # any remaining pairs will be added as is, with no conversion
            caps.merge!(data)
            caps
          end
        end

        # @param [Hash] opts
        # @option :browser_name             [String] required browser name
        # @option :browser_version          [String] required browser version number
        # @option :platform_name            [Symbol] one of :any, :win, :mac, or :x
        # @option :platform_version         [String] required platform version number
        # @option :accept_insecure_certs    [Boolean] does the driver accept SSL Cerfifications?
        # @option :proxy                    [Selenium::WebDriver::Proxy, Hash] proxy configuration
        #
        # @api public
        #

        def initialize(opts = {})
          @capabilities = opts
          self.proxy = opts.delete(:proxy)
        end

        #
        # Allows setting arbitrary capabilities.
        #

        def []=(key, value)
          @capabilities[key] = value
        end

        def [](key)
          @capabilities[key]
        end

        def merge!(other)
          if other.respond_to?(:capabilities, true) && other.capabilities.is_a?(Hash)
            @capabilities.merge! other.capabilities
          elsif other.is_a? Hash
            @capabilities.merge! other
          else
            raise ArgumentError, 'argument should be a Hash or implement #capabilities'
          end
        end

        def proxy=(proxy)
          case proxy
          when Hash
            @capabilities[:proxy] = Proxy.new(proxy)
          when Proxy, nil
            @capabilities[:proxy] = proxy
          else
            raise TypeError, "expected Hash or #{Proxy.name}, got #{proxy.inspect}:#{proxy.class}"
          end
        end

        # @api private
        #

        def as_json(*)
          hash = {}

          @capabilities.each do |key, value|
            case key
            when :platform
              hash['platform'] = value.to_s.upcase
            when :proxy
              hash['proxy'] = value.as_json if value
            when :firefox_options
              hash['moz:firefoxOptions'] = value
            when String, :firefox_binary
              hash[key.to_s] = value
            when Symbol
              hash[camel_case(key.to_s)] = value
            else
              raise TypeError, "expected String or Symbol, got #{key.inspect}:#{key.class} / #{value.inspect}"
            end
          end

          hash
        end

        def to_json(*)
          JSON.generate as_json
        end

        def ==(other)
          return false unless other.is_a? self.class
          as_json == other.as_json
        end

        alias_method :eql?, :==

        protected

        attr_reader :capabilities

        private

        def camel_case(str)
          str.gsub(/_([a-z])/) { Regexp.last_match(1).upcase }
        end
      end # W3CCapabilities
    end # Remote
  end # WebDriver
end # Selenium
