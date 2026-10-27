#!/bin/bash
VERSION_NAME=$1
BASE_URL="http://ceno-download.s3.amazonaws.com/ceno"
APK_NAME_DATE=$(basename ../output/release/ceno-arm64-v8a-release-${VERSION_NAME}* | cut -c 24-45)
AAB_NAME_DATE=$(basename ../output/release/ceno-${VERSION_NAME}* | cut -c 6-27)
RELEASE_DATA="{ \
                \"name\": \"Ceno for Android v${VERSION_NAME}\", \
                \"tag_name\": \"v${VERSION_NAME}\", \
                \"assets\":{ \
                    \"links\": [ \
                        { \"name\": \"ceno-arm64-v8a-release-${APK_NAME_DATE}.apk (RECOMMENDED)\", \
                            \"url\": \"${BASE_URL}/v${VERSION_NAME}/ceno-arm64-v8a-release-${APK_NAME_DATE}.apk\", \
                            \"link_type\":\"package\" \
                        }, \
                        { \"name\": \"ceno-armeabi-v7a-release-${APK_NAME_DATE}.apk\", \
                            \"url\": \"${BASE_URL}/v${VERSION_NAME}/ceno-armeabi-v7a-release-${APK_NAME_DATE}.apk\", \
                            \"link_type\":\"package\" \
                        }, \
                        { \"name\": \"ceno-x86_64-release-${APK_NAME_DATE}.apk\", \
                            \"url\": \"${BASE_URL}/v${VERSION_NAME}/ceno-x86_64-release-${APK_NAME_DATE}.apk\", \
                            \"link_type\":\"package\" \
                        }, \
                        { \"name\": \"ceno-${AAB_NAME_DATE}-release.aab\", \
                            \"url\": \"${BASE_URL}/v${VERSION_NAME}/ceno-${AAB_NAME_DATE}-release.aab\", \
                            \"link_type\":\"package\" \
                        }, \
                        { \"name\": \"sha256sums.txt\", \
                            \"url\": \"${BASE_URL}/v${VERSION_NAME}/sha256sums.txt\", \
                            \"link_type\":\"other\" \
                        }, \
                        { \"name\": \"sha256sums.txt.asc\", \
                            \"url\": \"${BASE_URL}/v${VERSION_NAME}/sha256sums.txt.asc\", \
                            \"link_type\":\"other\" \
                        } \
                    ] \
                } \
              }"

curl --request POST \
  -H "PRIVATE-TOKEN: ${GL_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "${RELEASE_DATA}" \
  "https://gitlab.com/api/v4/projects/38790003/releases"

ID=$(curl -L \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "X-GitHub-Api-Version: 2026-03-10" \
  -d "{ \"tag_name\":\"v${VERSION_NAME}\",\"name\":\"Ceno for Android v${VERSION_NAME}\"}" \
  "https://api.github.com/repos/ceno-app/ceno-android/releases" \
  | jq ".id")

for filename in ../output/release/*; do
  echo "put ${filename}"
  curl -L \
    -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer ${GH_TOKEN}" \
    -H "X-GitHub-Api-Version: 2026-03-10" \
    -H "Content-Type: application/octet-stream" \
    --data-binary "@${filename}" \
    "https://uploads.github.com/repos/ceno-app/ceno-android/releases/${ID}/assets?name=$(basename $filename)"
done
