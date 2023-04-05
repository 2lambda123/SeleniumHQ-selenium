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
import typing

from selenium.webdriver.chromium import service
from selenium.webdriver.common.options import BaseOptions

DEFAULT_EXECUTABLE_PATH = "chromedriver"


class Service(service.ChromiumService):
    """A Service class that is responsible for the starting and stopping of
    `chromedriver`.

    :param executable_path: install path of the chromedriver executable, defaults to `chromedriver`.
    :param port: Port for the service to run on, defaults to 0 where the operating system will decide.
    :param service_args: (Optional) List of args to be passed to the subprocess when launching the executable.
    :param log_path: (Optional) String to be passed to the executable as `--log-path`.
    :param env: (Optional) Mapping of environment variables for the new process, defaults to `os.environ`.
    :param options - this takes an instance of ChromeOptions
    """

    def __init__(
        self,
        executable_path: str = DEFAULT_EXECUTABLE_PATH,
        port: int = 0,
        service_args: typing.Optional[typing.List[str]] = None,
        log_path: typing.Optional[str] = None,
        env: typing.Optional[typing.Mapping[str, str]] = None,
        options: BaseOptions = None,
        **kwargs,
    ) -> None:
        super().__init__(
            executable_path=executable_path,
            port=port,
            service_args=service_args,
            log_path=log_path,
            env=env,
            start_error_message="Please see https://chromedriver.chromium.org/home",
            options=options,
            **kwargs,
        )
