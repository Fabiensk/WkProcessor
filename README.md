WkProcessor
===========

A Java program to perform processing on Wiki database XML dump, espacially French Wikivoyage.

Example of command line:
unxz < frwikivoyage-20140401-pages-meta-history.xml.xz | java -jar WkProcessor-1.0-SNAPSHOT.jar

This program will:
* Build the hierarchy of French Wikivoyage pages (relationship to parent is done by the template "Dans" for the latest revision)
* Collect the number of pages and characters for each countly.
* If a directory "out" is present in the current directory, the XML for the pages will be saved there (takes a lot of disk space).
* Only the pages in the main namespace are processed.

TODO (probably not anytime soon):
* Find a better way to collect the stats.
* Find out how to use xpath with decent performance.
* Find a better way to map XML objects into Java objects. Maybe with JAXB, or by writing a Python pulldom equivalent.
* Try to use the thread pool.
* Write a complete framework
 * Load the page files back to Java objects
 * Provide methods to manipulate Wiki documents
  * Find templates
  * Parse sections, listings, lists…
 * Allow to load either from Database dump, Database or Wikipedia site
 * Allow to write pipelines and filters
