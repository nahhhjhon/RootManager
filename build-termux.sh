#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$(dirname "$0")"
SDK="$HOME/android-sdk"
ANDROID_JAR="$SDK/platforms/android-35/android.jar"
AAPT2="/data/data/com.termux/files/usr/bin/aapt2"
D8="/data/data/com.termux/files/usr/bin/d8"
APKSIGNER="$SDK/build-tools/35.0.0/lib/apksigner.jar"
rm -rf build/classes build/dex
mkdir -p build/classes build/dex
javac -source 17 -target 17 -classpath "$ANDROID_JAR" -d build/classes src/main/java/com/local/rootmanager/MainActivity.java
d8 --lib "$ANDROID_JAR" --output build/dex $(find build/classes -name '*.class')
rm -f build/rootmanager-unsigned.apk build/rootmanager-unsigned-dex.apk build/RootManager.apk
"$AAPT2" link -o build/rootmanager-unsigned.apk -I "$ANDROID_JAR" --manifest src/main/AndroidManifest.xml
cp build/rootmanager-unsigned.apk build/rootmanager-unsigned-dex.apk
(cd build && zip -q rootmanager-unsigned-dex.apk -j dex/classes.dex)
if [ ! -f build/rootmanager.keystore ]; then
  keytool -genkeypair -v -keystore build/rootmanager.keystore -alias rootmanager -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname 'CN=Jhon Simbulas, OU=Root Manager, O=Jhon Simbulas, C=PH'
fi
java -jar "$APKSIGNER" sign --ks build/rootmanager.keystore --ks-key-alias rootmanager --ks-pass pass:android --key-pass pass:android --out build/RootManager.apk build/rootmanager-unsigned-dex.apk
java -jar "$APKSIGNER" verify --verbose build/RootManager.apk
cp build/RootManager.apk /sdcard/Download/RootManager-by-Jhon-Simbulas.apk
echo "APK: /sdcard/Download/RootManager-by-Jhon-Simbulas.apk"
