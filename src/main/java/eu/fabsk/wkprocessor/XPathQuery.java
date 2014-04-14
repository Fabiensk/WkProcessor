/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fabsk.wkprocessor;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Precompile some xpath expression to search in the XML DOM.
 * 
 * To refactor.
 * @author fab
 */
final class XPathQuery {

    private static final XPathQuery instance = allocate();
    private final XPathFactory factory = XPathFactory.newInstance();
    private final XPathExpression titleExpr;
    private final XPathExpression revisionExpr;
    private final XPathExpression idExpr;
    private final XPathExpression textExpr;

    /**
     * Compile.
     * @throws Exception 
     */ 
    private XPathQuery() throws Exception {
        this.titleExpr = factory.newXPath().compile("/page/title");
        this.revisionExpr = factory.newXPath().compile("/page/revision");
        this.idExpr = factory.newXPath().compile("id");
        this.textExpr = factory.newXPath().compile("text");
    }

    /**
     * Find a string node.
     * 
     * Surprisingly slow to search revision id.
     * @param node Node from which we search.
     * @param expr xpath
     * @return Found string, or null if not found.
     */
    static String getString(Node node, XPathExpression expr) {
        try {
            Object evaluate;
            evaluate = expr.evaluate(node, XPathConstants.STRING);
            if (evaluate instanceof String) {
                return (String) evaluate;
            }
            return null;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WkProcessor.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Search by hand the first child node having a name.
     * @param node Parent node.
     * @param name Name of child node.
     * @return Found node, or null if not found.
     */
    static Node getFirstMatchingNode(Node node, String name) {
        NodeList nodes = node.getChildNodes();
        for (int cnt = 0; cnt < nodes.getLength(); ++cnt) {
            Node child = nodes.item(cnt);
            if (child.getNodeName().equals(name))
                return child;
        }
        return null;
    }
    
    /**
     * Search by hand the text of the first child node having a name.
     * @param node Parent node.
     * @param name Name of child node.
     * @return Text of found node, or null if not found.
     */
    static String getFirstMatchingNodeAsString(Node node, String name) {
        Node found = getFirstMatchingNode(node, name);
        if (found==null) {
            return null;
        } else {
            return found.getTextContent();
        }
    }

    public static XPathQuery get() {
        return instance;
    }

    public XPathExpression getTitleExpr() {
        return titleExpr;
    }

    public XPathExpression getRevisionExpr() {
        return revisionExpr;
    }

    public XPathExpression getIdExpr() {
        return idExpr;
    }

    public XPathExpression getTextExpr() {
        return textExpr;
    }

    private static XPathQuery allocate() {
        {
            try {
                return new XPathQuery();
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
