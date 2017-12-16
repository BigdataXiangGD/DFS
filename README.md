# Distributed File System
### Guidong Xiang 17301984


The entire Distributed File System consists of many identical DFS servers and one DFS proxy server. 
The DFS proxy server is assumed to be the endpoint for this DFS service. 
So all clients communicate with this DFS proxy server to get the current master among the DFS servers.
The DFS proxy server acts as the introducer in group membership protocol and does not store any file as part of its service.

### features implemented
- Distributed Transparent File Access
- Replication
- Caching

### How to run programs
Step 1 - install maven in local, then cd into the project root directory, run command: mvn package -Dmaven.test.skip=true -U

Step 2 - Run SDFSProxy, ssh into the vm machine 1 , cd into the project root directory, run scripts/run_sdfsproxy.sh > ~/log.txt

Step 3 - Run SDFSFileServer, ssh into the vm machine ,  cd into the project root directory, run scripts/run_sdfsserver.sh > ~/log.txt

Step 4 - Run SDFSClient, You can run any number of clients you want in the group and at any machine
         cd into the project root directory, run scripts/run_sdfsclient.sh, Input fileops commands into client
