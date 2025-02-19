# Android Inventory App
Created by: Bradley Wells   
Computer Science Capstone Course (CS-499)  
Southern New Hampshire University  
Advised by: Dr. Maciosek  

## Overview
This guide provides the steps needed to open, build, and run the **Android Inventory App** located 
in the `Inventory` folder. The app is packaged in a `.zip` file titled `Android Inventory App.zip`. 
You will use **Android Studio (Ladybug)** to open and run the application.

---

## Prerequisites
Before running the application, ensure the following:

1. **Android Studio**
    - Install the latest version of Android Studio.
    - Ensure all necessary components like SDKs, build tools, and the emulator are installed.

2. **Java Development Kit (JDK)**
    - Ensure JDK 8 or higher is installed and configured.

3. **Android Device or Emulator**
    - A physical Android device with developer mode enabled or an Android Virtual Device (AVD) set up in Android Studio.

---

## Steps to Open and Run the Application

### Step 1: Extract the `.zip` File
1. Locate the file: `Android Inventory App.zip`.
2. Extract the contents to a folder on your local machine.
3. Navigate to the extracted folder and locate the project path:
   `Android Inventory App/Inventory`.

### Step 2: Open the Project in Android Studio
1. Launch **Android Studio (Ladybug)**.
2. Select **File > Open** from the menu.
3. Navigate to the `Inventory` folder and click OK.
4. Wait for Android Studio to load the project and sync the Gradle files.

### Step 3: Sync Gradle Files
1. If prompted, click **Sync Now** to sync the Gradle files.
2. Ensure there are no errors during the synchronization process. Resolve any missing dependencies or SDK versions if necessary.

### Step 4: Configure the Emulator or Device
1. **Emulator**:
    - Open the AVD Manager in Android Studio.
    - Create or select an Android Virtual Device (recommend using 'Medium Phone API 35').
    - Start the emulator.

2. **Physical Device**:
    - Connect your Android device via USB.
    - Enable **USB Debugging** in the developer options on your device.

### Step 5: Build and Run the Application
1. Click on **Build > Clean Project**, then **Build > Rebuild Project** to ensure all files are correctly set up.
2. Select the **Run/Debug Configuration** dropdown and ensure it points to the correct launcher activity.
3. Click the **Run** button (green triangle) or press **Shift + F10** to build and launch the app.
4. Select your target device (emulator or physical device) and wait for the app to install and run.

### Step 6: Test the App
1. Register a user by entering a username and password.
2. Login with the registered credentials.
3. Test the CRUD (Create, Read, Update, Delete) operations by adding, updating, and deleting items.
4. Test the sorting and filtering functionality.
5. Test the search functionality.
6. Test the item detail card.
7. Test SMS functionality by enabling SMS notifications (enter phone number for emulator or physical device).
8. Test Logout functionality.

---

## Common Issues and Fixes

1. **Gradle Sync Errors**:
    - Ensure your internet connection is active to download missing dependencies.
    - Verify the correct Android SDK version is installed via **File > Project Structure > SDK Location**.

2. **Emulator Not Starting**:
    - Ensure the emulator has enough allocated RAM.
    - Restart the emulator or create a new one in the AVD Manager.

3. **Build Errors**:
    - Use **Build > Clean Project** and **Build > Rebuild Project**.
    - Check the `build.gradle` file for dependency issues and update versions if needed.

4. **App Not Running on Device**:
    - Ensure your device is recognized in Android Studio under **Device Manager**.
    - Re-enable **USB Debugging** and confirm permissions.

---

## Notes
- Ensure you do not modify any core files in the project directory unless required.
- If the project files have been downloaded from GitHub, contact the developer for the google-services.json file to connect with the Firestore Database.
- For any issues not covered here, refer to the Android Studio documentation or contact the developer.

---

## Contact
For additional assistance, contact the developer at:
- **Email**: bradley.wells@snhu.edu
- **GitHub Repository**: https://github.com/bcwells24/bcwells24.github.io

