# Used to create a development image for working on Selenium

# You can find the new timestamped tags here: https://hub.docker.com/r/gitpod/workspace-full/tags
FROM gitpod/workspace-full:2022-06-20-19-54-55

USER root

#RUN apt-get update -qqy && apt-get install -y wget curl gnupg2

# So we can install browsers and browser drivers later
RUN wget https://packages.microsoft.com/config/ubuntu/21.04/packages-microsoft-prod.deb -O packages-microsoft-prod.deb && dpkg -i packages-microsoft-prod.deb && rm packages-microsoft-prod.deb
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - && echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list
RUN mkdir -p /home/gitpod/selenium

ENV DEBIAN_FRONTEND=noninteractive

# Things needed by bazel

#RUN apt-get update -qqy && \
#    apt-get -qy install build-essential \
#                        git-all \
#                        ca-certificates \
#                        openjdk-11-jdk \
#                        python3.9 python3-pip python-is-python3 \
#                        ruby-full \
#                        dotnet-sdk-5.0 && \
#    rm -rf /var/lib/apt/lists/* /var/cache/apt/*

RUN apt-get update -qqy && \
    apt-get -qy install python-is-python3 \
                        dotnet-sdk-5.0 && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/*

# Browsers

RUN apt-get update -qqy && \
    apt-get -qy install google-chrome-stable firefox && \
    rm -rf /var/lib/apt/lists/* /var/cache/apt/*

# Browser Drivers

RUN CHROME_MAJOR_VERSION=$(google-chrome --version | sed -E "s/.* ([0-9]+)(\.[0-9]+){3}.*/\1/") \
  && CHROME_DRIVER_VERSION=$(wget --no-verbose -O - "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_MAJOR_VERSION}") \
  && echo "Using ChromeDriver version: "$CHROME_DRIVER_VERSION \
  && wget --no-verbose -O /tmp/chromedriver_linux64.zip https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
  && rm -rf /home/gitpod/selenium/chromedriver \
  && unzip /tmp/chromedriver_linux64.zip -d /home/gitpod/selenium \
  && rm /tmp/chromedriver_linux64.zip \
  && mv /home/gitpod/selenium/chromedriver /home/gitpod/selenium/chromedriver-$CHROME_DRIVER_VERSION \
  && chmod 755 /home/gitpod/selenium/chromedriver-$CHROME_DRIVER_VERSION \
  && sudo ln -fs /home/gitpod/selenium/chromedriver-$CHROME_DRIVER_VERSION /usr/bin/chromedriver

# Bazel

RUN curl -L https://github.com/bazelbuild/bazelisk/releases/download/v1.12.0/bazelisk-linux-amd64 -o /usr/bin/bazelisk && \
    chmod 755 /usr/bin/bazelisk && \
    ln -sf /usr/bin/bazelisk /usr/bin/bazel

USER gitpod

#RUN git clone --depth 1 https://github.com/SeleniumHQ/selenium.git /home/dev/selenium
#RUN echo "build --//common:pin_browsers" >>/home/dev/selenium/.bazelrc.local
#RUN echo "build --//common:headless" >>/home/dev/selenium/.bazelrc.local
