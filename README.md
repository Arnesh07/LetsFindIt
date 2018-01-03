# LetsFindIt
Project repository for Winter of Code '17 (SDS MDG, IIT Roorkee)



This project aims to use the power of community to find lost objects quicher and with more ease. Whenever a user adds a new lost object, a geofence is created around the probable location of losing the object. Whenever another user enters that geofence, he is notified of the lost object(s) in that area, keeping the memory of the object fresh for the time where the user is in the geofence. If the user finds and reports the object, a chat-box is made available to connect the two users.

The project uses Google Firebase realtime database to store the information, as well as Firebase Authentication. Google places API is used to gather information about the location of the lost object. GeofencingClient from Google Play services is used to create and monitor the geofences.
