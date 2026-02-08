# APK Analyzer

**Author:** Pravin V  
**Email:** [pravin@pravstechhub.com](mailto:pravin@pravstechhub.com)  
**Website:** https://www.pravstechhub.com  

A professional Android application for analyzing installed APK files with comprehensive information display, advanced filtering, and full dark theme support.

---

## 🚀 Features

### Core Functionality
- 📱 **Comprehensive App Analysis** - Display detailed information for all installed applications
- 🔍 **Real-time Search** - Instantly filter apps by name or package
- 🎯 **Advanced Filtering** - Filter by All Apps, User Apps, System Apps, or Unknown Source
- 📋 **Tap-to-Copy** - Every field is copyable with a single tap
- 🌙 **Dark Theme** - Full dark mode support with OLED-friendly colors
- 🎨 **Material Design 3** - Modern, clean UI following Material Design guidelines

### Detailed Information Display

#### General Information (13+ Fields)
- Package name
- Version name & code
- App size (formatted)
- Installation date
- Last update date
- Target SDK & Min SDK
- APK path
- Data directory
- Native library directory
- UID
- Install source

#### Security Information
- **SHA-256 Signature** (lowercase, copyable)
- System app indicator

#### Components Analysis
- Activities count and list
- Services count and list
- Broadcast Receivers count and list
- Content Providers count and list

#### Permissions
- Complete list of requested permissions
- Easy-to-read permission names
- All permissions copyable

### User Actions
- 🚀 **Open App** - Launch applications directly
- 📤 **Share Info** - Share app details via any app
- 🗑️ **Uninstall** - Quick access to app settings for uninstallation

---

## 📸 Screenshots

*Light Theme & Dark Theme side by side showing app list and detailed information*

---

## 🛠️ Technical Stack

- **Language:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Architecture:** MVVM-inspired with clean separation of concerns
- **UI:** XML layouts with Material Components
- **Async:** Kotlin Coroutines
- **Efficiency:** RecyclerView with DiffUtil

---

## 📦 Installation

1. Clone the repository
```bash
git clone https://github.com/Pravs254gh/Apk-Analyzer.git
