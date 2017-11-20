# VoiceChat
## Description
Voice communicator using socekts for Computer Networks project at [Poznań University of Technology](http://fc.put.poznan.pl/index.php)  
Client written in Java, server in ANSI C


## Requirements for client
* Java 8
* JavaFX (included in Java 8)
* Maven

## Requirements for server
* Linux (might be **Bash on Ubuntu on Windows**)  

**Warning Java 9 may not run, because of some problems with JavaFX libraries**

## How to run client
clone repository  
mvn clean package  
mvn exec:java

## How to run server
cd /src/main/c/server/  
make  
./server
