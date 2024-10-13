In order to get the SHA execute the command = `keytool -genkeypair -v -keystore android-dev.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=CV, OU=UN, O=OR, L=LO, S=LO, C=EC"` this SHA-1 code should be copied in the SHA certificate fingerprint in firebase. The password is `android`
Create the firebase app and download the google-services.json file. 
The CLIENT_ID that is hardcoded in res/values/strings.xml in the tag `server_client_id` should be the same that is in the `google-services.json` in the field: `oauthclient.client_id`
The package name to setup both in Android and in firebase is: ``com.ys.phdmama``
