#!/bin/bash
# Usage: ./update-download.sh VERSION {CHANNEL}
# Update download.html page with the given version + date of Ceno apk.

version=$1
channel="${2:-release}"
date=$(basename ../output/release/ceno-arm64-v8a-release-${version}* | cut -d'-' -f6- | cut -d'.' -f1)

validVersion=$([[ "$version" =~ ^([0-9]|[1-9][0-9]).([0-9]|[1-9][0-9]).([0-9]|[1-9][0-9])$ ]] && echo true)
validDate=$([[ "$date" =~ ^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])_[0-9]{4}$ ]] && echo true)

if [[ "$validVersion" = true && "$validDate" = true ]]; then
    sed -Ei \
        -e "s#"https://ceno-download.s3.amazonaws.com/ceno/v.*/ceno-arm64-v8a-.*.apk"#"https://ceno-download.s3.amazonaws.com/ceno/v$version/ceno-arm64-v8a-$channel-$version-$date.apk"#g" \
        -e "s#"https://ceno-download.s3.amazonaws.com/ceno/v.*/ceno-armeabi-v7a-.*.apk"#"https://ceno-download.s3.amazonaws.com/ceno/v$version/ceno-armeabi-v7a-$channel-$version-$date.apk"#g" \
        -e "s#"https://ceno-download.s3.amazonaws.com/ceno/v.*/ceno-x86_64-.*.apk"#"https://ceno-download.s3.amazonaws.com/ceno/v$version/ceno-x86_64-$channel-$version-$date.apk"#g" \
        ./downloads/ceno-arm64-v8a-release.html \
        ./downloads/ceno-armeabi-v7a-release.html \
        ./downloads/ceno-x86_64-release.html
else
    if [[ "$validVersion" != true ]]; then
        echo "Version formatted incorrectly, ${version} should be in X.X.X format"
    fi
    if [[ "$validDate" != true ]]; then
        echo "Date formatted incorrectly, ${date} should be in YYYY-MM-DD_HHMM format"
    fi
fi
