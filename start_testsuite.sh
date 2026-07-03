#!/bin/bash
SCRIPT_DIR=$(dirname "$0")
UPLOAD_APKS=0
VERSION=""
while getopts un: option; do
    case "$option" in
        u) UPLOAD_APKS=1;;
        n) VERSION="${OPTARG}" ;;
        *) UPLOAD_APKS=0;;
    esac
done

if [ $UPLOAD_APKS -eq 1 ]; then
  curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
  -X POST "https://api-cloud.browserstack.com/app-automate/upload" \
  -F "file=@${SCRIPT_DIR}/app/build/outputs/apk/debug/ceno-${VERSION}-arm64-v8a-debug.apk" \
  -F "custom_id=ouinet-debug-latest"

  curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
  -X POST "https://api-cloud.browserstack.com/app-automate/espresso/test-suite" \
  -F "file=@${SCRIPT_DIR}/app/build/outputs/apk/androidTest/debug/ceno-${VERSION}-debug-androidTest.apk" \
  -F "custom_id=ouinet-debug-androidTest-latest"
fi

DEVICES='["Samsung Galaxy Note 9-8.1", "Huawei P30-9.0", "Xiaomi Redmi Note 9-10.0", "Xiaomi Redmi Note 11-11.0", "Google Pixel 6-12.0", "Samsung Galaxy S23-13.0", "Samsung Galaxy S24-14.0", "Google Pixel 9-15.0", "Samsung Galaxy S26-16.0", "Google Pixel 9-17.0"]'
CONFIG='{"clearPackageData": "true", "deviceLogs": "true", "devices": '"${DEVICES}"', "app": "ouinet-debug-latest", "testSuite": "ouinet-debug-androidTest-latest", "locale": "en_US" }'
# To start only specific tests, add to `class` option to config, e.g. "class": ["ie.equalit.ceno.ui.ScreenshotGenerator"]

curl -u "${BROWSERSTACK_USERNAME}:${BROWSERSTACK_ACCESS_KEY}" \
-X POST "https://api-cloud.browserstack.com/app-automate/espresso/v2/build" \
-d "${CONFIG}" \
-H "Content-Type: application/json"
