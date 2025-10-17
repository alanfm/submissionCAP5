package org.apache.xerces.dom;

import org.w3c.dom.DOMLocator;
import org.w3c.dom.Node;

public class DOMLocatorImpl implements DOMLocator {
    private final int lineNumber;
    private final int columnNumber;
    private final Node relatedNode;

    public DOMLocatorImpl(int lineNumber, int columnNumber, Node relatedNode) {
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        this.relatedNode = relatedNode;
    }

    @Override
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return columnNumber;
    }

    @Override
    public Node getRelatedNode() {
        return relatedNode;
    }

    public String getLocationDetails() {
        return "Line: " + lineNumber + ", Column: " + columnNumber;
    }
}