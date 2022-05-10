
# Programming / Discussion Task
Please change (or be prepared to dicsuss how you would change) the code in the following ways:

1. **Modify SQL Query** - the existing query loads the text for all documents associated with a particular docket entry.  The code should be modified to load the text for all documents associated with an entire case.
    * See below for information about the data model.
    * Note this program uses QueryDSL to construct SQL.  QueryDSL allows queries to be constructed in a type-safe manner.
        * BUILD NOTE: querydsl uses annotation processing to generate some classes referenced by `ProcessDoc.java` ; you will need to execute full maven build to get these generated classes and re-sync project in IDE (if using one) 
2. **Find matching lines** - the existing code counts the number of times the word "plaintiff" occurs in all loaded document text and prints the count. The code should be modified to print all lines that indicate what party an attorney represents.
    * Such lines usually take the following form:
        * `Attorneys for Plaintiff`
        * `Counsel for Defendant`
        * `Attorneys for Samsung Electronics`
        * `Attorney for Defendant Apple Inc.`
    * NOTE: You **do not** need to exhaustively discover / find all possible forms of "Attorneys for". 

# Database Schema / Data Model
The included sqlite3 database contains 5 tables. 

A **court** is represented in `dc_court`and has an `id` and a `name`, like "U.S. District Court for the Eastern District of Texas":
```sql
CREATE TABLE dc_court (
    id integer PRIMARY KEY,
    name text   -- the name of the court
);
```
A **case** represents a matter or proceeding before a specific court.  It has a title like "Apple v. Samsung":

```sql
CREATE TABLE dc_case (
    id integer PRIMARY KEY,
    title text,         -- the title of the case
    court_id integer,   -- the associated court (FK)
    filed_on date,      -- the date the case was filed
    terminated_on date, -- the date the case was terminated (or NULL if still open)
    FOREIGN KEY (court_id) REFERENCES dc_court (id) ON DELETE CASCADE
);
```
Activity on a case is represented by docket entries, each of which contain a note about an action taken by one of the parties involved in a case, an attorney, a judge, or court clerk.  Some examples of docket entries:
* Original COMPLAINT filed. Cause: 35:271 Patent Infringement.
* Corporate Disclosure Statement filed by Samsung Electronics.
* ANSWER to Original Complaint by Sandisk Corporation.
* ORDER setting scheduling conference for 4/15/02 at 3:00 in Tyler, Texas scheduling conference for 3:00 4/15/02 before Judge John Hannah Jr (signed by Judge John Hannah Jr)

```sql
CREATE TABLE dc_docket_entry (
    id integer PRIMARY KEY,
    case_id integer, -- the associated case (FK)
    number integer,  -- the docket entry "number", representing the order of docket entries for a case (starting from 1)
    filed_on date,   -- the date the docket entry was made
    text text,       -- the text (note) of the docket entry
    FOREIGN KEY (case_id) REFERENCES dc_case (id) ON DELETE CASCADE    
);
```

Many docket entries have one or more **documents** associated with them.  For example:
* The docket entry with the text "Original COMPLAINT filed. Cause: 35:271 Patent Infringement." would have at least one document associated, the Complaint, which is a document laying out the facts and legal reasons a party is bringing the action to the court.  Other documents may also be attached, like Exhibits, which contain extra information described in the Complaint.
* A docket entry describing an "ORDER" will likely have one document attached, which contains the actual judge's order or ruling on a particular legal matter in the case.

```sql
CREATE TABLE dc_document (
    id integer PRIMARY KEY,
    docket_entry_id integer, -- the associated docket entry (FK)
    number text,             -- the document "number", representing the order of documents for a docket entry (starting from 1)
    description text,        -- a description or title of the document's content
    FOREIGN KEY (docket_entry_id) REFERENCES dc_docket_entry (id) ON DELETE CASCADE
);
```

Documents are obtained from the court's electronic filing system in PDF format, so **document text** must be extracted from the PDF, either through PDF software or OCR software.  The output of each extraction method is stored, so a document will have multiple text outputs associated with it.  A priority value indicates which text output is preferred (highest = preferred).
```sql
CREATE TABLE dc_document_text (
    id bigint PRIMARY KEY,
    document_id bigint,                   -- the associated document (FK)
    "timestamp" timestamp with time zone, -- the timestamp of text extraction
    backend character varying,            -- the method (software name) used for extraction
    priority smallint,                    -- the priority of this particular output
    raw_text text,                        -- the raw text that was extracted
    FOREIGN KEY (document_id) REFERENCES dc_document (id) ON DELETE CASCADE
);
```
