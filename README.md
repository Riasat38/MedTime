# MedTime 

<div align="center">


**An app to remind you to take your medicine**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google%20Gemini-blue.svg)](https://ai.google.dev/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)

</div>

## 📖 Overview

**MedTime** is an intelligent Android medication management application that leverages AI-powered prescription analysis to help users never miss a dose. Built with modern Android technologies, MedTime uses Google's Gemini AI to automatically extract medication information from prescription images, making medication tracking effortless and accurate.

### 🎯 Key Highlights

- **AI-Powered Prescription Analysis**: Extract medication details instantly from photos
- **Multilingual Support**: Handles both English and Bengali prescriptions
- **Smart Reminders**: Push notifications and alarm-based medication reminders
- **Secure Authentication**: Firebase-based user authentication and data storage
- **Modern UI/UX**: Beautiful Material Design 3 interface with Jetpack Compose

---
## App Demo : https://youtu.be/Dpsy2OYxBJg
## ✨ Working Features

### 🔐 Authentication System
- **User Registration**: Secure sign-up with name, email, password, gender, and age
- **User Login**: Email/password authentication with Firebase
- **Session Management**: Persistent login with secure session handling
- **User Profile**: View and manage personal information

### 📸 AI Prescription Analysis
- **Camera Integration**: Capture prescription photos directly from the app
- **Gallery Upload**: Import existing prescription images
- **Gemini 2.5 Flash Integration**: Advanced AI model for accurate text extraction
- **Multilingual OCR**: Supports Bengali (বাংলা) and English prescriptions
- **Smart Parsing**: Automatically extracts:
  - Medication names
  - Dosage information
  - Frequency (daily, weekly, monthly)
  - Duration (days, weeks, months)
  - Timing (morning, afternoon, evening, night)
  - Special instructions (before/after food, with water, etc.)

### 📋 Medication Management
- **Editable Medications**: Review and modify AI-extracted information
- **Custom Titles**: Name your prescriptions for easy identification
- **Medication Cards**: Visual representation of each medication with details
- **Duration Tracking**: Automatic calculation of end dates
- **Active/Inactive Status**: Track ongoing and completed medications

### 🔔 Smart Notification System
- **Push Notifications**: Firebase Cloud Messaging for reliable reminders
- **Alarm Reminders**: System alarms for critical medication times
- **Notification Preview**: See how your reminders will look before saving
- **Multiple Time Slots**: Support for medications taken multiple times per day
- **Boot Persistence**: Reminders survive device restarts

### 📚 Prescription Records
- **View All Prescriptions**: Comprehensive list of saved prescriptions
- **Expandable Cards**: Tap to view detailed medication information
- **Delete Function**: Remove old or incorrect prescriptions
- **Refresh**: Pull latest data from cloud storage
- **Real-time Sync**: Automatic synchronization with Firebase Firestore

### 🎨 User Interface
- **Material Design 3**: Modern, intuitive interface
- **Dark/Light Theme**: Adaptive theming support
- **Bottom Navigation**: Easy navigation between Home, Prescription, and Records
- **Gradient Buttons**: Custom styled interactive elements
- **Responsive Layout**: Optimized for various screen sizes
- **Loading States**: Clear feedback during operations

---

## 🛠️ Technology Stack

### Frontend
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material Design 3** - Google's latest design system
- **Compose Navigation** - Type-safe navigation
- **Coil** - Image loading library
- **CameraX** - Modern camera API

### Backend & Services
- **Firebase Authentication** - User management
- **Firebase Firestore** - Cloud database
- **Firebase Cloud Messaging** - Push notifications
- **Google Gemini AI** - Prescription analysis (gemini-2.5-flash)

### Architecture & Libraries
- **MVVM Architecture** - Model-View-ViewModel pattern
- **Kotlin Coroutines** - Asynchronous programming
- **ViewModel** - Lifecycle-aware state management
- **WorkManager** - Background task scheduling
- **Retrofit** - HTTP client for API calls
- **Gson** - JSON serialization

### Development Tools
- **Kotlin** - Primary programming language
- **Gradle (KTS)** - Build system
- **Android SDK 33+** - Minimum Android 13
- **Target SDK 36** - Latest Android features

---

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio** - Hedgehog (2023.1.1) or later
- **JDK** - Java Development Kit 11 or higher
- **Android SDK** - API Level 33 or higher
- **Git** - Version control system

### Required Accounts
- **Google Account** - For Firebase and Gemini API access
- **Firebase Project** - Set up at [Firebase Console](https://console.firebase.google.com/)
- **Gemini API Key** - Obtain from [Google AI Studio](https://makersuite.google.com/app/apikey)

---

## 🚀 Installation & Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Riasat38/MedTime.git
cd MedTime
```

### 2. Firebase Configuration

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app to your Firebase project
   - Package name: `com.example.medtime`
4. Download `google-services.json`
5. Place it in the `app/` directory

**Enable Firebase Services:**
- **Authentication**: Enable Email/Password sign-in
- **Firestore Database**: Create database in production mode
- **Cloud Messaging**: Automatically enabled

**Firestore Security Rules:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /prescriptions/{prescriptionId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 3. Gemini API Configuration

1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create or sign in with your Google account
3. Generate a new API key
4. Create a `local.properties` file in the project root (if it doesn't exist)
5. Add your API key:

```properties
sdk.dir=/path/to/Android/sdk
GEMINI_API_KEY=your_api_key_here
```

**⚠️ Security Note**: Never commit `local.properties` to version control. It's already in `.gitignore`.

### 4. Build and Run

#### Using Android Studio
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Connect an Android device or start an emulator
4. Click **Run** ▶️ or press `Shift + F10`

#### Using Command Line
```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

---

## 📱 App Architecture

MedTime follows the **MVVM (Model-View-ViewModel)** architecture pattern with a clean separation of concerns:

```
app/src/main/java/com/example/medtime/
├── ai/                     # AI/ML Integration
│   └── GeminiModel.kt     # Gemini API service
├── auth/                   # Authentication
│   └── AuthRepository.kt  # Firebase Auth logic
├── components/             # Reusable UI components
│   ├── GradientButton.kt
│   ├── MedicationCard.kt
│   ├── PrescriptionCard.kt
│   └── NotificationPreviewCard.kt
├── data/                   # Data models
│   ├── Medication.kt
│   ├── Prescription.kt
│   ├── User.kt
│   └── SessionManager.kt
├── notification/           # Notification system
│   ├── NotificationHelper.kt
│   ├── MedicationScheduler.kt
│   ├── MedicationReminderReceiver.kt
│   └── BootReceiver.kt
├── repository/             # Data layer
├── screens/                # UI screens
│   ├── HomeScreen.kt
│   ├── LoginScreen.kt
│   ├── SignUpScreen.kt
│   ├── PrescriptionScreen.kt
│   └── Records.kt
├── ui/theme/               # Material Design theme
├── utils/                  # Utility classes
├── viewmodel/              # ViewModels
│   ├── LoginViewModel.kt
│   ├── PrescriptionViewModel.kt
│   └── PrescriptionListViewModel.kt
└── MainActivity.kt         # Entry point
```

### Data Flow
```
User Action → ViewModel → Repository → Firebase/Gemini API
                ↓
              State
                ↓
         UI Updates (Compose)
```

---


---

## 💡 Usage Guide

### First Time Setup
1. **Launch the app** on your Android device
2. **Sign up** with your email and password
3. **Fill in** your personal details (name, age, gender)
4. **Grant permissions** for camera and notifications

### Analyzing a Prescription
1. Navigate to the **Prescription** tab
2. Choose **Camera** or **Gallery**
3. Capture or select your prescription image
4. Wait for AI analysis (usually 2-5 seconds)
5. **Review** extracted medications
6. **Edit** any incorrect information
7. Add a **prescription title**
8. Select **reminder method** (Push or Alarm)
9. Preview notifications
10. Tap **Save**

### Managing Medications
1. Go to the **Records** tab
2. View all your saved prescriptions
3. Tap a card to expand details
4. Use the delete icon to remove prescriptions
5. Pull down to refresh

### Receiving Reminders
- **Push Notifications**: Appear in notification tray
- **Alarm Reminders**: Full-screen alerts with sound
- Tap notification to open the app
- Mark as taken (future feature)

---

## 🔮 Future Upgrades

### Planned Features for version 2.0
- Prompt engineering for better context extrction
- More available models
- Reduce app bundle size
- Optimization
- Modularize the model interaction

#### Possible Features
-  **Offline Mode**
  - Local database with Room
  - Sync when online
  - Offline prescription viewing
    
-  **Medication Intake Tracking**
  - Mark medications as taken/skipped
  - Adherence statistics and charts
  - Streak tracking for motivation

-  **Advanced Reminder Customization**
  - Snooze functionality
  - Custom notification sounds
  - Reminder intensity levels


-  **Doctor Integration**
  - Share prescriptions with doctors
  - Telemedicine integration
  - Prescription renewal requests

-  **Health Tracking**
  - Blood pressure logging
  - Sugar level tracking
  - Integration with health apps

-  **Family Account**
  - Manage medications for family members
  - Caregiver mode
  - Multiple user profiles


#### Nice to Have

-  **Pharmacy Locator**
  - Find nearby pharmacies
  - Price comparison
  - Online ordering integration


-  **Wearable Support**
  - Smartwatch notifications
  - Quick mark as taken
  - Health data integration



## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 Riasat38

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🙏 Acknowledgments

- **Google Gemini AI** - For powerful prescription analysis capabilities
- **Firebase** - For robust backend infrastructure
- **Jetpack Compose** - For modern Android UI development
- **Material Design** - For beautiful design guidelines
- **Android Community** - For continuous support and resources

---

## 📞 Contact & Support

- **Developer**: [@Riasat38](https://github.com/Riasat38)

---

## 📊 Project Status

**Current Version**: 1.0.0  
**Status**: Active Development  
**Last Updated**: December 2026


