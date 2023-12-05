# This file has been generated using `bazel run scripts:pinned_browsers`

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("//common/private:dmg_archive.bzl", "dmg_archive")
load("//common/private:drivers.bzl", "local_drivers")
load("//common/private:pkg_archive.bzl", "pkg_archive")

def pin_browsers():
    local_drivers()

    http_archive(
        name = "linux_firefox",
        url = "https://ftp.mozilla.org/pub/firefox/releases/120.0.1/linux-x86_64/en-US/firefox-120.0.1.tar.bz2",
        sha256 = "8c9dccdc35dbd3739f6a1e0459a1a415c6ee7ffe4d2272cba1fd3987f9c2133a",
        build_file_content = """
filegroup(
    name = "files",
    srcs = glob(["**/*"]),
    visibility = ["//visibility:public"],
)

exports_files(
    ["firefox/firefox"],
)
""",
    )

    dmg_archive(
        name = "mac_firefox",
        url = "https://ftp.mozilla.org/pub/firefox/releases/120.0.1/mac/en-US/Firefox%20120.0.1.dmg",
        sha256 = "b30cbaafe57c49fd334db05824b1641b75227da2415e6ae1751f72f9a1335d2d",
        build_file_content = "exports_files([\"Firefox.app\"])",
    )

    http_archive(
        name = "linux_beta_firefox",
        url = "https://ftp.mozilla.org/pub/firefox/releases/121.0b7/linux-x86_64/en-US/firefox-121.0b7.tar.bz2",
        sha256 = "f706ae774e37786d8782920655d89f0580b8fbd113a2888ac14d49cc810e3714",
        build_file_content = """
filegroup(
    name = "files",
    srcs = glob(["**/*"]),
    visibility = ["//visibility:public"],
)

exports_files(
    ["firefox/firefox"],
)
""",
    )

    dmg_archive(
        name = "mac_beta_firefox",
        url = "https://ftp.mozilla.org/pub/firefox/releases/121.0b7/mac/en-US/Firefox%20121.0b7.dmg",
        sha256 = "560032deec826dc0b0b6061e0108aaeb1324e39652bd8f8ceb639da5087c7b83",
        build_file_content = "exports_files([\"Firefox.app\"])",
    )

    http_archive(
        name = "linux_dev_firefox",
        url = "https://ftp.mozilla.org/pub/firefox/releases/121.0b7/linux-x86_64/en-US/firefox-121.0b7.tar.bz2",
        sha256 = "f706ae774e37786d8782920655d89f0580b8fbd113a2888ac14d49cc810e3714",
        build_file_content = """
filegroup(
    name = "files",
    srcs = glob(["**/*"]),
    visibility = ["//visibility:public"],
)

exports_files(
    ["firefox/firefox"],
)
""",
    )

    dmg_archive(
        name = "mac_dev_firefox",
        url = "https://ftp.mozilla.org/pub/firefox/releases/121.0b7/mac/en-US/Firefox%20121.0b7.dmg",
        sha256 = "560032deec826dc0b0b6061e0108aaeb1324e39652bd8f8ceb639da5087c7b83",
        build_file_content = "exports_files([\"Firefox.app\"])",
    )

    http_archive(
        name = "linux_geckodriver",
        url = "https://github.com/mozilla/geckodriver/releases/download/v0.33.0/geckodriver-v0.33.0-linux64.tar.gz",
        sha256 = "5f5e89bb31fe5f55f963f56ef7e55a5c8e9dc415d94b1ddc539171a327b8e6c4",
        build_file_content = "exports_files([\"geckodriver\"])",
    )

    http_archive(
        name = "mac_geckodriver",
        url = "https://github.com/mozilla/geckodriver/releases/download/v0.33.0/geckodriver-v0.33.0-macos.tar.gz",
        sha256 = "a39c72553beae18c58a560c84cfe86c1708d101bb3d57b8744e3eca64f403703",
        build_file_content = "exports_files([\"geckodriver\"])",
    )

    pkg_archive(
        name = "mac_edge",
        url = "https://msedge.sf.dl.delivery.mp.microsoft.com/filestreamingservice/files/fec5727f-a1e0-458c-a53d-cc69caef1230/MicrosoftEdge-119.0.2151.97.pkg",
        sha256 = "786f8c5d3c53e54ddcf14d780164f1bc7f21483a5e01988a1b930b2aa790e7b0",
        move = {
            "MicrosoftEdge-119.0.2151.97.pkg/Payload/Microsoft Edge.app": "Edge.app",
        },
        build_file_content = "exports_files([\"Edge.app\"])",
    )

    http_archive(
        name = "linux_edgedriver",
        url = "https://msedgedriver.azureedge.net/119.0.2151.97/edgedriver_linux64.zip",
        sha256 = "0039b5ce30479fd8789cb395fd1e578e4a7ebb76d80c80fae3aa624d144a5fdd",
        build_file_content = "exports_files([\"msedgedriver\"])",
    )

    http_archive(
        name = "mac_edgedriver",
        url = "https://msedgedriver.azureedge.net/119.0.2151.97/edgedriver_mac64.zip",
        sha256 = "b5ec14956a341db07373783ad84f7701441dfc9ceec7a0895778ad76b70c2ddb",
        build_file_content = "exports_files([\"msedgedriver\"])",
    )

    http_archive(
        name = "linux_chrome",
        url = "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/119.0.6045.105/linux64/chrome-linux64.zip",
        sha256 = "aa2cb76a385bf0694987e9b697a315973afae946191b0d9cf31ce05ca1d44d7f",
        build_file_content = """
filegroup(
    name = "files",
    srcs = glob(["**/*"]),
    visibility = ["//visibility:public"],
)

exports_files(
    ["chrome-linux64/chrome"],
)
""",
    )

    http_archive(
        name = "mac_chrome",
        url = "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/119.0.6045.105/mac-x64/chrome-mac-x64.zip",
        sha256 = "d5ea4d9bcb6ad2465717f690e6f44ef9de27cf22ebfd7d7d987cabfd4d476cd6",
        strip_prefix = "chrome-mac-x64",
        patch_cmds = [
            "mv 'Google Chrome for Testing.app' Chrome.app",
            "mv 'Chrome.app/Contents/MacOS/Google Chrome for Testing' Chrome.app/Contents/MacOS/Chrome",
        ],
        build_file_content = "exports_files([\"Chrome.app\"])",
    )

    http_archive(
        name = "linux_chromedriver",
        url = "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/119.0.6045.105/linux64/chromedriver-linux64.zip",
        sha256 = "da8e8b028da912c0a2d5ec4fbf59c4324d93925861e3d53259a628c90ec37ff6",
        strip_prefix = "chromedriver-linux64",
        build_file_content = "exports_files([\"chromedriver\"])",
    )

    http_archive(
        name = "mac_chromedriver",
        url = "https://edgedl.me.gvt1.com/edgedl/chrome/chrome-for-testing/119.0.6045.105/mac-x64/chromedriver-mac-x64.zip",
        sha256 = "b11014f51240373f92a2fea86a865ab0ca1aa8cfa62194836da83ddb05ec5422",
        strip_prefix = "chromedriver-mac-x64",
        build_file_content = "exports_files([\"chromedriver\"])",
    )
