package com.company.matt.telles.document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;

public class IntelyCareSearchEngine {
    private static final String INDEX_DIR = "c:/temp/lucene6index";

    public static void main(String[] args) throws Exception
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        SearchEngineDriver driver = new SearchEngineDriver();

        Boolean complete = false;
        while (!complete) {
            try {
                System.out.print("Enter command: ");
                String s = br.readLine();

                DocumentStatus status = driver.processCommand(s);
                if (status.Status()) {
                    if (status.InputCommand() == "end") {
                        complete = true;
                        continue;
                    }
                    if (status.InputCommand() == "index") {
                        System.out.println("index ok " + status.DocumentID().toString() );
                    } else {
                        System.out.print("query results ");
                        Iterator<String> doc_id_iterator = status.SearchDocuments().iterator();
                        while (doc_id_iterator.hasNext()) {
                            System.out.print(doc_id_iterator.next() + " ");
                        }
                        System.out.println();
                    }
                } else {
                    System.out.println("index error " + status.ErrorString());
                }

            } catch (java.io.IOException exception) {
                System.out.println("Exception in input!");
            }
        }

    }

}

