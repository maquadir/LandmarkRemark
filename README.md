# LandmarkRemark Android App
A simple android mobile application that allows users to save location based notes on a map. These notes can be displayed on the map where they were saved and viewed by the user that created the note as well as other users of the application.

# Application Design & Architecture
The native Android application is written in Kotlin using Android studio using no third-party (non-Google) libraries.Google Firebase cloustore has been used as the backend-as-a-service provider.

The app follows the MVVM architecture with the following concepts utilized in the application
- Data binding
- View Model

### Following are the Program files
- SignInActivity - To sign in the app using a unique "username"
- MapsActivity - The main screen of the app displaying the map and the following controls
  - See current location 
  - Save a short note at the current location
  - See notes that a user have saved at the location they were saved on the map
  - See the location, text, and user-name of notes other users have saved
  - Search for a note based on contained text or username
  - Sign out
- LocationViewModel - The view model class to bind the view with UI controller(MapsActivity) data

# Application Flow



# Implicit Requirements

# Technology Stack

# Time estimation
