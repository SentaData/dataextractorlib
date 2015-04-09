##Overview
This is the Library/module that contains static data extraction, dynamic data extraction, a service for sending that data to the server and all the xallegro api calls.
It is used from Dataextractor application. 

![Package Diagram](https://sentasecure.atlassian.net/wiki/download/attachments/9928708/worddav25fd37e303fd97a9e538150e85f65a43.png?version=1&modificationDate=1426530418841&api=v2 "Package Diagram")

##Analysis
###Xallegro Api
There are 3 separate entities. The api action, the http manager that sends the action to the server and the Task that handler the whole procedure (eg execution in a background thread) in a total of 3 layers of abstraction.
Moreover each api call implements the ApiAction interface, so that we can create a separate class for each new xallegro api call. In this way we are respecting the Open-Closed principle.
Currently there are 3 api actions: Generic, Dashboard, Register.
GenericAction: Uses parametrised data like username, password and a list of param elements
DashboardAction: The action for logging in the main xallegro dashboard
RegisterAction: The action for registering a device in the senta secure server using phone's static parameters.
The CallApiTask extends AsyncTask and uses the HttpManager to send the api call, through an Http post request, to the server.

###Otto message bus
The DataextractorLib module uses otto message bus. The message bus is mainly used to send messages from the background/api engine of the module to the front end/ui.
In this way we are reassured that application components stay decoupled and it is easy to add more listeners to the various events that are triggered.
Additionally, whenever an api call returns a result a message is sent in the message bus. All the activities/fragments that have registered in the message bus and have subscribed for the specific event
(using the @Subscribe annotation).

###Parameters
####Static
Static parameters are extracted in StExtractStatic class. Moreover method  createParamList retrieves phone's build and telephony info and creates a list of parameters.
CreateXmlString method in the same class uses this list along with info about phone's sensors, static processes and creates the full list of phone's static parameters.
####Dynamic
Dynamic parameters are extracted in StExtractDynamic class is responsible for extracting phone's dynamic parameters. It implements Runnable interface and in the run method
all the phone's dynamic parameters such as running processes, battery status and memory information are packaged in an xml request and sent to the server.