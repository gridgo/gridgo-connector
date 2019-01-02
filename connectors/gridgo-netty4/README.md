# gridgo-netty4

## overview
This connector aims to handle duplex communication, support 2 transport protocols: TCP and WS (both binary and text frame). <br/>
Each connector provide both consumer and producer.

## endpoint syntax
scheme: ``netty4`` <br/>
syntax: `` {type}:{transport}://{host}[:{port}][/{path}] `` <br/>
where:
- **type** is ``server`` or ``client	``.
- **transport** is ``tcp`` or ``ws``.
- **host** can be ip (both v4 and v6), hostname or interface name.
- **port**: required for ``tcp`` but optional for ``ws`` (80 by default).
- **path**: required for ``ws`` transport, optional (and will be ignored) for ``tcp``.

## params

### underlying socket options
*** all default values are taken from system config
- **solinger**: (integer) socket linger option
- **sobacklog**: (integer) socket option backlog
- **sokeepalive**: (boolean) socket keepalive option
- **sobroadcast**: (boolean) socket broadcast option
- **sorcvbuf**: (integer) socket receive buffer option
- **sosndbuf**: (integer) socket send buffer option
- **soreuseaddr**: (boolean) socket reuse address option
- **sotimeout**: (integer) socket timeout option
- **tcpnodelay**: (boolean) tcp nodelay option

### netty options
- **bossThreads**: (server only) number of thread use for boss group. Default: 1
- **workerThreads**: (client and server) number of thread use for worker group. Default: 1

### other config
- **frameType**: (client and server ws only) can be ``TEXT`` or ``BINARY`` (case insensitive) to indicate transmitted frame format . Note that frameType only affect on ``send`` action, receive will detect type and parse by BElement.fromRaw and BElement.fromJson automatically. 
- **autoParse**: (client and server ws only) boolean config indicate where client/server will/won parse received frame as BElement (if not, byte[] or text passed as a BValue)

## binary format
default gridgo-socket-netty4 connector using BFactory default serializer for serialize/deserialize binary stream. 
<br/><br/>
message frames are always prepended by 4 bytes length (a big endian integer).

## example endpoint
- tcp server bind on localhost port 8888, 2 bossThreads and 4 workerThreads, solinger=0: ``netty4:server:tcp://localhost:8888?bossThreads=2&workerThreads=4&solinger=0``
- websocket client connect to example.host port 8888, path /test: ``netty4:client:ws://example.host:8888/test``
