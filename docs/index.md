dd-sword2
=========

DANS SWORD2 based deposit service

SYNOPSIS
--------

    dd-sword2 { server | check }

DESCRIPTION
-----------




ARGUMENTS
---------

        positional arguments:
        {server,check}         available commands
        
        named arguments:
        -h, --help             show this help message and exit
        -v, --version          show the application version and exit

EXAMPLES
--------
Examples of Java client code are available in the project [easy-sword2-dans-examples](https://github.com/DANS-KNAW/easy-sword2-dans-examples){:target=_blank:}. 



INSTALLATION AND CONFIGURATION
------------------------------

### Installation

Currently this project is built as an RPM package for RHEL7/CentOS7 and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-sword2` and the configuration files to `/etc/opt/dans.knaw.nl/dd-sword2`.

For installation on systems that do no support RPM and/or systemd:

1. Build the tarball (see next section).
2. Extract it to some location on your system, for example `/opt/dans.knaw.nl/dd-sword2`.
3. Start the service with the following command
   ```
   /opt/dans.knaw.nl/dd-sword2/bin/dd-sword2 server /opt/dans.knaw.nl/dd-sword2/cfg/config.yml 
   ```

### Configuration

The configuration options are documented as comments in the
installed [`config.yml`](https://github.com/DANS-KNAW/dd-sword2/blob/master/src/main/assembly/dist/cfg/config.yml){:target=_blank:}.

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
different path, then activate it by using Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

    mvn clean install assembly:single
