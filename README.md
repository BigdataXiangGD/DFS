# Distributed File System
### Guidong Xiang 17301984

The entire Distributed File System consists of many identical DFS servers and one DFS proxy server. 
The DFS proxy server is assumed to be the endpoint for this DFS service. So all clients communicate with this DFS proxy server to get the current master among the DFS servers.The DFS proxy server acts as the introducer in group membership protocol and does not store any file as part of its service.

### Distributed Transparent File Access
The client gets list of servers with file from master and then gets the file from server.

### Replication
The master stores a list of which files are replicated by which nodes and periodically checks the
replication of all files.
