# LandmarkRemark Android App
A simple android mobile application that allows users to save location based notes on a map. These notes can be displayed on the map where they were saved and viewed by the user that created the note as well as other users of the application.

# Application Design & Architecture
The native Android application is written in Kotlin using Android studio using no third-party (non-Google) libraries.Google Firebase cloustore has been used as the backend-as-a-service provider.

The app follows the MVVM architecture with the following concepts utilized in the application
- Data binding
- View Model

### Following are the Program files
- SignInActivity - To sign in the app using a unique "username"
- MapsActivity - The main screen of the app displaying the map and its controls
- LocationViewModel - The view model class to bind the view with UI controller(MapsActivity) data

# Application Flow


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

# Technology Stack
- Android Studio using Kotlin
- Firebase Cloudstore
- Google Maps SDK and API
- MVVM Android Architecture

# Time estimation

# Support
### Reference Material
- Udacity 
- StackOverflow
