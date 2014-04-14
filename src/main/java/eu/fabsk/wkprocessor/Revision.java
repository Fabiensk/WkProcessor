/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fabsk.wkprocessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.w3c.dom.Node;

/**
 * Process the data for a revision of a page.
 *
 * @author fab
 */
class Revision {

    String text = null;
    // "\\{\\{dans\\|\\s*([^}]*\\s*)}}"
    private static final Pattern regexDans = Pattern.compile("\\{\\{\\s*dans\\s*\\|\\s*([^}|]*)\\s*(?:\\|[^}]*)?\\s*}}",
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private String parent = null;
    private final Node node;
    private final int id;
    private boolean country = false;
    private int size;

    /**
     * Get the revision.
     *
     * @param node XML node for the revision.
     */
    public Revision(Node node) {
        this.node = node;
        //this.id = Integer.parseInt(XPathQuery.getString(node, XPathQuery.get().getIdExpr()));
        this.id = Integer.parseInt(XPathQuery.getFirstMatchingNodeAsString(node, "id"));
    }

    /**
     * Process data from the page text.
     */
    void prepareData() {
        // get the Wiki text of the page
        this.text = XPathQuery.getString(node, XPathQuery.get().getTextExpr());
        // if not a redirect.
        if (!text.contains("#REDIRECT") && !text.contains("#redirect")) {
            // get name of parent page.
            this.parent = "";
            Matcher match = regexDans.matcher(text);
            if (match.find()) {
                this.parent = match.group(1).replace("_", " ");
            }
            // is it a country page?
            if (text.contains("type=pays")) {
                this.country = true;
            }
            // size of the page is useful for stats.
            this.size = text.length();
        }
    }

    String getParent() {
        return parent;
    }

    public Node getNode() {
        return node;
    }

    public int getId() {
        return id;
    }

    public boolean isCountry() {
        return country;
    }

    public int getSize() {
        return size;
    }

}
