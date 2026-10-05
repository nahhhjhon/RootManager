# 🛡️ Root Manager by Jhon Simbulas

A simple Android root manager for Magisk-rooted devices.

Root Manager allows users to manually manage which applications are granted or denied **Superuser (root) access** through Magisk root policies.

## ✨ Features

- 📱 View installed applications
- 🔍 Search applications
- 🖼️ Display application icons
- 🆔 Display application UID
- 🛡️ Manually grant Superuser access
- 🚫 Manually deny Superuser access
- 🔑 View applications with root access
- 👤 Filter User Apps
- ⚙️ Filter System Apps
- 🔄 Refresh application list
- 🌙 Dark mobile-friendly interface
- 👑 Designed for Magisk-rooted Android devices

## 🔐 Manual Superuser Management

Root Manager manages root access using the application's Android UID and Magisk's root policy database.

### Allow Root

When root access is enabled for an application, Root Manager sets its Magisk policy to allow Superuser access.

### Deny Root

When root access is disabled, the application's Magisk policy is changed to deny Superuser access.

> ⚠️ Root access gives an application powerful permissions over your Android device. Only grant root access to applications you trust.

## 📋 Requirements

- Android device
- Root access
- Magisk
- ARM64 or comp
atible Android device
- Android 8.0 or newer recommended

## 🔨 Building with Termux

This project uses a manual Android build process.

Required tools include:

- Java/Javac
- Android SDK `android.jar`
- AAPT2
- D8
- apksigner

The included build script is:

```text
build-termux.sh
