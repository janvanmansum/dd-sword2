dd-sword2
=========

DANS SWORD v2 based deposit service

SYNOPSIS
--------

    dd-sword2 { server | check }

DESCRIPTION
-----------

### Overview

#### Context

The `dd-sword2` service is the [DANS]{:target=_blank} implementation of the [SWORDv2]{:target=_blank} protocol. It is a rewrite in Java of the Scala-based
project [easy-sword2]{:target=_blank}. Like its predecessor, it does **not** implement the full SWORDv2 specifications. Also, since the SWORDv2 specs leave
various important issues up to the implementer, the service adds some features.

The best starting point for learning about `dd-sword2` is this document. Where appropriate, this document contains references to the
[SWORDv2 specifications document]{:target=_blank}. When reading the SWORDv2 docs, keep in mind that it is itself built on other specifications, and refers to
those often, especially:

* [AtomPub]{:target=_blank}
* [Atom]{:target=_blank}
* [HTTP]{:target=_blank}

#### Purpose of the service

At the highest level `dd-sword2` is a service that accepts ZIP packages that comply with the [BagIt]{:target=_blank} packaging format and produces a
[deposit directory]{:target=_blank} for each.

### Interfaces

The service has the following interfaces.

![Overview](img/overview.png){:width=50%}

#### SWORDv2

* _Protocol type_: HTTP
* _Internal or external_: **external**
* _Purpose_: depositing packages, submitting them for archival processing and tracking progress

#### Deposit directories

* _Protocol type_: Shared filesystem
* _Internal or external_: **internal**
* _Purpose_: handing packages to the post-submission processing service and reporting back status changes written by that service to the `deposit.properties`
  files of the deposit directories

#### Admin console

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: application monitoring and management

### Processing

The following sections describe the interaction of a client with the SWORDv2 interface. The examples are [curl]{:target=_blank} commands. The meaning of the shell variables is
as follows:

| Variable           | Meaning                                                                                                                 |
|--------------------|-------------------------------------------------------------------------------------------------------------------------|
| `USER`             | user name of sword client                                                                                               |
| `PASSWORD`         | password of the sword client                                                                                            |
| `SWORD_BASE_URL`   | the base URL of the SWORD service <br/>(the same URL is configured in [config.yml]{:target=_blank} as `sword2.baseUrl`) |

#### Getting the service document

The [service document]{:target=_blank} is an XML document that lets the client discover the capabilities and the supported collections of the service. It can
be [retrieved](https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#protocoloperations_retreivingservicedocument){:target=_blank} with a simple
GET request:

```bash
curl -X GET -u $USER:$PASSWORD $SWORD_BASE_URL/servicedocument
```

#### Creating and submitting a deposit

A deposit is created by [binary file deposit]{:target=_blank}. The other options that SWORDv2 specifies are currently not supported. Furthermore, the only
[packaging]{:target=_blank} that is supported is `http://purl.org/net/sword/package/BagIt`. This means that:

* the payload of the upload must be a ZIP file containing a [bag]{:target=_blank};
* the `Packaging` header must be set to `http://purl.org/net/sword/package/BagIt`.

It is furthermore **mandatory** to send along the `Content-MD5` header. Note that SWORD2 requires the content of this header to be a **hex encoded** MD5 digest,
rather than the base64 encoded MD5 digest specified in [RFC1864]{:target=_blank} about Content-MD5.

If `bag.zip` is such a ZIP file, and there is a collection at path `collections/mycollection`, then it can be uploaded as follows:

```bash
curl -X POST \
     -H 'Content-Type: application/zip' \
     -H "Content-MD5: $(md5 -q bag.zip)" \ 
     -H 'Packaging: http://purl.org/net/sword/package/BagIt' \
     --data @bag.zip -u $USER:$PASSWORD $SWORD_BASE_URL/collections/mycollection
```

(The `md5` command used above is the one from BSD and MacOS. You may have to get the correct output in a different way on other systems.)

If the upload is successful the client will receive a [deposit receipt]{:target=_blank}. This is an Atom Entry document that contains, among other things, the
statement URL (Stat-IRI), which is the URL the client can use to [track post-submision processing](#tracking-post-submission-processing).

#### Continued deposit

If the bag to be uploaded is larger than 1G it is recommended to use a [continued deposit]{:target=_blank}. The client must split the ZIP file into chunks and
send these in separate requests with the `In-Progress` header set to `true` for all chunks except the last. The names of the chunk files must be: the name of the
complete ZIP file, extended with `.n`, where n is the sequence number.

**(1)** The first chunk is sent to the collection URL ([Col-IRI]{:target=_blank} in SWORD terms), **(2)** the subsequent chunks are sent to the SWORD "edit" URL 
([SE-IRI]{:target=_blank}), which can be found in the deposit receipt of the first upload.

The client indicates that it will be sending more chunks by including the header `In-Progress: true`. Since the content of each separate chunk is not a valid 
ZIP file, the `Content-Type` must be set to `application/octet-stream` (which is a fancy way of saying the content consists of bytes).

If `bag.zip.1`, `bag.zip.2` and `bag.zip.3` are the chunks created by splitting `bag.zip`, they can be uploaded as follows:

**Step (1)**

```bash
curl -X POST \
     -H 'Content-Type: application/octet-stream' \
     -H 'In-Progress: true' \
     -H "Content-MD5: $(md5 -q bag.zip.1)" \ 
     -H 'Packaging: http://purl.org/net/sword/package/BagIt' \
     --data @bag.zip.1 -u $USER:$PASSWORD $SWORD_BASE_URL/collections/mycollection
```





**Step (2)**

Parts 2 and 3 sent to the 



#### Finalizing a deposit

TODO

#### Tracking post-submission processing

TODO

ARGUMENTS
---------

        positional arguments:
        {server,check}         available commands
        
        named arguments:
        -h, --help             show this help message and exit
        -v, --version          show the application version and exit

EXAMPLES
--------

Java client code examples are available in [easy-sword2-dans-examples]{:target=_blank}.

INSTALLATION
------------
Currently this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-sword2` and the configuration files to `/etc/opt/dans.knaw.nl/dd-sword2`.

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-sword2`.
3. Start the service with the following command
   ```bash
   /opt/dans.knaw.nl/dd-sword2/bin/dd-sword2 server /opt/dans.knaw.nl/dd-sword2/cfg/config.yml 
   ```

CONFIGURATION
-------------
This service can be configured by changing the settings in [config.yml]{:target=_blank}. See the comments in that file for more information.

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 11 or higher
* Maven 3.3.3 or higher
* RPM

Steps:

    git clone https://github.com/DANS-KNAW/dd-sword2.git
    cd dd-sword2 
    mvn clean install

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM packaging will be activated. If `rpm` is available, but at a
different path, then activate it by using Maven's `-P` switch: `mvn -Prpm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single

[DANS]: https://www.dans.knaw.nl/

[SWORDv2]: https://sword.cottagelabs.com/previous-versions-of-sword/sword-v2/

[easy-sword2]: https://dans-knaw.github.io/easy-sword2/

[BagIt]: https://datatracker.ietf.org/doc/html/rfc8493

[bag]: https://datatracker.ietf.org/doc/html/rfc8493

[deposit directory]: https://dans-knaw.github.io/dd-ingest-flow/deposit-directory/

[easy-sword2-dans-examples]: https://github.com/DANS-KNAW/easy-sword2-dans-examples

[SWORDv2 specifications document]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html

[binary file deposit]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#protocoloperations_creatingresource_binary

[service document]: https://www.ietf.org/rfc/rfc5023.html#section-8

[AtomPub]: https://www.ietf.org/rfc/rfc5023.html

[Atom]: https://www.ietf.org/rfc/rfc4287.html

[HTTP]: https://www.rfc-editor.org/rfc/rfc2616.html

[config.yml]: https://github.com/DANS-KNAW/dd-sword2/blob/master/src/main/assembly/dist/cfg/config.yml

[packaging]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#packaging

[continued deposit]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#continueddeposit

[deposit receipt]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#depositreceipt

[Col-IRI]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#terminology

[SE-IRI]: https://swordapp.github.io/SWORDv2-Profile/SWORDProfile.html#terminology

[curl]: https://www.man7.org/linux/man-pages/man1/curl.1.html

[RFC1864]: https://www.rfc-editor.org/rfc/rfc1864.html