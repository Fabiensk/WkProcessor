/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fabsk.wkprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author fab
 */
class Page {

    private String title;
    private final Document doc;
    private List<Revision> revisions = new ArrayList<Revision>();
    private String parent;
    private boolean country;
    private int size;

    public Page(Document doc) throws Exception {
        this.doc = doc;
        this.title = XPathQuery.getString(doc, XPathQuery.get().getTitleExpr());
        // get the revisions
        Object evaluate = XPathQuery.get().getRevisionExpr().evaluate(doc, XPathConstants.NODESET);
        NodeList nodes = (NodeList) evaluate;
        for (int cnt = 0; cnt < nodes.getLength(); ++cnt) {
            revisions.add(new Revision(nodes.item(cnt)));
        }
        Collections.sort(revisions, new Comparator<Revision>() {

            @Override
            public int compare(Revision r1, Revision r2) {
                return r1.getId() - r2.getId();
            }

        });
    }

    String getTitle() {
        return title;
    }

    public String getParent() {
        return parent;
    }

    public boolean isCountry() {
        return country;
    }

    public int getSize() {
        return size;
    }

    void writeRevisionsToDisk(String outputPath) throws Exception {
        // create dir
        StringBuilder articleDirName = new StringBuilder();
        articleDirName.append(File.separatorChar).append(title.replace("/", "\\"));
        File articleDir = new File(articleDirName.toString());
        if (articleDir.exists() == true) {
            if (articleDir.isDirectory() == false) {
                throw new Exception("Non-directory entry exists: " + articleDirName);
            }
        } else {
            if (articleDir.mkdir() == false) {
                throw new Exception("Failed to create directory: " + articleDirName);
            }
        }

        // create entries for each page
        for (Revision revision : revisions) {
            String revisionId = XPathQuery.get().getString(revision.getNode(), XPathQuery.get().getIdExpr());
            if (revisionId != null) {
                writeRevisionToFile(revisionId, articleDirName, revision.getNode());
            }
        }
    }

    /**
     * Write the page XML to a file.
     * @param title Title of the page.
     * @throws Exception 
     */
    void writePageToDisk(String outputPath) throws Exception {
        StringBuilder articleFileName = new StringBuilder(outputPath);
        articleFileName.append(File.separatorChar).append(title.replace("/", "\\"));
        File file = new File(articleFileName.toString());
        if (file.exists() == false) {
            writeXmlToFile(file, doc);
        }
    }

    /**
     * Write the XML under a node into a file.
     * @param file File target.
     * @param node Node to write.
     * @throws Exception 
     */
    private void writeXmlToFile(File file, Node node) throws Exception {
        // write to file
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        DOMSource domSource = new DOMSource(node);
        transformer.transform(domSource, new StreamResult(file));
    }

    /**
     * Write a revision of a page into a file for later processing.
     * 
     * Slow, not called.
     * @param revisionId Id of revision, used to build the filename.
     * @param articleDirName Directory for this page.
     * @param revisionNode XML Node for the revision.
     * @throws Exception 
     */
    private void writeRevisionToFile(String revisionId, StringBuilder articleDirName, Node revisionNode) throws Exception {
        // build path
        StringBuilder filename = new StringBuilder(articleDirName);
        filename.append(File.separatorChar).append(revisionId);
        // write to file
        writeXmlToFile(new File(filename.toString()), revisionNode);
    }

    /**
     * Collect the data before display.
     */
    void collectData() {
        Revision rev = revisions.get(revisions.size() - 1);
        rev.prepareData();
        this.parent = rev.getParent();
        this.country = rev.isCountry();
        this.size = rev.getSize();
    }
}
