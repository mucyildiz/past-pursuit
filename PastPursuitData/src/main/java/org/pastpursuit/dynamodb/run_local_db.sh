#!/bin/bash
export USE_LOCAL_ORIGIN=1
export LOCAL_DYNAMODB_ENDPOINT=http://localhost:8000
java -Djava.library.path=./DynamoDBLocal_lib -jar ~/Desktop/dynamodb_local_latest/DynamoDBLocal.jar -sharedDb