// Copyright 2011 Software Freedom Conservancy
// Licensed under the Apache License, Version 2.0 (the "License");
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

#include "IESession.h"
#include "IECommandExecutor.h"
#include "logging.h"
#include "interactions.h"

namespace webdriver {

IESession::IESession() {
}

IESession::~IESession(void) {
}

void IESession::Initialize(void* init_params) {
  LOG(TRACE) << "Entering IESession::Initialize";

  HANDLE mutex = ::CreateMutex(NULL, FALSE, MUTEX_NAME);
  if (mutex != NULL) {
    // Wait for up to the timeout (currently 30 seconds) for other sessions
    // to completely initialize.
    DWORD mutex_wait_status = ::WaitForSingleObject(mutex, MUTEX_WAIT_TIMEOUT);
    if (mutex_wait_status == WAIT_ABANDONED) {
      LOG(WARN) << "Acquired mutex, but received wait abandoned status. This "
                << "could mean the process previously owning the mutex was "
                << "unexpectedly terminated.";
    } else if (mutex_wait_status == WAIT_TIMEOUT) {
      LOG(WARN) << "Could not acquire mutex within the timeout. Multiple "
                << "instances may hang or behave unpredictably";
    } else if (mutex_wait_status == WAIT_OBJECT_0) {
      LOG(DEBUG) << "Mutex acquired for session initalization";
    } else if (mutex_wait_status == WAIT_FAILED) {
      LOGERR(WARN) << "Mutex acquire waiting is failed";
    }
  } else {
    LOGERR(WARN) << "Could not create session initialization mutex. Multiple " 
                 << "instances will behave unpredictably. ";
  }

  SessionParameters* params = reinterpret_cast<SessionParameters*>(init_params);
  int port = params->port;
  std::string launch_api = params->launch_api;
  std::string ie_switches = params->ie_switches;

  IECommandExecutorThreadContext ctx = IECommandExecutorThreadContext();
  ctx.port = port;
  ctx.launch_api = launch_api;
  ctx.ie_switches = ie_switches;
  ctx.hwnd = NULL;

  unsigned int thread_id = 0;

  HANDLE event_handle = ::CreateEvent(NULL, TRUE, FALSE, EVENT_NAME);
  if (event_handle == NULL) {
    LOGERR(DEBUG) << "Unable to create event " << EVENT_NAME;
  }
  HANDLE thread_handle = reinterpret_cast<HANDLE>(_beginthreadex(NULL,
                                                                 0,
                                                                 &IECommandExecutor::ThreadProc,
                                                                 reinterpret_cast<void*>(&ctx),
                                                                 0,
                                                                 &thread_id));
  if (event_handle != NULL) {
    DWORD thread_wait_status = ::WaitForSingleObject(event_handle, THREAD_WAIT_TIMEOUT);
    if (thread_wait_status != WAIT_OBJECT_0) {
      LOGERR(WARN) << "Unable to wait until created thread notification: '" << thread_wait_status << "'.";
    }
    ::CloseHandle(event_handle);
  }

  if (thread_handle != NULL) {
    ::CloseHandle(thread_handle);
  } else {
    LOG(DEBUG) << "Unable to create thread for command executor";
  }

  std::string session_id = "";
  if (ctx.hwnd != NULL) {
    LOG(TRACE) << "Created thread for command executor returns HWND: '" << ctx.hwnd << "'";

    // Send INIT to window with port as WPARAM
    // It is already deprecated
    ::SendMessage(ctx.hwnd,
                  WD_INIT,
                  static_cast<WPARAM>(port),
                  NULL);

    vector<wchar_t> window_text_buffer(37);
    ::GetWindowText(ctx.hwnd, &window_text_buffer[0], 37);
    session_id = StringUtilities::ToString(&window_text_buffer[0]);
    LOG(TRACE) << "Session id is retrived from command executor window: '" << session_id << "'";
  } else {
    LOG(DEBUG) << "Created thread does not return HWND of created session";
  }

  if (mutex != NULL) {
    LOG(DEBUG) << "Releasing session initialization mutex";
    ::ReleaseMutex(mutex);
    ::CloseHandle(mutex);
  }

  this->executor_window_handle_ = ctx.hwnd;
  this->set_session_id(session_id);
}

void IESession::ShutDown(void) {
  LOG(TRACE) << "Entering IESession::ShutDown";

  // Kill the background thread first - otherwise the IE process crashes.
  stopPersistentEventFiring();

  // Don't terminate the thread until the browsers have all been deallocated.
  int is_quitting = static_cast<int>(::SendMessage(this->executor_window_handle_,
                                                   WD_GET_QUIT_STATUS,
                                                   NULL,
                                                   NULL));
  int retry_count = 50;
  while (is_quitting > 0 && --retry_count > 0) {
    ::Sleep(100);
    is_quitting = static_cast<int>(::SendMessage(this->executor_window_handle_,
                                                 WD_GET_QUIT_STATUS,
                                                 NULL,
                                                 NULL));
  }

  DWORD process_id;
  DWORD thread_id = ::GetWindowThreadProcessId(this->executor_window_handle_,
                                               &process_id);
  HANDLE thread_handle = ::OpenThread(SYNCHRONIZE, FALSE, thread_id);
  ::SendMessage(this->executor_window_handle_, WM_CLOSE, NULL, NULL);
  if (thread_handle != NULL) {
    DWORD wait_result = ::WaitForSingleObject(thread_handle, 30000);
    if (wait_result != WAIT_OBJECT_0) {
      LOG(DEBUG) << "Waiting for thread to end returned " << wait_result;
    }
    ::CloseHandle(thread_handle);
  }
}

bool IESession::ExecuteCommand(const std::string& serialized_command,
                               std::string* serialized_response) {
  LOG(TRACE) << "Entering IESession::ExecuteCommand";

  // Sending a command consists of five actions:
  // 1. Setting the command to be executed
  // 2. Executing the command
  // 3. Waiting for the response to be populated
  // 4. Retrieving the response
  // 5. Retrieving whether the command sent caused the session to be ready for shutdown
  ::SendMessage(this->executor_window_handle_,
                WD_SET_COMMAND,
                NULL,
                reinterpret_cast<LPARAM>(serialized_command.c_str()));
  ::PostMessage(this->executor_window_handle_,
                WD_EXEC_COMMAND,
                NULL,
                NULL);
  
  int response_length = static_cast<int>(::SendMessage(this->executor_window_handle_,
                                                       WD_GET_RESPONSE_LENGTH,
                                                       NULL,
                                                       NULL));
  LOG(TRACE) << "Beginning wait for response length to be not zero";
  while (response_length == 0) {
    // Sleep a short time to prevent thread starvation on single-core machines.
    ::Sleep(10);
    response_length = static_cast<int>(::SendMessage(this->executor_window_handle_,
                                                     WD_GET_RESPONSE_LENGTH,
                                                     NULL,
                                                     NULL));
  }
  LOG(TRACE) << "Found non-zero response length";

  // Must add one to the length to handle the terminating character.
  std::vector<char> response_buffer(response_length + 1);
  ::SendMessage(this->executor_window_handle_,
                WD_GET_RESPONSE,
                NULL,
                reinterpret_cast<LPARAM>(&response_buffer[0]));
  *serialized_response = &response_buffer[0];
  bool session_is_valid = ::SendMessage(this->executor_window_handle_,
                                        WD_IS_SESSION_VALID,
                                        NULL,
                                        NULL) != 0;
  return session_is_valid;
}

} // namespace webdriver
