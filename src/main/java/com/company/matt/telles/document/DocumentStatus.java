package com.company.matt.telles.document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class DocumentStatus {

    Boolean docStatus;
    String inputCommand;

    String docID;
    String errorString;

    Set<String> documents = new HashSet<>();

    public DocumentStatus() {
        docStatus = false;
        inputCommand = "";
        docID = "";
        errorString = "";
    }

    public DocumentStatus(Boolean status, String command, String documentID, String errString) {
        docStatus = status;
        inputCommand = command;
        docID = documentID;
        errorString = errString;
    }

    public void setDocStatus(Boolean status) { docStatus = status; }
    public void setInputCommand(String cmd) { inputCommand = cmd; }
    public void setDocID(String id) { docID = id; }
    public void setErrorString(String errString) { errorString = errString; }

    public void setDocuments(Set<String> docIds) { documents = docIds; }

    public Boolean Status() { return docStatus; };
    public String  DocumentID() { return docID; };
    public String ErrorString() { return errorString; };
    public String InputCommand() { return inputCommand; };
    public Set<String> SearchDocuments() { return documents; }


}
