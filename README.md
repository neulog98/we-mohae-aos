# 우리모해 Android

Android 애플리케이션 ID는 `com.woorimwohae.app.aos`이며, Kotlin 소스 namespace는 `com.woorimwohae.app`입니다.

## 릴리스 APK 서명 키 설정

GitHub SSH 키와 Android APK 서명 키는 서로 다른 키입니다. APK 서명 키는 앱 업데이트 때마다 동일한 키를 사용해야 하므로 안전한 별도 저장소에 백업해야 합니다.

1. 프로젝트 루트에서 keystore를 생성합니다.

```bash
mkdir -p keystore
keytool -genkeypair -v \
  -keystore keystore/woorimohae-release.jks \
  -alias woorimohae \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

2. 서명 설정 파일을 생성합니다.

```bash
cp keystore.properties.example keystore.properties
```

3. `keystore.properties`의 `storePassword`, `keyPassword`를 실제 값으로 변경합니다.

```properties
storeFile=keystore/woorimohae-release.jks
storePassword=실제_저장소_비밀번호
keyAlias=woorimohae
keyPassword=실제_키_비밀번호
```

4. 릴리스 APK를 빌드하고 서명을 검증합니다.

```bash
./gradlew assembleRelease
$ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

`keystore.properties`, `.jks`, `.keystore` 파일은 Git에서 제외됩니다. 해당 파일과 비밀번호를 GitHub에 커밋하지 마세요.
