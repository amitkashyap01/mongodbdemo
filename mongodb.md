##mongod Defaults
----------------

port: 27017			(To change: mongod --port <port number>)
DB path: /data/db   (To change: mongod --dbpath <directory path>
bind_ip: localhost	(To allow clients running on 123.123.123.123: mongod --bind_ip 123.123.123.123)
auth: disabled		(To enable: mongod --auth) 


###command line options 		Configuration File Options
--auth							security.authorization
--dbpath						storage.dbPath
--logpath						systemLog.path and systemLog.destination
--bind_ip						net.bind_ip
--replSet						replication.replSetName
--keyFile
--sslPEMKey
--sslCAKey
--sslMode
--fork (run a deamon)

##mongod Configuration File (in yaml)
  
storage:
  dbPath: /var/mongodb/db
net:
  bindIp: localhost
  port: 27000
security:
  authorization: enabled
systemLog:
  path: /var/mongodb/logs/mongod.log
  destination: file
processManagement:
  fork: true  //mongod process will run in the background
  





###To use this configuration file...use command	
mongod --config "/etc/mongod.conf"
or
mongod -f "/etc/mongod.conf"

	
##To create a new user:
-----------------------
mongo admin --host localhost:27000 --eval '
  db.createUser({
    user: "m103-admin",
    pwd: "m103-pass",
    roles: [
      {role: "root", db: "admin"}
    ]
  })
'
	

##Basic commands
----------------
###Basic Helper Groups
1. db.<method>(); //To help with db related operations
  *  db.<collection>.<method>();
1. rs.<method>(); //To help with Replica Set related operations
1. sh.<method>(); //To help with sharding related operations



###User Management
* db.createUser();
* db.dropUser();

###Collection Management
* db.renameCollection();
* db.collection.createIndex();
* db.collection.drop();

###Database Management
* db.dropDatabase();
* db.createCollection();

###Database Status
* db.serverStatus();

###Database Commands
db.runCommand({ <COMMAND> });
db.commandHelp("<command>");


###Logging Basics
* db.getLogComponents();
* db.setLogLevel(0, "index");
  * -1 means logging verbosity is inheriated from parent
  * 0 default value
  * can be 1-5
  
* To access the log from Mongo Shell -> db.adminCommand({ "getLog": "global" }) 
* To access the log from Command Prompt -> tail -f <path-to-log-file> 

###To shutdown mongod process
use admin
db.shutdownServer()


##MongoDB Profiler
When enabled, system will store all the profiling details on below operations in a new DB called **system** in a collection **profile**
1. CRUD
1. Adminitrative Operations
1. Configuration Operations

####Commands 
db.getProfilingLevel()
db.setProfilingLevel(1) // db.setProfilingLevel(1, {slowms: 0})

* 0 (default -  don't profile) 
* 1 (profile slow ops)
* 2 (profile all ops)


##Authentication Mechanisms
-- Four types for client authentication mechanisms
1. SCRAM (Default) - Salted Challenge Response Authentication Mechanisms
1. X.509 
1. LDAP     (avialable in enterprise version)
1. KERBEROS (avialable in enterprise version)

There is also, interal cluster authentication

##Authorisation
-- Role Based Access Control (RBAC)(roles - priveledges)

###Roles
- Role is a set of priveledges. A priveledge is a set of actions on resources (database, collection, clustral level resource).
- A role can have Network Authentication Restrictions (clientSource, serverAddress)

###Built-In Roles
* Database User - read, readWrite, readAnyDatabase, readWriteAnyDatabase
* Database Administration - dbAdmin, userAdmin, dbOwner, dbAdminAnyDatabase, userAdminAnyDatabase
* Cluster Administration  - clusterAdmin, clusterManager, clusterMonitor, hostManager
* Backup/Restore - backup, restore
* Super User - root

###userAdmin
- Only deals with user management. Cannot create any database or update any data/db
db.createUser({
	user: 'security_officer',
	pwd: :'security_officer',
	roles: [{db: 'admin', role: 'userAdmin'}]

});

###dbAdmin
- To adminitrating database. Can do DDL, but not DML.
db.createUser({
	user: 'dba',
	pwd: :'dba',
	roles: [{db: 'm103', role: 'dbAdmin'}]

});

###dbOwner
- Combination of readWrite, userAdmin and dbAdmin. Can do any adminitrative actions
- A user can have different role on different db
- Let's give dba user created previously a dbOwner role of playground db
db.grantRolesToUser('dba', [{db: 'playground', role: 'dbOwner'}]



###TO get info on role
db.runCommand({rolesInfo: {role: 'dbOwner', db: 'playground'}, showPrivileges: true})

###How to do
1. Enable authentication by adding below in configuration file
security:
  authorization: enabled
  
  * This will enable authentication and authorization
  
1. Add a user in admin db with root priveledge

db.createUser({
	user: "root",
	pwd: "root",
	roles: ["root"]
	});

1. Start the mongo shell by providing the details

mongo --port 27000 --username root --password root --authenticationDatabase admin



##MongoDB Server Tools
* mongostat
	mongostat --port 30000
	
* mongodump - dump the BSON representation of mongodb
	mongodump --port 30000 --db applicationData --collection products

* mongorestrore - restore the BSON representation of mongodb
	mongorestore --drop --port 30000 dump/
	
* mongoexport - export the JSON/csv representation of mongodb
	mongoexport --port 30000 --db applicationData --collection products -o products.json
	
* mongoimport - import the JSON/csv representation of mongodb
	mongoimport --port 30000 --username "m103-application-user" --password "m103-application-pass" --authenticationDatabase admin --db applicationData --collection products products.json

mongo "mongodb+srv://sandbox-2mtgq.mongodb.net/test" --username m001-student  --password m001-mongodb-basics


insertMany has options to insertMany
1) Ordered Insert (Default option): As soon as there is an error, it will stop inserting

2) Unordered Insert: Add {"ordered": false}

db.moviesScratch.insertMany([  {
								"_id": "tt124"
								"title": "ABCD"
								},
								{
								"_id": "tt124"
								"title": "ABCD"
								}
								],
								{
									"ordered" : false
								}
								);
								
								

 db.myMovies.updateOne(
						{"title":"Star Trek"}, 
						{$set:
							{"year": 2010}
						});

 db.myMovies.updateOne(
						{"title":"Star Trek"}, 
						{$inc:
							{"year": 2010}
						});



db.movieDetails.find(
						{"writers": 
						
							{$in: ["Ethan Coen", "Joel Coen"]}
							
						}).count();
						
						
						
db.movieDetails.find(
						{$or : 
								[
									{watlev: "always dry"}, 
									{depth: 0}
								]
						});


// Array field operator


db. movieDetails.find(
						{genres: 
							{	$all: ["Comedy", "Drama"] //not nessarily in order
							}
						},
						{"_id":0, title:1, genres: 1})
						
						.pretty();
						
						
						
How many documents contain at least one score in the results array that is greater than or equal to 70 and less than 80?			
{results: {$elemMatch: {$gte:70, $lt:80}}}
						
						

db. movieDetails.find(
						{countries: 
							{	$size: 2} //where array size is 2
						},
						{"_id":0, title:1, genres: 1})  // we call it projection
						
						.pretty();




{$and: [{tripduration: null}, {tripduration: {$exists: true}}]}




db.surveys.find(

				{results: 
					{$elemMatch: {"product": "abc", "score":7}}
				});
				
				
				
// RegEx


 db. movieDetails.find(
						{ "awards.text": {$regex: /^Won .*/}},  
						{_id: 0, "awards.text":1})
					.pretty();
						
						
{cast: {$in: ["Jack Nicholson", "John Huston"]}, viewerRating: {$gt: 7}, mpaaRating: "R"}