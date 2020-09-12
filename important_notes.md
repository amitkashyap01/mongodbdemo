- Difference between BSON and JSON? Why MongoDB uses BSON?
- Transactions in MongoDB.
- $slice and $substr
- $addToSet
- to disable journaling for replica set members using the WiredTiger storage engine?
- exporting mongodb collection into csv/
- $sample
- a correct definition for idempotence
- What settings can be controlled by rs.reconfig()? 
- features of primary shard?
- Does the hashed index provide extra security?






Important:
* By default, test DB is connected when we start mongo.
* Default size of a document is 16MB, default chunk size is 64MB (can be set 1MB to 1GB), default size of oplog is 5% of free disk (for 64bit machine, it is 192MB)

* Maximum number of 64 indexes are possible on a collection. Maximum size of key size is 1MB.

* In $type, we can provide either string or integer value of BSON type. Here, to search for a field containing null, provides it's integer equivalent BSON type i.e. 10. for example, db.hello.find({c: {$type: 10}}); 

* MongoDB stores system information in collections that use the "<database>.system.*" namespace, which MongoDB reserves for internal use. Do not create collections that begin with system.
 

- _id field of a document is immutable. That means it cannot be updated/replaced using MongoDB query.
- As of MongoDB 4.0, journaling cannot be disabled for replica set members.
https://docs.mongodb.com/manual/core/journaling/

- mongodump will export the documents in BSON. It is also the preferred way to transfer documents from one instance of MongoDB to another instance.

However, if you need to export to a CSV file, you would use mongoexport.

The correct answer is the one that includes --type=csv, which tells which format we want mongoexport to use for the output. The default type is JSON.

- Hashed indexes cannot be unique.

- Journaling cannot be disabled for replica set members.
- Replication is a feature handled at a higher level in the mongod process. **A storage engine has the mission to store and retrieve documents from cache and memory**. Replication, Sharding, processing of MongoDB Query Language queries and more, are all done in higher layers in the mongod process.

- mongodump will export the documents in BSON. It is also the preferred way to transfer documents from one instance of MongoDB to another instance. If you need to export in any other format, you need to you mongoexport with --type for destination type.

- **Covered Queries** All fields used in the selection filter of the query must be in the index, so the system can find the documents that satisfy the selection filter without having to retrieve the document from the collection.

All fields returned in the results must be in the index, so again there is no need to retrieve the full document. A common mistake is not to provide a projection that filters out the field _id, which is returned by default. If the _id field is not a field in the index definition, it is not available, and the query system will need to fetch the full document to retrieve the value.

On the other hand, it is OK to ask for more fields than the ones provided in the selection filter, as long as those are in the index values, the system has all the information needed to avoid fetching the full document from the collection.

- To use $text in the $match stage, the $match stage has to be the first stage of the pipeline. Views do not support text search.

- If you have a delayed member in your replica set, for example, a delay of one hour, it will take one hour before changes on the Primary are replicated to this member.

If a user were to drop a collection or database on the Primary, you would have one hour to go to this delayed member to retrieve the destroyed data.

You can also query older versions of your documents, however, you can't choose a historical version to retrieve as you only get the one that existed one hour ago.

- The Oplog collection only contains an entry for a given write query if the operation has modified a document. Oplog supports importency which is helpful in case of recovery.

- When a chunk is in flight, reads and writes from the application can still access the documents in that chunk. Modifications on documents are propagated to the shard where it is migrated.

Until the chunk is fully migrated, the shard (donor) that is sending it to another shard (receiver) is the only location where the all documents are present in their latest form. For that reason, the donor shard is processing the reads.

- For a given database in a cluster, not all collections may be sharded. As a matter of fact, you are likely to shard only the very large collections. For the ease of management and to provide features like $lookup across collections, it makes sense to group all non-sharded collections together, and this location is referred to as the Primary Shard for this given database. Other databases in the cluster are likely to have a different Primary Shard to level the space and load between the shards.

As a note, the term Primary Shard is used here, so be careful not to confuse this notion with the Primary replica in a replica set.


- MongoDB uses Raft protocol to elect secondary node as primary in case of failover scenario.

- MongoDB Architecture:

* Layer1: MongoDB Query Language (MQL): MongoDB drivers uses this layer to send and receive MongoDB data as BSON.
* Layer2: MongoDB Document Data Model - It manages namespaces, indexes and data structures. 
* Layer3: Storage Layer - I contains storage engine like wiredTiger which is the default storage engine for MongoDB. 

Along with these layers, we have 2 veritical layer which are Security and Admin.

- For insertMany() method, the default is ordered insertion and when an error occured, the insertation will stop. To keep the insertation continue in case of error, we need to provide another argument to it as {"ordered": false}

- using upsert: true as the third parameter of update() method after filter and update paramenters, means if the filter condition doesn't match any document, insert a new document in database.

- findAndModify() - by default return the document before update. If new: true, it will return the updated document.
- \_id can also be a document provided it is unique in the collection.
 ```
 e.g. 
  db.hello.insert({_id: {a: 1, b: 2}, c: 8});
  

 ```

- db.hello.find({d: {$ne: 9}}); method will return all those documents as well which do not have d field at all.
- db.hello.find({d: null}); This will return all the those documents where d matches with null as well those documents where key d is not there.

###Array Operators:
* $all : match all the documents where all the elements specified in $all are present (NOT necessirly in order)
* $size
* $elemMatch


* db.hello.distinct("d");  It will return all the distinct values of field d from all documents
* db.hello.distinct("d", {c:1});  It will return all the distinct values of field d where documents have value for field c as 1.
* Order of cursor methods in which those work. It doesn't matter in which order you have metioned in your query
1. sort
2. skip
3. limit

* db.hello.update({d:32}, {c:121}); PLease note that this command will replace all the fields with c:121 where d is 32.
* db.hello.update({d:32}, {$set: {c: 124}}); This is the case where only field c will be updated.

## Indexes
```
 #To create index
 db.hello.createIndex({c:1, d:1});
 
 
 #To get indexes
 db.hello.getIndexes();
 
 #Important to understand
 db.hello.find().sort({c:1, d:1});  - In this case index will be used
 db.hello.find().sort({c:-1, d:-1}) - In this case index will be used.
 
 db.hello.find().sort({c:1, d:-1}) - In this case, index will NOT be used.
 db.hello.find().sort({d:1, c:1}) - In this case, index will NOT be used.
 
 So, it is important to note that sequence of the field is important as well as sorting order must be as per the index which is created.
 
```


* Multikey indexes do not support covered queries.

* explain() method has following arguments
 * queryPlanner: This is default. This will not execute the query
 * executionStats: This will execute the query
 * allPlansExecution: This is for most verbose output. This will also execute the command.

* db.hello.createIndex({a:1}, {unique:true}) - To create unique index on field a. This will throw an error if same value is inserted twice for field a.

* You cannot create unique index on a field which is not present in all the documents. For such fields, we can use {unique: true, sparse: true}
* Sparse index cannot be used for sorting.

* With version mongodb 4.2:
 * We can crete index in hybrid manner which has advantage of performance of foreground index creating and advantage of no locking of background index creation. Hybrid index builds are only type available now.
 * With 4.2 version, we can have index key size limit more than 1024 bytes.
 
 
### Aggregation
* The only limitation with $match operator is, it cannot use $where operator and if it is using $text, it must be first stage in pipeline.
* $sort can take advantage of index if this is placed early in the pipeline. Otherwise it will do in-memory sorting and we can use allowDiskUsage:true for allowing sorting to use disk space.
* $lookup is similar to Left Outer Join.

* $out stage will out the aggregation output to a new collection in the same database. If the collection already exists, it will override. Also, this collection must be unsharded.

* $merge is an improvement over $out where the output to can be merged with an existing collection and the new output collection can exist in the same database or other database. Here, the collection can be shared as well.

* In case of $out, $facet, $lookup and $graphLookup, primary shard will do the work of merging results from all the shards. In case other operators, merging will happen on random shard.




