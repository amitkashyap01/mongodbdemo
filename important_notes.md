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
- _id field of a document is immutable. That means it cannot be updated/replaced using MongoDB query.
- As of MongoDB 4.0, journaling cannot be disabled for replica set members.
https://docs.mongodb.com/manual/core/journaling/

- mongodump will export the documents in BSON. It is also the preferred way to transfer documents from one instance of MongoDB to another instance.

However, if you need to export to a CSV file, you would use mongoexport.

The correct answer is the one that includes --type=csv, which tells which format we want mongoexport to use for the output. The default type is JSON.

- Hashed indexes cannot be unique.

