# Schizophrenic Squid

## Requirements

### Backend Testing and Deployment

- python3
- virtualenv
- pip
- gcloud sdk

### Frontend Build and Development

- android-sdk
- gradle

Set JAVA_HOME environment variable to location of Java before building. 

Add your SHA-1 signature to your firebase project on the Google Cloud Platform. The project should have a real-time database
with the relevant key/value pairs in addition to the storage provided by Google.

You can get the signature by running:
 
```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore -list -v -storepass android
```
For detailed instructions, refer to https://codelabs.developers.google.com/codelabs/firebase-android/index.html

## Frontend

### Build on Windows

```bash
cd frontend/Fireapp
./gradlew.bat assembleRelease
```

### Build on Linux

```
cd frontend/Fireapp
./gradlew assembleRelease
```

## Backend

### Local Testing

```bash
virtualenv -p python3 env
source env/bin/activate
pip install -r requirements.txt
gcloud auth application-default login
python main.py
```

### Deployment

```bash
cd backend
gcloud app deploy
cd firebase
firebase deploy
```

For detailed instructions, refer to https://codelabs.developers.google.com/codelabs/cloud-vision-app-engine/index.html

## Authors

Miko Pohjalainen
Lauri Tervonen
Joonas Harjumaki
Muhammad Abdullah Khan

