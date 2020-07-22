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


 --fork has to be used with --logpath or --syslog

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
  logAppend: true
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
	mongoimport --port 30000 --username "m103-application-user" --password "m103-application-pass" --authenticationDatabase admin --db applicationData --collection products --file=/dataset/products.json
	
	
	
##Replication
- MongoDB uses Async Statement-Based Replication. Hence, plateform independent.
- Other than PRIMARY and SECONDARY nodes in a replica set, we can also have "ARBITER" node..which holds NO Data, can vote in an election (can serve for ti breaker), cannot become primary ARBITER node will have priority 0.
- Raft is a protocol for implementing distributed consensus.
- Default MongoDB asynchronous replication works on PV1 (Protocal Version 1)
- We should always have ODD number of nodes in a replica set
- Any change in any node configuration is called topology change.
- Replica set can have upto 50 nodes...only max 7 nodes are voting members
- Secondary node can be defined as HIDDEN node..which can have data which is hidden from application. A hidden node will have PRIORITY 0.
- Can be also a DELAYED node...it will have "slaveDelay": 3600...can serve  to provide hot backup

mongo --host "m103-repl/localhost:27001" --username abcs --password asdfasdf --authenticationDatabase admin

- To enabled replica set, Add below in mongod.conf file
security:
  keyFile: /var/mongodb/pki/m103-keyFile
replication:
  replSetName: m103-example    //mongo shell option for this --replSet
  
- Generate keyFile
openssl rand -base64 741 > /var/mongodb/pki/m103-keyFile
- Add chmod 600 /var/mongodb/pki/m103-keyFile
- 
##Replica Set Commands
* rs.initiate() -- To initiate replica set
* rs.status()  	-- Reports health on replica set nodes..get data from heartbeats
* rs.add("localhost:27001") -- To add new node to replica set
* rs.isMaster()			-- Describes a node's role in replica set
* rs.setDown() 			-- To force an election by setting down from current primary 
* db.serverStatus()['repl'] -- Section of the db.serverStatus() output
* rs.printReplicationInfo() -- Only returns oplog data relative to current node. Contains timestamps for the first and last oplog events.
* rs.addArb("localhost:800909") -- To add a arbiter node

##Replica Set reconfiguration
* rs.conf() -- TO get the current configuration of all the nodes
* rs.reconfig(cfg) -- To reconfigure...


##Local DB
* local database contains oplog.rs collection along with other collections. oplog.rs will keep track of all the replications. This is a capped collection that means it will be limited size. It's size is normally 5% of free disk.
* oplog size can also be set in configuration file with below option
replication:
  oplogSize: 5MB
* Replication window is proportional to the system load
* One operation may result in many oplog.rs entries
* Any data written in local database will not be replicated.


##Read and Write 
* rs.slaveOk() -- To commands run from secondary node to tell mongo db to enable read operation 


##Write Concerns
###Write Concerns Level
Higher the level, higher the durability of the data. Means more nodes have received the write.
* 0 - Don't wait for acknowledgement
* 1 (default) - Wait for acknowledgement from the primary only
* =>2 - Wait for acknowledgement from primary and one or more nodes
* 'majority' - Wait for acknowledgement  for majority of replica set members


###Write Concerns Options
* wtimeout: <int> - the time to wait for requested write concern before marking the operation as failed
* j: <true|false> - requires the node to commmit the write operation to journal before returning an acknowledgement

###Write Concerns Commands

* In case for below insert operation, we have 3 node replica set. One node is failed. We will have write operation still committed on healthly nodes. Also, unhealthy nodes will receive the new document once it comes back online.

```
db.employees.insert(
  { "name": "Aditya", "salary_USD": 50000 },
  { "writeConcern": { "w": 3, "wtimeout": 1000 } }
)
```

##Read Concerns

###Read Concerns Level
* local
* available (sharded clusters)
* majority
* linerizable -- majority committed + read your own write functionality. Strongest Durability.

###Read Preference
Read Preference lets you route read operation to specific replica set members:

####Read Preference Modes:
* primary (Default)
* primaryPreferred
* secondary
* secondaryPreferred
* nearest             -- Least latency


##SHARDING
* Horizontal scaling
* Shards: store distributed collections
* Metadata: Information about which data is store in the sharde
* Config Servers: Store metadata of each shards
* Mongos: routes the queries to the correct sharde

##When to do sharding
* When vertical scaling either not possible or not econamical from operational viewerRating
* When data needs to be stored based on geographical location

### Sharding Architecture
* Each shard are configured for replica set to provide high availablity
* Client connects to Mongos process which in turn connects to shard based on required data


### Setting up Sharded Cluster
* Configuration server's configuration file will be similar to the configuration file we created for replica set and below extra option:
sharding:
  clusterRole: configsvr
* Mongos config, it will also be similar but it will not have db file option and also, extra option which contains config server replica set 
sharding:
  configDB: m103-csrs/192.168.103.100:26001,192.168.103.100:26002,192.168.103.100:26003
* Also, all the shards node onfiguration needs to be update with below option:
sharding:
  clusterRole: shardsvr

* With the new configuration start mongos process
mongos -f mongos.conf

* connect to this process
	mongo --port 26000 --username "m103-admin" --password "m103-pass" --authenticationDatabase admin
*  Mongos commands

  * sh.status()
  * sh.addShard("m103-repl/192.168.103.100:27012")

* The mongos configuration file doesn't need to have a dbpath.
* The mongos configuration file needs to specify the config servers.
* Mongos inherit the users created on configuration server
* 


## Shard Key
* Shard key determine the distribution of data in a sharded cluster
* Must use a shard key field which is present in all the documents
* Shard key field must be indexed
	* Indexes must exist First before you can selected the indexed fields for your shard key
* Shard Keys are immutable
	* You can not change the shard key fields post sharding
	* You cannot change the values of shard key fields post sharding.
* Shard Keys are permanent
	* You cannot unshard a sharded collection.
	
	
* A database can contain both sharded collections and unsharded collecitons


##Good Sharding Key
* To provide good write distribution. Shard Key should have
* High Cardinality (lots of unique possible values)
* Low Frequency (very little repetition of those unique values)
* Non-monotonically changing (non-linear change in values)
	
##How to Shard
* Use sh.enableSharding("<database>") to enable sharding for a specified database
* Use db.<collection>.createIndex() to create  the index for your shard key fields
* Use sh.shardCollection("<database>.<collection>", {shard key}) to shard the collection


##Hashed Shard Key
* Provide even distribution for monotonically changing shard keys
* 

## Drawback:
* Queries on ranges of shard key values are more likely to be scatter-grather
* Cannot suppport geographically isolacted read operations using zoned sharding
* Hashed key must be **single non-array field**..that means doesn't support compound index
* Hashed indexes doesn't support fast sorting


###How to create Hashed Shard
* Use sh.enableSharding("<database>") to enable sharding for a specified database
* Use db.<collection>.createIndex({"<field>": "hashed"}) to create  the index for your shard key fields
* Use sh.shardCollection("<database>.<collection>", {"<shard key field>" : "hashed"}) to shard the collection


##Chunk 
* Logical grouping of documents
* Lower limit (min key) is inclusive and uper limit (max key) is exclusive
* Default chunk size is 64MB. A chunk can be of size 1MB <= Chunk Size <= 1024MB
* Chunk size is configurable during runtime
* To lower the value of chunk size, go to db and fire below command
db.settings.save({_id: "chunksize", value: 2}) //2 is in MB

* If the frequency of a shardkey is below less, then 1 chunk becomes very large and it is known as Jumbo Chunk
* A chunk can only live at one designated shard at a time


###Jumbo Chunk
* Larger than defined chunk size
* Cannot move jumbo chunk
	* Once marked jumbo, the balancer skips these chunks and avoids trying to move them
	
* In some cases, these will not be able to split


##Balancer
* Balancer process runs on primay member of Config Server replica set
* Balancer is responsible for evenly distributing chunks across the sharded cluster
* Balancer is an automatic process and requires minimal user configuration


##Mongos
* mongos handles all the queries in the cluster
* mongos builds a list of shard to target a query and merges the result from each shard
* mongos supports standard query modifiers like sort, limit and skip

* sort() - the mongos pushes the sort to each shard and merge-sort the results
* limit() - the mongos passes the limit to each targeted shards and then re-applies the limit to the merged set of results
* skip() - the mongos performs the skipping on merged set of results and doesn't push skip to shard


* Targeted queries require the shard key in the query. 
* In case of compound index as a shard key, we can use shard key prefix to make it targeted query
* Without shard key, mongos must perform a scatter-grather query means it wll check with each shard


#M121
##Aggregation Pipeline
* Pipelines are compostion of stages. Can contain one or more stages.
db.userColl.aggregate([{stage1}, {stage2}, {...stageN}], {options})
* Stages are configured to transform data. Stages are composed of one or more aggregration operators or expressions.
* Some expressions can only be used in certian stages.

* Aggregation Operators: $match, $project etc...
* Query Operators: $gte, $lt, $in etc..


##Filtering using $match
* $match uses standard query operator syntax.
* $match should be the early stage...being the first stage, it takes the advances of indexes. 
* with $text query operator, $match must the first stage. 
* $match does not allow projection.
* You can not use $where with $match

```
db.movies.aggregate([
						{
							$match: {
										"imdb.rating": {$gte: 7},
										"genres": {$nin: ["Crime", "Horror"]},
										"rated": {$in: ["PG", "G"]},
										"languages": {$all: ["English", "Japanese"]}
									}
						}
					]).itcount();
```

##Shaping documents with project
* Syntax
db.collecitons.aggregate([{$project: {.....}}])
* Once we specify one field to retain, we must specify all fields we want to retain. The _id is only expection to this case.

* Beyond simply removing and retaining fields, $project lets us add new fields.
* $project can be used as many times as required in a aggregration pipeline.
* $project can be used to reassign values to existing field names and to derive entirely new fields

```
db.movies.aggregate([
						{
							$match: {
										"imdb.rating": {$gte: 7},
										"genres": {$nin: ["Crime", "Horror"]},
										"rated": {$in: ["PG", "G"]},
										"languages": {$all: ["English", "Japanese"]}
									}
						},
						{
							$project: {
										_id:0 , title: 1, rated: 1
										}
						}
					]);
```
* Another example
```
db.movies.aggregate([
						{
							$project: {
										_id: 0,
										splitTitle: {
											$size: {$split: ["$title", " "]}
										}
									}
						},
						{
							$match: {
								"splitTitle":1
							}
						}
						
					]).itcount();
```


##$addFields
* To retain transformed fields along with existing fields
* Example

```
db.movies.aggregate([
  {
    $match: {
      title: "Life Is Beautiful"
    }
  },
  {
    $addFields: {
      myRating: "$tomatoes.critic.rating"
    }
  }
]).pretty();
```

##$geoNear
* Aggregation stage to perform geo related queries
* $geoNear requires collection to have one and only one 2dsphere index
* If using 2dsphere, the distance is returned in meters. If using legacy co-ordinates, the distance is returned in radians. 
* $geoNear must be the first stage in aggregration pipeline.


##Cursor like stages
* $limit: {integer}
* $skip: {integer}
* $count: {<name we want the count to be called>}
* $sort: {<field on which we want to sort on>, <integer, direction to sort .. -1 mean desc, 1 means 1>}
 
* $sort take advantage of indexes if used early withing a pipeline
* BY default, $sort will only use upto 100MB of RAM. Setting allowDiskUsage: true will allow for larger sorts.

##$sample
* Select a set of random documents from a collection
-------------

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