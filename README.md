# NodeCraftAPI
NodeCraft API for Java

Requires json-simple library: https://code.google.com/archive/p/json-simple/

### Download Jar
https://github.com/teej107/NodeCraftAPI/releases/tag/v1

###Code Example
Please review the [API Documentation](https://developers.nodecraft.com) for more details on specific operations and acquiring an API key.
```
NodeCraft nc = new NodeCraft(username, apiKey);
try
{
	Map map = nc.listServices();
}
catch (IOException e)
{
	e.printStackTrace();
}
```
###Documentation
https://teej107.github.io/NodeCraftAPI/NodeCraft%20API/javadocs/com/teej107/nodecraft/NodeCraft.html

