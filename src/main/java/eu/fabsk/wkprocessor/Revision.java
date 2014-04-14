/*
 * Copyright (C) 2014 F a b i e n  S H U M - K I N G
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
