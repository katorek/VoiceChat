# VoiceChat Communicator
## Description
Voice communicator using sockets for Computer Networks project at [Poznań University of Technology](http://fc.put.poznan.pl/index.php)  
Client written in Java, server in ANSI C.  
Client sends data from microphone, sends to server which send data to others connected clients.  
Server reads users from /src/main/c/server/users.txt and adds them to userlist. Default 10 is max users count. 5 max connections. To change limits edit MAX_CONNECTIONS and MAX_USERS const variables.  


## Requirements for client
* Java 8
* JavaFX (included in Java 8)
* Maven

## Requirements for server
* Linux (might be **Bash on Ubuntu on Windows**)  

**Warning Java 9 may not run, because of some problems with JavaFX libraries**

## How to run client
1. clone repository  
2. mvn clean package  
3. mvn exec:java

## How to run server
1. cd /src/main/c/server/  
2. make  
3. ./server  

**Note. To add more users from the beggining, edit users.txt in format username;password;username2;password2;... and so on.**
