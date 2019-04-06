# gridgo-netty4

## overview
This connector aims to handle duplex communication, support 2 transport protocols: TCP and WS (both binary and text frame). <br/>
Each connector provide both consumer and producer.

## endpoint syntax
scheme: `netty4` <br/>
syntax: ` {type}:{transport}://{host}[:{port}][/{path}] ` <br/>
where:
- **type** is `server` or `client	`.
- **transport** is `tcp`, `ws` or `wss`.
- **host** can be ip (both v4 and v6), hostname or interface name.
- **port**: required for `tcp` but optional for `ws` (80 by default).
- **path**: required for `ws` transport, optional (and will be ignored) for `tcp`.

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

### ssl config (required if transport == wss)
- **sslContext**: name of `SSLContext` instance which registered in connector context's registry
- **sslProtocol**: (optional) default TLS.
- **keyStoreFile**: (required if `sslContext` is unset) path to ssl keystore file (relative to process working dir).
- **keyStoreType**: (optional) default `JKS`. value is one of `JCEKS`, `JKS`, `DKS`, `PKCS11`, `PKCS12`.
- **keyStoreAlgorithm**: (optional) default `SunX509`. The algorithm of the `keyStoreFile`'s content.
- **keyStorePassword**: (optional) default empty. Password for `keyStoreFile`.

### other config
- **format**: (default `null`) use for serialize/deserialize received data by `BElement.ofBytes(...)`, if it's not set, `null` value passed then it use default serializer in BSerializerRegistry - `msgpack` (or `json` if it's `websocket` with `TextWebSocketFrame`).
- **nativeBytesEnabled**: (default `false`) when developer want to pass origin data (`File`, `ByteBuffer`, `InputStream`) wrapped in a `BReference` instance without any formatting. Note that `format` config will be ignored.
- **autoParse**: (client and server websocket only, default `true` - recommended) boolean value indicate where client/server will/won parse received frame as BElement (if not, byte[] or text passed as a BValue). If `false`, config `format` will be ignored.
- **frameType**: (client and server websocket only) can be `TEXT` (default) or `BINARY` - case insensitive - indicate transmitted frame format . Note that `frameType` only affect on `send` action, `receive` action will detect type and parse by `BElement.ofBytes` and `BElement.ofJson` if `autoParse == true` (if not, `BValue` with `byte[]` or `String` will be passed). 

## binary format
default gridgo-socket-netty4 connector using `BElement` to serialize/deserialize message, if param `format` doesn't specified, default serializer (should be `msgpack` if system properties `gridgo.bean.serializer.binary.default` is absent) will be used.
<br/><br/>
message frames are always prepended by 4 bytes length (a big endian integer).

## example endpoint
- tcp server bind on localhost port 8888, 2 bossThreads and 4 workerThreads, solinger=0: `netty4:server:tcp://localhost:8888?bossThreads=2&workerThreads=4&solinger=0`
- websocket client connect to example.host port 8888, path /test: `netty4:client:ws://example.host:8888/test`
