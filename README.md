# mlkit
mlkit text recognition example
features: 
- read text from captured image and image from gallery
- flash control
- store text to cloud database (firebase)
- retrieve text from cloud database

positive case
![2023-01-24_1674569674107](https://user-images.githubusercontent.com/44308954/214323483-2f5b5eac-872f-478f-9407-bdd23bb02761.gif)

negative case
![2023-01-24_1674568975607](https://user-images.githubusercontent.com/44308954/214323599-401e438d-a223-4db4-87f4-7304227f197c.gif)

summary

the app require permission to access camera. run time permission will appear on first install or while permission revoked from device.
![Screenshot_20230124-211701332](https://user-images.githubusercontent.com/44308954/214320046-5272b90b-5ef9-4f57-8f56-385e519cf6fb.jpg)

if permission declined, there would be appear new popup that will send you to app info on your device, to grant permission manually. 
![Screenshot_20230124-211709152](https://user-images.githubusercontent.com/44308954/214321316-5478e3c9-e4bb-4343-8785-1f91ed80ea57.jpg)

![Screenshot_20230124-211722736](https://user-images.githubusercontent.com/44308954/214322170-a053e417-1cfc-4c5d-b782-d9302929540c.jpg)
![Screenshot_20230124-211729081](https://user-images.githubusercontent.com/44308954/214322244-6181a817-dc53-46e3-bca1-d83bfbe06d05.jpg)

after permission granted, user can use their camera to capture picture of text that will be converted to text. 
reading text will not always success. due to usage of mlkit library require google play service and some device might unable to convert 
image to text due to unfinished download progress on google play service or if google play didn't installed on device.
![Screenshot_20230124-211742192](https://user-images.githubusercontent.com/44308954/214322630-73a2fe48-5d8a-4c9c-beb7-9d8fd82a019b.jpg)

