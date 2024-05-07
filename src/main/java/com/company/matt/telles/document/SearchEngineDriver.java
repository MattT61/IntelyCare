package com.company.matt.telles.document;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SearchEngineDriver {
    private static IndexWriter writer;
    private static final String INDEX_DIR = "c:/temp/lucene6index";

    public SearchEngineDriver() throws Exception {
        writer = createWriter();
    }


    private static Document createDocument(String id, String token) throws IOException
    {
        Document document = new Document();
        document.add(new StringField("id", id , Field.Store.YES));
        document.add(new TextField("token", token , Field.Store.YES));
        writer.addDocument(document);
        return document;
    }

    private static IndexWriter createWriter() throws IOException
    {
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter theWriter = new IndexWriter(dir, config);
        return theWriter;
    }

    public Boolean addTokensForDocumentID(String docID, String[] tokens) throws Exception {

        String sql = "INSERT INTO documents(id, token) VALUES(?, ?)";
        for (Integer idx = 2; idx < tokens.length; ++idx ) {
            createDocument(docID, tokens[idx]);
        }
        writer.commit();
        return true;
    }
    public String[] parseCommand(String cmd) {
        // Split it into tokens

        String[] tokens = cmd.split("\\s+"); // Split by one or more spaces
        return tokens;
    }

    private static Boolean deleteTokensForDocumentID(String id) throws Exception
    {
        try {
            Term term = new Term("id", id);
            writer.deleteDocuments(term);
            long results = 0;
            results = writer.commit();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    private static TopDocs searchByToken(String token, IndexSearcher searcher) throws Exception
    {
        QueryParser qp = new QueryParser("token", new StandardAnalyzer());
        Query query = qp.parse(token);
        return searcher.search(query, 10);
    }

    private static IndexSearcher createSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }

    public DocumentStatus processCommand(String cmd ) throws Exception {
        DocumentStatus returnStatus = new DocumentStatus();

        String[] tokens = parseCommand(cmd);
        if (tokens.length < 1)
        {
            returnStatus.setErrorString("Not enough arguments");
            return returnStatus;
        }


        switch ( tokens[0].toLowerCase(Locale.ROOT) ) {
            case "index":
                returnStatus.setInputCommand("index");
                try {
                    // Try to convert the dc id
                    returnStatus.setDocID(tokens[1]);
                    returnStatus.setDocStatus(true);
                }
                catch (NumberFormatException nfe) {
                    returnStatus.setDocStatus(false);
                    returnStatus.setErrorString("Invalid doc id");

                }

                // Validate the tokens
                if (tokens.length < 3) {
                    returnStatus.setDocStatus(false);
                    returnStatus.setErrorString("No tokens to add.");

                }

                for (int idx=2; idx<tokens.length; ++idx ) {
                    for ( int ch=0; ch<tokens[idx].length(); ++ch ) {
                        if (!Character.isLetterOrDigit(tokens[idx].charAt(ch))) {
                            returnStatus.setDocStatus(false);
                            returnStatus.setErrorString("Invalid characters in token");

                        }
                    }
                }
                // First, delete the existing tokens for this ID
                if (!deleteTokensForDocumentID(returnStatus.DocumentID())) {
                    returnStatus.setDocStatus(false);
                    returnStatus.setErrorString("Error deleting existing tokens from database.");

                }
                // Add the tokens to the document-id
                if (!addTokensForDocumentID(returnStatus.DocumentID(), tokens))
                {
                    returnStatus.setDocStatus(false);
                    returnStatus.setErrorString("Error adding token to database.");

                }
                break;
            case "query":
                returnStatus.setDocStatus(true);
                returnStatus.setInputCommand("query");

                String queryString = "";
                // Combine the tokens remaining after the query command to form the command
                for (Integer idx = 1; idx < tokens.length; ++idx )
                    queryString = queryString + tokens[idx] + " ";
                // And replace all of the boolean operators
                queryString = queryString.replace("|", " AND ");
                queryString = queryString.replace("&", " OR ");
                IndexSearcher searcher = createSearcher();

                // Do search here and return results.
                TopDocs foundDocs = null;
                try {
                    foundDocs = searchByToken(queryString, searcher);
                }
                catch ( Exception e ) {
                    returnStatus.setDocStatus(false);
                    returnStatus.setErrorString("error Error in search string.");
                    break;
                }

                Set<String> doc_ids = new HashSet<String>();
                for (ScoreDoc sd : foundDocs.scoreDocs)
                {
                    Document d = searcher.doc(sd.doc);
                    doc_ids.add( d.get("id") );
                }

                returnStatus.setDocuments(doc_ids);
                break;
            case "stop":
                returnStatus.setDocStatus(true);
                returnStatus.setInputCommand("end");
                break;
            default:
                returnStatus.setDocStatus(false);
                break;
        }
        return returnStatus;
    }
}
