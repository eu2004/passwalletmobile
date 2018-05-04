http://www.androhub.com/authenticate-using-google-sign-in-on-android/
https://developers.google.com/identity/sign-in/android/start-integrating


keytool -exportcert -keystore c:\Users\emilu\.android\debug.keystore -list -v
content://com.google.android.drive/open/FILE_ID
https://drive.google.com/open?id=0B_ga1eslVOK3dVIwQjJ3SHg4ZEE
content://com.google.android.drive/open/0B_ga1eslVOK3dVIwQjJ3SHg4ZEE
https://console.cloud.google.com/apis/credentials?project=passwallet-201108



C:\Users\emilu\.android\passwallet.keystore
P4ssw4llet_Secret_Key.
PasswalletKey
P4sswallet_Device_Key.

https://developers.google.com/identity/sign-in/android/start-integrating

keytool -exportcert -alias PasswalletKey -keystore C:\Users\emilu\.android\passwallet.keystore -list -v

"%JAVA_HOME%\bin\keytool" -exportcert -keystore C:\Users\emilu\.android\android.keystore -list
21:E6:56:09:27:9F:54:96:B7:00:50:7D:31:52:4F:E3:6E:0F:EF:51

> "%JAVA_HOME%\bin\keytool" -exportcert -alias PasswalletKey -keystore C:\Users\emilu\.android\passwallet.keystore -list -v                                                             
Enter keystore password:                                                                                                                                                                
Alias name: PasswalletKey                                                                                                                                                               
Creation date: Apr 25, 2018                                                                                                                                                             
Entry type: PrivateKeyEntry                                                                                                                                                             
Certificate chain length: 1                                                                                                                                                             
Certificate[1]:                                                                                                                                                                         
Owner: CN=Emilu, OU=Home, O=Home, L=Craiova, ST=Dolj, C=RO                                                                                                                              
Issuer: CN=Emilu, OU=Home, O=Home, L=Craiova, ST=Dolj, C=RO                                                                                                                             
Serial number: 21b328e7                                                                                                                                                                 
Valid from: Wed Apr 25 14:05:01 EEST 2018 until: Sun Apr 19 14:05:01 EEST 2043                                                                                                          
Certificate fingerprints:                                                                                                                                                               
         MD5:  83:80:C1:63:73:4E:DC:79:DF:C5:CF:CD:06:61:BC:C8                                                                                                                          
         SHA1: 53:7D:EB:A1:B6:93:34:81:21:F4:D7:A2:5A:70:BA:C3:55:78:13:22
			   53:7D:EB:A1:B6:93:34:81:21:F4:D7:A2:5A:70:BA:C3:55:78:13:22
		 115950897832-4bto6rc242dnvr2krnfqga1bi49cgi1s.apps.googleusercontent.com
		 
         SHA256: 41:B9:28:44:8D:37:62:43:12:C5:B5:48:4E:D3:5E:D2:32:C7:FB:B0:EF:F8:BD:31:EC:B9:24:58:C2:45:DA:5F                                                                        
         Signature algorithm name: SHA256withRSA                                                                                                                                        
         Version: 3                                                                                                                                                                     
                                                                                                                                                                                        
Extensions:                                                                                                                                                                             
                                                                                                                                                                                        
#1: ObjectId: 2.5.29.14 Criticality=false                                                                                                                                               
SubjectKeyIdentifier [                                                                                                                                                                  
KeyIdentifier [                                                                                                                                                                         
0000: CC 32 11 76 D5 8C A7 2A   8F 18 0D E8 76 54 96 AE  .2.v...*....vT..                                                                                                               
0010: B0 D3 43 A1                                        ..C.                                                                                                                           
]                                                                                                                                                                                       
]                                                                                                                                                                                       


115950897832-tqpj7agmjp9dd9linnhvepdocjle81hm.apps.googleusercontent.com