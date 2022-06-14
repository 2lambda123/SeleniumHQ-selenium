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

from typing import Any, Dict, Mapping, Type, TypeVar, Tuple

from selenium.common.exceptions import (ElementClickInterceptedException,
                                        ElementNotInteractableException,
                                        ElementNotSelectableException,
                                        ElementNotVisibleException,
                                        InsecureCertificateException,
                                        InvalidCoordinatesException,
                                        InvalidElementStateException,
                                        InvalidSessionIdException,
                                        InvalidSelectorException,
                                        ImeNotAvailableException,
                                        ImeActivationFailedException,
                                        InvalidArgumentException,
                                        InvalidCookieDomainException,
                                        JavascriptException,
                                        MoveTargetOutOfBoundsException,
                                        NoSuchCookieException,
                                        NoSuchElementException,
                                        NoSuchFrameException,
                                        NoSuchShadowRootException,
                                        NoSuchWindowException,
                                        NoAlertPresentException,
                                        ScreenshotException,
                                        SessionNotCreatedException,
                                        StaleElementReferenceException,
                                        TimeoutException,
                                        UnableToSetCookieException,
                                        UnexpectedAlertPresentException,
                                        UnknownMethodException,
                                        WebDriverException)


_KT = TypeVar("_KT")
_VT = TypeVar("_VT")


class ErrorCode:
    """
    Error codes defined in the WebDriver wire protocol.
    """
    # Keep in sync with org.openqa.selenium.remote.ErrorCodes and errorcodes.h
    SUCCESS: int = 0
    NO_SUCH_ELEMENT: Tuple[int, str] = (7, 'no such element')
    NO_SUCH_FRAME: Tuple[int, str] = (8, 'no such frame')
    NO_SUCH_SHADOW_ROOT: Tuple[str] = ("no such shadow root",)
    UNKNOWN_COMMAND: Tuple[int, str] = (9, 'unknown command')
    STALE_ELEMENT_REFERENCE: Tuple[int, str] = (10, 'stale element reference')
    ELEMENT_NOT_VISIBLE: Tuple[int, str] = (11, 'element not visible')
    INVALID_ELEMENT_STATE: Tuple[int, str] = (12, 'invalid element state')
    UNKNOWN_ERROR: Tuple[int, str] = (13, 'unknown error')
    ELEMENT_IS_NOT_SELECTABLE: Tuple[int, str] = (15, 'element not selectable')
    JAVASCRIPT_ERROR: Tuple[int, str] = (17, 'javascript error')
    XPATH_LOOKUP_ERROR: Tuple[int, str] = (19, 'invalid selector')
    TIMEOUT: Tuple[int, str] = (21, 'timeout')
    NO_SUCH_WINDOW: Tuple[int, str] = (23, 'no such window')
    INVALID_COOKIE_DOMAIN: Tuple[int, str] = (24, 'invalid cookie domain')
    UNABLE_TO_SET_COOKIE: Tuple[int, str] = (25, 'unable to set cookie')
    UNEXPECTED_ALERT_OPEN: Tuple[int, str] = (26, 'unexpected alert open')
    NO_ALERT_OPEN: Tuple[int, str] = (27, 'no such alert')
    SCRIPT_TIMEOUT: Tuple[int, str] = (28, 'script timeout')
    INVALID_ELEMENT_COORDINATES: Tuple[int, str] = (29, 'invalid element coordinates')
    IME_NOT_AVAILABLE: Tuple[int, str] = (30, 'ime not available')
    IME_ENGINE_ACTIVATION_FAILED: Tuple[int, str] = (31, 'ime engine activation failed')
    INVALID_SELECTOR: Tuple[int, str] = (32, 'invalid selector')
    SESSION_NOT_CREATED: Tuple[int, str] = (33, 'session not created')
    MOVE_TARGET_OUT_OF_BOUNDS: Tuple[int, str] = (34, 'move target out of bounds')
    INVALID_XPATH_SELECTOR: Tuple[int, str] = (51, 'invalid selector')
    INVALID_XPATH_SELECTOR_RETURN_TYPER: Tuple[int, str] = (52, 'invalid selector')

    ELEMENT_NOT_INTERACTABLE: Tuple[int, str] = (60, 'element not interactable')
    INSECURE_CERTIFICATE: Tuple[str] = ('insecure certificate',)
    INVALID_ARGUMENT: Tuple[int, str] = (61, 'invalid argument')
    INVALID_COORDINATES: Tuple[str] = ('invalid coordinates',)
    INVALID_SESSION_ID: Tuple[str] = ('invalid session id',)
    NO_SUCH_COOKIE: Tuple[int, str] = (62, 'no such cookie')
    UNABLE_TO_CAPTURE_SCREEN: Tuple[int, str] = (63, 'unable to capture screen')
    ELEMENT_CLICK_INTERCEPTED: Tuple[int, str] = (64, 'element click intercepted')
    UNKNOWN_METHOD: Tuple[str] = ('unknown method exception',)

    METHOD_NOT_ALLOWED: Tuple[int, str] = (405, 'unsupported operation')


class ErrorHandler:
    """
    Handles errors returned by the WebDriver server.
    """

    def check_response(self, response: Dict[str, Any]) -> None:
        """
        Checks that a JSON response from the WebDriver does not have an error.

        :Args:
         - response - The JSON response from the WebDriver server as a dictionary
           object.

        :Raises: If the response contains an error message.
        """
        status = response.get('status', None)
        if not status or status == ErrorCode.SUCCESS:
            return
        value = None
        message = response.get("message", "")
        if isinstance(status, int):
            value_json = response.get('value', None)
            if value_json and isinstance(value_json, str):
                import json
                try:
                    value = json.loads(value_json)
                    if len(value.keys()) == 1:
                        value = value['value']
                    status = value.get('error', None)
                    if not status:
                        status = value.get("status", ErrorCode.UNKNOWN_ERROR)
                        message = value.get("value") or value.get("message")
                        if not isinstance(message, str):
                            value = message
                            message = message.get('message')
                    else:
                        message = value.get('message', None)
                except ValueError:
                    pass

        exception_class: Type[WebDriverException]
        if status in ErrorCode.NO_SUCH_ELEMENT:
            exception_class = NoSuchElementException
        elif status in ErrorCode.NO_SUCH_FRAME:
            exception_class = NoSuchFrameException
        elif status in ErrorCode.NO_SUCH_SHADOW_ROOT:
            exception_class = NoSuchShadowRootException
        elif status in ErrorCode.NO_SUCH_WINDOW:
            exception_class = NoSuchWindowException
        elif status in ErrorCode.STALE_ELEMENT_REFERENCE:
            exception_class = StaleElementReferenceException
        elif status in ErrorCode.ELEMENT_NOT_VISIBLE:
            exception_class = ElementNotVisibleException
        elif status in ErrorCode.INVALID_ELEMENT_STATE:
            exception_class = InvalidElementStateException
        elif status in ErrorCode.INVALID_SELECTOR \
                or status in ErrorCode.INVALID_XPATH_SELECTOR \
                or status in ErrorCode.INVALID_XPATH_SELECTOR_RETURN_TYPER:
            exception_class = InvalidSelectorException
        elif status in ErrorCode.ELEMENT_IS_NOT_SELECTABLE:
            exception_class = ElementNotSelectableException
        elif status in ErrorCode.ELEMENT_NOT_INTERACTABLE:
            exception_class = ElementNotInteractableException
        elif status in ErrorCode.INVALID_COOKIE_DOMAIN:
            exception_class = InvalidCookieDomainException
        elif status in ErrorCode.UNABLE_TO_SET_COOKIE:
            exception_class = UnableToSetCookieException
        elif status in ErrorCode.TIMEOUT:
            exception_class = TimeoutException
        elif status in ErrorCode.SCRIPT_TIMEOUT:
            exception_class = TimeoutException
        elif status in ErrorCode.UNKNOWN_ERROR:
            exception_class = WebDriverException
        elif status in ErrorCode.UNEXPECTED_ALERT_OPEN:
            exception_class = UnexpectedAlertPresentException
        elif status in ErrorCode.NO_ALERT_OPEN:
            exception_class = NoAlertPresentException
        elif status in ErrorCode.IME_NOT_AVAILABLE:
            exception_class = ImeNotAvailableException
        elif status in ErrorCode.IME_ENGINE_ACTIVATION_FAILED:
            exception_class = ImeActivationFailedException
        elif status in ErrorCode.MOVE_TARGET_OUT_OF_BOUNDS:
            exception_class = MoveTargetOutOfBoundsException
        elif status in ErrorCode.JAVASCRIPT_ERROR:
            exception_class = JavascriptException
        elif status in ErrorCode.SESSION_NOT_CREATED:
            exception_class = SessionNotCreatedException
        elif status in ErrorCode.INVALID_ARGUMENT:
            exception_class = InvalidArgumentException
        elif status in ErrorCode.NO_SUCH_COOKIE:
            exception_class = NoSuchCookieException
        elif status in ErrorCode.UNABLE_TO_CAPTURE_SCREEN:
            exception_class = ScreenshotException
        elif status in ErrorCode.ELEMENT_CLICK_INTERCEPTED:
            exception_class = ElementClickInterceptedException
        elif status in ErrorCode.INSECURE_CERTIFICATE:
            exception_class = InsecureCertificateException
        elif status in ErrorCode.INVALID_COORDINATES:
            exception_class = InvalidCoordinatesException
        elif status in ErrorCode.INVALID_SESSION_ID:
            exception_class = InvalidSessionIdException
        elif status in ErrorCode.UNKNOWN_METHOD:
            exception_class = UnknownMethodException
        else:
            exception_class = WebDriverException
        if not value:
            value = response['value']
        if isinstance(value, str):
            raise exception_class(value)
        if message == "" and 'message' in value:
            message = value['message']

        screen = None  # type: ignore[assignment]
        if 'screen' in value:
            screen = value['screen']

        stacktrace = None
        st_value = value.get('stackTrace') or value.get('stacktrace')
        if st_value:
            if isinstance(st_value, str):
                stacktrace = st_value.split('\n')
            else:
                stacktrace = []
                try:
                    for frame in st_value:
                        line = self._value_or_default(frame, 'lineNumber', '')
                        file = self._value_or_default(frame, 'fileName', '<anonymous>')
                        if line:
                            file = f"{file}:{line}"
                        meth = self._value_or_default(frame, 'methodName', '<anonymous>')
                        if 'className' in frame:
                            meth = "{}.{}".format(frame['className'], meth)
                        msg = "    at %s (%s)"
                        msg = msg % (meth, file)
                        stacktrace.append(msg)
                except TypeError:
                    pass
        if exception_class == UnexpectedAlertPresentException:
            alert_text = None
            if 'data' in value:
                alert_text = value['data'].get('text')
            elif 'alert' in value:
                alert_text = value['alert'].get('text')
            raise exception_class(message, screen, stacktrace, alert_text)  # type: ignore[call-arg]  # mypy is not smart enough here
        raise exception_class(message, screen, stacktrace)

    def _value_or_default(self, obj: Mapping[_KT, _VT], key: _KT, default: _VT) -> _VT:
        return obj[key] if key in obj else default
