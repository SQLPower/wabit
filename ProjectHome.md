SQL Power Wabit Community Edition, sponsored by SQL Power Group Inc, is a cross-platform, open-source ad-hoc reporting tool with a GUI and an embeddable API.

For information about the Community Edition or Enterprise Edition of SQL Power Wabit, please visit http://www.sqlpower.ca/page/wabit

## SQL Power Wabit 1.3.4 available! ##

Noteworthy changes in 1.3.4
Bug Fixes:

  * Fixed an issue with loading manually edited SQL queries

## SQL Power Wabit 1.3.3 available! ##

Noteworthy changes in 1.3.3
Bug Fixes:

  * Improved the performance of OLAP (XML/A) queries.
  * Improved chart exports to PDF.
  * Other minor bug fixes.

## SQL Power Wabit 1.3.2 available! ##

Noteworthy changes in 1.3.2
Bug Fixes:

  * Fixes a bug in the persistence order of OLAP queries properties.
  * Fixed a bug where the query panel toolbar was not being painted correctly when switching from the query pen to the SQL text editor.
  * Fixed a bug in the charts panel where column names were compared in a case-sensitive way.
  * Fixed an IndexOutOfBoundsException (IOOBE) when editing XY type charts.
  * Fixed the bug where a creating a Chart with duplicate columns caused an IOOBE.
  * Fixed a performance issue where memory intensive string formatting was performed when not necessary.
  * Grand totals can now display without sub totals.
  * Image data no longer resides in the user session, resulting in improved loading times.
  * Various other bugs were fixed and several performance optimizations were added.

Improvements:

  * Support for page breaks between each section of a report.
  * Support for custom numeric and date formats in reports.
  * Custom font color for the data cells and headers.

## SQL Power Wabit 1.3.1 available! ##

The 1.3.1 release is the stable and official followup of the 1.3.0 release candidates. We've worked hard at making this release of SQL Power Wabit the most stable and feature rich yet.

Bug Fixes:
  * Parameters no longer reset after doing a print preview
  * Parameters no longer risk disappearance when they are saved and loaded.
  * Relational result sets no longer overflow their allocated space in the thin client
  * Totals are now calculated correctly in all cases in paginated reports
  * The thin client now recognizes when there are no workspace available, rather than displaying 'loading' forever

Improvements:
  * Faster performance when updating a result set in the thin client
  * Result sets embedded in reports display groupings properly
  * Thin client now displays the report parameters



## SQL Power Wabit 1.3.0 Release Candidate 2 available! ##

Noteworthy changes in 1.3.0-RC2

> Bug Fixes
    * Fixes a bad UTF encoding in numeric format options
    * Reports were sometimes printing result set column borders
    * Parameters panel could be displayed for non ContextAware content boxes.
    * OLAP query reset button was not sending reset event to the query panel

## SQL Power Wabit 1.3.0 Release Candidate 1 available! ##

Noteworthy changes in 1.3.0-RC1

> Improvements:
    * Parameters and parameter value selectors have been added report content boxes.
    * Global report parameters and parameter value selectors have been added.
    * SQL Power Wabit thin client libraries are now completely independent and can be embedded in web pages and corporate web sites.

> Bug Fixes
    * Fixes UI freeze when populating a query results table with >1000 rows.

## SQL Power Wabit 1.1.1 Released! ##

Noteworthy Changes in 1.1.1

> Improvements:
    * Uses the SQLP Library's variable insertion GUI.
    * Relational queries can expose variables to other objects.
    * Relational queries can use variables.
    * OLAP queries can expose variables to other objects.

> Bug fixes:
    * Replacing data source when opening local workspace causes problem with SQL queries
    * Wabit deletes entire workspace file if it encounters an exception during save
    * The stop button on the query text editor is never becoming enabled.
    * Initial creation of report on trial server takes too much time
    * Datasource copy issues between servers and clients

## SQL Power Wabit 1.1.0 Released! ##

Noteworthy Changes in 1.1.0

  * Architectural refactoring - Many classes are now part of the SQL Power Library
  * Stability enhancements for SQL Stream queries

## SQL Power Wabit 1.0.1 Released! ##

Noteworthy Changes in 1.0.1

> Bug Fixes
    * Fixes a bug with the server where modifying an image would make all clients crash
    * Fixes a bug with the server where modifying a relational query would make all clients crash
    * Fixes a bug where creating relational queries might corrupt the JCR repository
    * Numerous performance/stability enhancements
    * Ability to create server-side data sources from clients

## Wabit 1.0.0 Released! ##

Wabit 1.0.0 has been released! Notable changes include:

User Interface
  * Added 'Gratuitous Animation' feature to charts

SQL Power Wabit Enterprise Server Support
  * Added support for connecting to, and communicating with the SQL Power Wabit Enterprise Server

Try it out and give us feedback on our forum! (http://www.sqlpower.ca/forum/forums/show/227.page)

## SQL Power Wabit Enterprise Edition Released ##

The Enterprise Edition Server for SQL Power Wabit has been released. Using the server with the SQL Power Wabit client allows:

  * Report scheduling and distribution
  * Live collaboration
  * Fine grained security
  * Thin client web access

To try out the Enterprise Edition  go to the SQL Power Wabit download page at: http://download.sqlpower.ca/wabit/current.html

If you want to use the Enterprise Edition for production use or for support see the product page at: http://www.sqlpower.ca/page/wabit-ep

## Other Projects ##

If you like the SQL Power Wabit, then you may be interested in other open-source projects sponsored by SQL Power:

  * [SQL Power DQguru](http://code.google.com/p/power-matchmaker) (formerly known as the SQL Power MatchMaker) is an open-source data cleansing and de-duping tool.
  * [SQL Power Architect](http://code.google.com/p/power-architect) is an open-source data modeling tool.