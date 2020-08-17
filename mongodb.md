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
* $count: <name we want the count to be called>
* $sort: {<field on which we want to sort on>, <integer, direction to sort .. -1 mean desc, 1 means 1>}
 
* $sort take advantage of indexes if used early withing a pipeline
* BY default, $sort will only use upto 100MB of RAM. Setting allowDiskUsage: true will allow for larger sorts.

##$sample
* Select a set of random documents from a collection
* {$sample: {size : <N, how many documents}}
When N<=5% of number of documents in source collections AND
Source collection has >=100 documents AND
$sample is the first stage

##$arrayElemAt:

```
db.movies.aggregate([
	{
		$match: {
			awards: {$exists: true},
			awards: {$regex: /^Won .*/}
		}
	},
	{
		$project: {
			_id:0,
			title:1,
			oscar:{ $arrayElemAt: [{$split: ["$awards", " "]}, 0]},
		}
	
	}
]);
```

##$group stage
```
{
	$group: 
		{
			_id : <matching/grouping criteria>,
			fieldName: <accumulator expression>,
			.. <as many as fieldName as required>
		
		}
}
```

* Example: here, each time group categories based on year, the sum expression is called
```
{
	$group: {
			_id: "$year",
			num_films_in_year: {$sum: 1}

	}	
}
```
* To group all documents, assign _id to null
```
{
	$group: {
			_id: null,
			count: {$sum: 1}

	}	
}
```
* $group can be used multiple times in a pipeline

```
db.movies.aggregate([
  {
    $match: {
      awards: /Won \d{1,2} Oscars?/
    }
  },
  {
    $group: {
      _id: null,
      highest_rating: { $max: "$imdb.rating" },
      lowest_rating: { $min: "$imdb.rating" },
      average_rating: { $avg: "$imdb.rating" },
      deviation: { $stdDevSamp: "$imdb.rating" }
    }
  }
])
```

##Accumulator expressions with $project 

* Available accumulator expression in project : $sum, $avg, $max, $min, $stdDevPop (Standard Diviation Population), $stdDevSam (Standard Divition Sample)
* Within $project, expressions have no memory between documents
* May still have to use $reduce or $map for complex calculations

$$this refers to current value of array
$$value refers to current accumulator value



##$unwind stage
* $unwind stage is used to unwind a array field
* $unwind has 2 forms
	* Short Form
		```
			$unwind: <field path>
		```
	* Long Form
		```
			$unwind:{
					path: <field path>,
					includeArrayIndex: <string>,
					preserveNullAndEmptyArrays: <boolean>
				}
		```

* $unwind may cause peformance issue on large collections


##Bulk Write:
* Bulk writes allows MongoDB clients to send multiple writes together
* Bulk Writes can be ordered or unordered. 
* Default is "Ordered". In case of ordered, executes writes sequentially and will end execution after first write failure.

##Connection Pooling
* Connection pools allow for reuse of connections
* Subsequent requests appear faster to client
* Default connection pool size in MongoDB is 100.
* Always use connection pooling


##Connection Configuration
* Always try to set wtimeout with writeConcern majority. E.g.
{w: "majority", wtimeout: 5000}

* Always configure for and handle serverSelectionTimeout errors.

##Final Exam
* skip will always be executed before limit. Even if their order is different
```
db.movies.aggregate([
{
	$match: {
		languages: "English",
		cast: {$elemMatch: {$exists: true}},
		"imdb.rating": {$gte: 0}
	}
},
{
	$unwind: "$cast" 
},
{
	$group: {
		_id: "$cast",
		num_of_movies: {$sum:1},
		avarage: {$avg: "$imdb.rating"}
	}
},
{
	$sort: {num_of_movies: -1}
},
{
	$limit:1
}
]);
```

##$lookup stage
* It is similar to join operation in relational database
* Syntax
	$lookup:{
		from: <collection to join>,
		localField: <field from the input document>,
		foreignField: <field from the documents of the "from" collection>,
		as: <output array field>
	}
	
* Here, the "from" collection cannot be sharded
* The "from" collection must be in the same datbase
* The values in "localField" and "foreignField" are matched on equality.
* "as" can be any name, but if it exists in the working document, that will be overwritten

```
db.air_routes.aggregate([
	{
		$match: {
			airplane: {$in: ["747", "380"]}
		}
	},
    {
      "$lookup": {
        "from": "air_alliances",
        "localField": "airline.name",
        "foreignField": "airlines",
        "as": "alliance"
      }
    },
	{
		$unwind: "$alliance"
	},
	{
		$group:{
			_id: "$alliance.name",
			num_of_routes: {$sum:1}
		}	
	
	},
	{
		$sort: {	num_of_routes: -1}
	
	}
  ]);
```

##$groupLookup 
* $groupLookup provides MongoDb a trasitive closure implementation
* $groupLookup provides MongoDB a graph or a graph-like capability
* Syntax
```
	{
		$groupLookup: {
			from: <lookup table>,
			startWith: <expression for value to start from>,
			connectFromField: <field name to connect from>,
			connectToField: <field name to connect to>,
			as: <field name for result array>,
			maxDepth: <optional - max number for recurrasive depth>,
			depthField: <optional - field name for number of iterations to reach this node>,
			restrictSerchWithMatch: <optional - match condition to apply to lookup>
		}
		
	}
```

* connectToField will be used on recursive find operation
* connectFromField value will be used to match connectToField in a recursive match
* depthField determines a field in the result document, which specifies the number of recursive lookups needed to reach that document
* maxDepth allows you to specify the number of recursive lookups
* from collection cannot be sharded
* memory allocation $allowDiskUsage

#Facet
* Introduced in MongoDB 3.4
* Single query facets are supported by the new aggregation pipeline stage $sortByCount.
* As like any other aggregation pipelines, except for $out, we can use the output of this stage, as input for downstream stages and operators, manipulating the dataset accordingly.
```
[
  {"$match": { "$text": {"$search": "network"}}},
  {"$sortByCount": "$offices.city"}
]
```
* $sortByCount is equivalent to a group stage to count occurance, and then sorting in desending order
* Example:

```
	{
		$group: {
			_id: "$imdb.rating",
			count: {$sum:1}
		}
	},
	{
		$sort: {
			count: -1
		}
	}
```

is equivalent to
```
	{
		$sortByCount: {"$imdb.rating"}
	}
```

##Facet Bucket ($bucket stage)
* When we want returned values in ranges
* We must always specify at least 2 values in boundaries
* boundaries must all be of the same general type (String, Numeric)
* **count** is inserted by default with no **output**, but removed when output is specified
* Syntax:

```
	db.companies.aggregate([
		{
			$match: {
						founded_year: {$gt: 1990},
						number_of_employees: {$ne: null} //This is not required if we are using default in $bucket.
					}
		},
		{
			$bucket: {
						groupBy: "$number_of_employees",
						boundaries: [0, 20, 50, 100, 500, 1000, Infinity], //data type must be the same for all
						default: "Other", //When a document cannot be categoried in any of the above buckets, it will come 	//here.
						output: {
									total: {$sum: 1},
									avarage: {$avg: "$number_of_employees"},
									categories: {$addToSet: "$category_code"}
								} //output is optional, it is used to add additional output
					}
		}
	]);
```


##Automatic Buckets ($bucketAuto stage)
* Given a number of buckets, try to distribute documents evenly accross buckets.
* Cardinality of groupBy may impact even distribution and number of buckets
* Syntax
```
	[
	{
		$match: {
			"offices.city": "New York"
		}
	},
	{
		$bucketAuto: {
			"groupBy" : "$founded_year",
			"buckets: 5,
			"output": {
				total: {$sum: 1},
				average: {$avg: "number_of_employees"}
			}
		}
	}
	]
```
* With $bucketAuto stage, we also have an option called "granularity" which has predefined values and help in bucketing the values
* adhere bucket boundaries to a numerical series set by the granularity option.
* Specifying granularity requies the expression to groupBy to resolve to a numeric value

##Multiple Facets using $facet stage
* The $facet stage allows several sub-pipelines to be executed to produce multiple facets.
* The $facet stage allows the application to generate several different facets with one single database request
* The output of the individual $facet sub-pipelines are not shared. That means all the sub-pipelines get the same copy of data
* Syntax
```
db.companies.aggregate([
	{
		$match: {"text" : { $search: "Database"}}
	},
	{
		$facet: 
		{
			"Categories" : [ $sortByCount : "$category_code"],
			"Employees": [
				{
					$match: {founded_year: {$gt: 1980}}
				},
				{
					$bucket: {
								groupBy: "$number_of_employees",
								boundaries: [0, 20, 50, 100, 500, 1000, Infinity],
								default: "Other"
							}
				}
			],
			"Founded": [
				{
					$match: {"offices.city": "New York"},
				},
				{
					$bucketAuto: {
									groupBy: "$founded_year",
									buckets: 5
								}
				}
			]
		}
	}
]);
```

* Another example
```
db.movies.aggregate([
  {
    $match: {
      metacritic: { $gte: 0 },
      "imdb.rating": { $gte: 0 }
    }
  },
  {
    $project: {
      _id: 0,
      metacritic: 1,
      imdb: 1,
      title: 1
    }
  },
  {
    $facet: {
      top_metacritic: [
        {
          $sort: {
            metacritic: -1,
            title: 1
          }
        },
        {
          $limit: 10
        },
        {
          $project: {
            title: 1
          }
        }
      ],
      top_imdb: [
        {
          $sort: {
            "imdb.rating": -1,
            title: 1
          }
        },
        {
          $limit: 10
        },
        {
          $project: {
            title: 1
          }
        }
      ]
    }
  },
  {
    $project: {
      movies_in_both: {
        $setIntersection: ["$top_metacritic", "$top_imdb"]
      }
    }
  }
])
```


##$redact stage 

* Protect the information from unauthorized access
* $redact has following forms
```
 {$redact: <expressions>}
```
* Here expression can be any expression or combination of expressions which ultimately results one of the below values

**$$DESCEND** - Retain the fields at current document level being evaluated except sub-documents
**$$PRUNE**   - Remove all the fields at current document level without further inception
**$$KEEP** 	  - Retail all the fields at current document level without further inception

* Field must be present in all the level of documents
* $$KEEP and $$PRUNE automatically apply to all levels below the evaluated level
* $$DESCEND retains current level and evaluates the next level
* $redact is not for restricting access to a collection

* Example
```
// creating a variable to refer against
var userAccess = "Management"

// comparing whether the value/s in the userAccess variable are in the array
// referenced by the $acl field path
db.employees
  .aggregate([
    {
      "$redact": {
        "$cond": [{ "$in": [userAccess, "$acl"] }, "$$DESCEND", "$$PRUNE"]
      }
    }
  ])
  .pretty()

``` 

##$out stage
* Will create a new collection or overwrite the existing collection if specified
* Honors indexes on existing collections
* Will not create or overwrite data if pipeline errors.
* Creates collection in the same database as the source collection.
* $out stage must be the last stage of a pipeline

* Syntax
```
db.collection.aggregate([ {stage1}, {stage2}...{stageN}, {$out: "new_collection"}]);
```

* $out cannot be used within a facet
* new_collection must be unsharded


##$merge stage
* Introduced in MongoDB 4.2
* Like $out stage, $merge stage must also be the last stage of the pipeline
* Unlike $out stage, $merge stage has below properties
  * new_collection can exist
  * same or different DB
  * can be sharded

* Syntax:
```
{
	$merge: {
		into : <target>,
		on: <fields> //Optional field. It is the matching field during merge. Default value is _id field. Must 	//have unique index present on this field.
	}
}
```

* Example1: Simply specify the collection name of the current database
```
{
	$merge: {
		into : "collection"
	}
}
```

* Example2: specify the collection name and database
```
{
	$merge: {
		into : {db: "db2", coll: "collection2"}
	}
}
```

* Syntax:
```
{
	$merge: {
		into : <target>,
		whenNotMatched: <Optional. Default value: "insert". Other values "discard" and "fail"
		whenMatch: <Optional. Default value: "merge". Other values "replace", "keepExisting", "fail", [...]
					//here [...] can be custom implementation
	}
}


```

#Views
* MongoDB provides non-materized views that means the view is computed everytime a read operation is performed against that view
* Views contain no data themselves. They are created on demand and reflects data in the source collection.
* Views are read-only. Write operations to view will error.
* From user perspective, views are perceived as collections but Views have some restrictions like:
* No Write Operation, No Index Operations (create, update), No renaming, Collation restrictions, No mapReduce, No $text, No geoNear or $geoNear
* find() operations with following projection operators are not permitted.
   * $, $elemMatch, $slice, $meta
   
* View definitions are public. 
* AVoid referring to sensitive fields within the pipeline that defines a view.

* Horizontal slicing is performed with the $match stage, reducing the number of documents that are returned.
* Vertical slicing is performed with a $project stage or other shaping stage, modifying individual documents.

* View syntax
```
	db.createView(<view_name>, <source_collection>, <pipeline>, <collation>)l
```

##$merge Single View Example
```
//$merge updates fields from mflix.users collection into sv.users collection. Our "_id" field is unique username.
//here, db is mflix db.

db.users.aggregate([
	{
		$project: {
				"_id": "$username",
				"mflix": "$$ROOT"
		}
	},
	{
		$merge: {
			"into" : {"db": "sv", "collection": "users"},
			"whenNotMatched": "discard"
		
		}
	}

]);


```


#Aggregation Performance

* Index usage. In a aggregation pipeline, if a stage doesn't usage index then further stages make NO use of index.
* When $limit and $sort are close together a very performant top-k sort can be performed
* Use db.collection.aggregate([{pipeline}],{explain: true});
* Transforming data in a pipeline stage prevents us from using indexes in the stages that follow


* Memory Constraints
* Results are subject to 16MB document limit. 
	* To mitigate this, use $limit and $project stages

* 100MB of RAM per stage
	* To mitigate this, use indexes and as a last resort, use allowDiskUse like
	db.orders.aggregate([...], {allowDiskUse: true});
	
* Below operators cause a merge stage on the primary shard for a database
	* $out
	* $facet
	* $lookup
	* $graphLookup
	
* The Aggregation Framework will automatically reorder stages in certain conditions

* Avoid unneccesary stages, the Aggregation Framework can project fields automatically if final shape of the output document can be determined from initial input.

* Use accumulator expressions, $map , $reduce and $filter in project before $unwind, if possible.
* Every high order array function can be implemented with $reduce if the provided expressions do not meet your needs. 

* Causing a merge in a sharded deployment will cause all subsequent pipeline stages to be performed in the same location as the merge

* The query in a $match stage can be entirely covered by an index

#Final Exam:
* geoNear needs to be the first stage of our pipeline
* $out is required to be the last stage of the pipeline
* $indexStats must be the first stage in a pipeline and may not be used within a $facet
* you cannot use an accumulator expression in a $match stage.
e.g. db.collection.aggregate([{"$match": { "a" : {"$sum": 1}  }}]);
* can not nest a $facet stage as a sub-pipeline.
* 


#M220J
* MongoDB URI
mongodb+srv://username:password@hostname/database

* Use below mongodb driver library
```
<dependency>
	<groupId>org.mongodb</groupId>
	<artifactId>mongodb-driver-sync</artifactId>
</dependency>
```

* MongoDB Driver Base Classes
```
MongoClient mongoClient; //Base connection class that handles the configuration and establish the connection between cluster and application.  
MongoDatabase mongoDatabase;
MongoCollection collection;
Document document;
Bson bson;
```

* By default the Java driver will set a batchSize of 0. Which means the driver will use the server defined batchSize, which by default is 101 documents. However, you can define your own batchSize for a find operation.
 ```
	sortableCollection.find().batchSize(10);
 ```
 
* Cursor methods (sort, limit, skip, batchSize) have equivalent aggregation stages
* The order by which the cursor methods are appended to the find iterable does not impact the results
* But the order by which aggregation stages are defined in the pipeline does!


##Write operations
* doc.append() and doc.put() do the basically the same thing but put method will replace a key if that is already existing.

* Though collection.insertOne(doc) method returns void if all goes well but if there is an error will inserting, it will return MongoWriteException. 

*
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