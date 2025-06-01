@echo off

echo Step 1: Navigating to client directory...
cd client

echo Step 2: Cleaning and packaging the client project...
start cmd /k "mvn clean package && mvn javafx:run"

echo Step 3: Returning to the root directory and navigating to server...
cd ..

echo Step 4: Navigating to server directory...
cd server

echo Step 5: Cleaning and packaging the server project...
start cmd /k "mvn clean package && mvn exec:java"

exit
