# Ace Kernel Manager

纯 Kotlin + Jetpack Compose，极简高斯模糊风格。Root 管理 / 模块管理 / 分区刷写。

## 功能

- 设备信息面板
- Root 检测（Magisk/KernelSU/APatch）
- Magisk 模块管理（读 /data/adb/modules/）
- 超级用户授权
- 分区刷写（SAF 文件选择器，选镜像 → 点分区 → 一键刷入）

## Termux 打包

```bash
# 1. 安装 aapt2
pkg install aapt2

# 2. 接受 SDK 许可证
yes | sdkmanager --licenses

# 3. 安装 SDK
sdkmanager "platforms;android-34" "build-tools;34.0.0"

# 4. 生成签名
keytool -genkey -v -keystore app/debug.keystore \
  -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=Debug,O=Android,C=US" -noprompt

# 5. 编译
chmod +x gradlew
./gradlew assembleRelease
```

APK 输出：`app/build/outputs/apk/release/app-release.apk`

## PC 打包

```bash
keytool -genkey -v -keystore app/debug.keystore \
  -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=Debug,O=Android,C=US" -noprompt

chmod +x gradlew
./gradlew assembleRelease
```

- 包名：`com.kerneluser.ace`
- v1.0.00 / minSdk 24 / targetSdk 34