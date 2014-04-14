WkProcessor
===========

A Java program to perform processing on Wiki database XML dump.

Example of command line:
unxz < frwikivoyage-20140401-pages-meta-history.xml.xz | java -jar WkProcessor-1.0-SNAPSHOT.jar

TODO:
* Find a better way to collect the stats.
* Find out how to use xpath with decent performance.
* Find a better way to map XML objects into Java objects. Maybe with JAXB, or by writing a Python pulldom equivalent.
* Try to use the thread pool.