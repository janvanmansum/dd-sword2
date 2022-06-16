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
project [easy-sword2]{:target=_blank}. As its predecessor, it does **not** implement the full SWORDv2 specifications. Also, since the SWORDv2 specs leave
various important issues up to the implementer, the service adds some features. The best starting point for learning about `dd-sword2` is this document. Where
appropriate it contains references to the SWORDv2 specifications document. Client programmers should consult the [easy-sword2-dans-examples]{:target=_blank}
project.

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
  files of the deposit directories.

#### Admin console

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: application monitoring and management

### Processing

#### Creating and submitting a deposit

TODO

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
This service can be configured by changing the settings
in [`config.yml`](https://github.com/DANS-KNAW/dd-sword2/blob/master/src/main/assembly/dist/cfg/config.yml){:target=_blank}. See the comments in that file for
more information.

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

[deposit directory]: https://dans-knaw.github.io/dd-ingest-flow/deposit-directory/

[easy-sword2-dans-examples]: https://github.com/DANS-KNAW/easy-sword2-dans-examples