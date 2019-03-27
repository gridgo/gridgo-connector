# gridgo-jetty-http

## overview
This connector aims to handle http request by embedded http server [Jetty](https://www.eclipse.org/jetty/)

## endpoint syntax
scheme: `jetty` <br/>
syntax: ` http://{host}[:{port}][/{path}] ` <br/>
where:
- **host**: required - can be ip (both `ipv4` and `ipv6`), `hostname` or `interface name`.
- **port**: optional - default `80`.
- **path**: optional - default `/*` .

## params

### jetty options
- **gzip**: optional - default `false`. if you want response compressed by `gzip`.
- **session**: optional - default `false`. if servlet has `session manager`. 
- **security**: optional - default `false`. 

### other configs
- **http2Enabled**: optional - default `true`.
- **mmapEnabled**: optional - default `true`. If response contains `BReference` which wrap an instance of `File`, responder will try to create MappedByteBuffer to stream data directly to output stream.
- **format**: optional - default `null`. From request side, it use for (and only for) `application/octet-stream` mime, `multipart`, `json` or `text` mimes detected automatically. From response side, it would be used when `content-type` is a kind of `binary`, and the response value cannot be send directly via `sendContent` method (which require `File`, `InputStream` or `ByteBuffer`).

## example endpoint
- http server on port 80: `jetty:http://127.0.0.1/path`.
- http server with gzip enabled, listen on port 8888, on hostname `my.hostname.com`: `jetty:http://my.hostname.com:8888/path?gzip=true`.
