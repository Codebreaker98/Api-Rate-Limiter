# API RATE LIMITER

This project provides a solution to handle multiple API calls from various users and rate limit them based on some specified predefined limits in specified Time Frames.

## Video Demonstration
You may view the [demonstration of the project here](http://tiny.cc/apiRateLimitSoaTeam01).

## Compilation
* Install JDK 1.8
* Place the ```api_rate_limit_source``` folder where you want to set up the project.
* For Eclipse
    * Open your Eclipse IDE. 
    * Import the project as Maven Project.
    * Wait till ```Maven``` install all the dependencies.
    * You can check the progress in the bottom-right corner of your Eclipse.
    * Now click on the project and run as Java Application.
* For IntelliJ
    * Open IntelliJ IDEA
    * Import project or Open Project and select ```pom.xml``` file in the root dir.
    * It'll start installing the ```Maven``` dependencies.
    * Once complete, open ```com.blueoptima.apirate.TestapiApplication.java``` and run its ```main()``` method.
* The project is configured to run on port ```8080```.
* If you get an error like **port already in use**, then open the file ```src/main/resources/application.properties``` and uncomment the ```server.port``` line and give a port according to your choice.

## Additional dependencies
* The project depends on ```Firebase``` for its database.
* DB credentials are already added in the project in ```/serviceAccountKey.json``` for smooth compiling and it'll automatically connect to the database.
* To view the database, please send us your email ID and we can grant access. After that, you will be able to view the database [here](https://console.firebase.google.com/u/0/project/spring-api-test-4a18e/firestore).

### API Endpoints
```/api/addOrg``` : used for adding an Organisation to the database.
```/api/addOrUpdateEndpoint``` : used for setting the Organizations endpoints.
```/api/deleteEndpoint``` : used for deleting the Organization endpoints.
```/api/checkLimit``` : used for check cheking if the Clients have enough limits to access the Org APIs.

