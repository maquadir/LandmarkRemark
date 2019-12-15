# LandmarkRemark Android App
A simple android mobile application that allows users to save location based notes on a map. These notes can be displayed on the map where they were saved and viewed by the user that created the note as well as other users of the application.

# Application Design & Architecture
The native Android application is written in Kotlin using Android studio using no third-party (non-Google) libraries.Google Firebase cloustore has been used as the backend-as-a-service provider.

The app follows the MVVM architecture with the following concepts utilized in the application
- Data binding(very minimal)
- View Model

<img width="330" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860286-0d6aec80-1f74-11ea-9ce8-dd78a41b8238.png">


### Following are the Program files
- SignInActivity - To sign in the app using a unique "username"
- MapsActivity - The main screen of the app displaying the map and its controls
- LocationViewModel - The view model class to bind the view with UI controller(MapsActivity) data

# Explicit Requirements
- Display current location on the map
- Save a short note at the current location
- See notes that a user have saved at the location they were saved on the map
- See the location, text, and user-name of notes other users have saved
- Search for a note based on contained text or username

# Implicit Requirements
- Signing in by inputing a unique username which is an entry point to the application.
- On singing in , user is presented with a location permissions dialog with the below access actions.
 - Accept - On accepting, the current location of the user is displayed on the map.
 - Deny - The map does not display the current location of user and instead a toast message is displayed informing the user to enable location access. The user in this state will be able to create markers and notes normally.
- A signout button on the action bar to help the user sign out.
- On sign in , the application checks for any location data from the firebase, loads and displays on the map.
- Displaying current user location by using a blue dot.
- Map click event to add a new marker with an associated note presented by an input dialog.The marker is represented by a standard black location icon.
- On clicking the marker, a custom info window is displayed with the following attribute data
  - Note
  - Username(creator)
  - Location(latitude and longitude)
- On clicking an info window, the user who created the note has an option to edit the note and update it
- If a user clicks on info windows not created by him/her , he/she will not be able to edit the note and a toast message is displayed to inform the user.
- On searching based on a note or username, display a toast message of the number of occurences of the search result.
- The searched result displays the corresponding map markers highlighted by a standard red location icon.
- On clearing the search bar the map markers are reset back to the black location icons.

# Application Flow
<img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860312-73577400-1f74-11ea-85bc-93c75cb4187f.jpg">   <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860327-98e47d80-1f74-11ea-8702-e689cd6495db.jpg">   <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860337-ae59a780-1f74-11ea-84ae-b134223d2777.jpg">   <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860387-35a71b00-1f75-11ea-876d-7ff4cd3dc336.jpg">
<img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860412-84ed4b80-1f75-11ea-9bf4-9cbb2279baae.jpg">   <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860421-9c2c3900-1f75-11ea-8df2-aad3dcd7183d.jpg"> <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860438-c67df680-1f75-11ea-95d6-d763ab263ca5.jpg"> <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860452-eb726980-1f75-11ea-93f9-4a7414951781.jpg"> <img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860460-047b1a80-1f76-11ea-914c-d744d0b5a61a.jpg">
<img width="250" alt="architecture" src="https://user-images.githubusercontent.com/19331629/70860466-1f4d8f00-1f76-11ea-950d-47a648fc9f97.jpg">

# Testing Process
- Debug using an emulator or usb device by opening the project in Android Studio
- Build an apk and load it onto a device
- Install the apk included in the zipped project files (not included in git repo)

### Application start
On application start, it will ask for a username to sign in. If no username is entered the application will not open , instead it displays a dialog informing to enter a username.Once signed in using a username the maps screen open asking for permission to access your current location:
If the location permission is denied, the application can still be be used. The user can see landmarks posted by other users, and search them. However, they will not be able to find their own location, but will be able to create any new landmarks on the map.

### Adding a landmark with a note
When the application is started and location permission is approved, then the user is automatically pointed to its current location. The user can create landmark at their current location and any other location on the map. The primary way to create a new landmark is to click any location on the map which will display a dialog to insert a note on that location.After entering a note and clicking ok a landmark with a note is created for that user which can be looked up by clicking on the marker. On clicking cancel, the marker is not added to the map.

### Searching
Click the search icon to display a editable search bar. On typing a valid note text or username the user is automatically pointed to its location with the marker icon changing to red along with a dialog to display the number of found occurences.

### Changing username
Click the Sign out button on the top-right corner to sign out and enter the application with a different username.
Type in a new username and sign in.Any new landmarks you make will have that new username associated with it and it will also display all other markers created by other users

# Technology Stack
- Android Studio using Kotlin
- Firebase Cloudstore
- Google Maps SDK and API
- MVVM Android Architecture

# Limitations
- Firebase database access only till 5th Jan 2020 - After that time, all client requests to your Firestore database will be denied.
- The map view activity of the application resets everytime a new note is added which means the activity is recreated. We can come up with an alternative to avoid this.
- A user cannot edit note of a marker that belongs to another user. A user can only edit notes that were created by him/her.

# Time estimation
Total time 12-24 hours which is more than the recommended due to my obsession with design,covering multiple implicit requirements and testing towards making a good build.
- Brainstorming the problem - 3hrs
- Initial map - 0.5hr
- Current location + permissions - 0.5hr
- Testing and optimizing - 0.5hr
- Adding landmarks - 0.5 hr
- Testing and optimizing - 0.5hr
- Adding Firebase - 1hr
- Testing and optimizing - 0.5hr
- Searching landmarks - 2hr
- Testing and optimizing - 1hr
- Redesigning the app - 2hr
- Testing and optimizing - 0.5hr
- Changing landmark icons to differentiate between searched and existing - 1 hr
- Testing and optimizing - 1hr
- Embedding MVVM in code - 1 hr
- Testing and optimizing - 0.5hr
- Custom dialogs for notes, marker info windows - 1 hr
- Testing and optimizing - 1hr
- Final testing, code cleanup, documentation - 3 hr

# Support
### Reference Material
- Udacity 
- StackOverflow
