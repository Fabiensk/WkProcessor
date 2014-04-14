/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fabsk.wkprocessor;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author fab
 */
public class WkProcessor {

    private static final String outputPath = "./out";
    private Map<String, List<String>> hierarchy = new HashMap<>();
    private Set<String> countries = new HashSet<>();
    private Map<String, Figures> allFigures = new HashMap<>();
    private boolean hasFileOutput;

    public WkProcessor() throws Exception {
        File outDir = new File(outputPath);
        hasFileOutput = (outDir.exists() == true && outDir.isDirectory() == true);
        if (hasFileOutput == false) {
            System.out.println("Directory 'out/' does not exist in current directory, no file output for pages.");
        }
    }

    /**
     * Process the XML stream.
     *
     * @param stream XML stream
     * @throws XMLStreamException
     * @throws ParserConfigurationException
     */
    void parse(InputStream stream) throws XMLStreamException, ParserConfigurationException {
        // reader producing events on XML elements
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(stream);

        // builds DOM for the pages
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = null;
        Node curNode = null;

        // process the XML document. Create a new DOM for each page.
        // Assumes that the document contains only nodes, which themselves contain
        // either children nodes or text data
        while (reader.hasNext()) {
            int type = reader.next();
            switch (type) {
                // new node in input, create a new DOM or a node in the DOM
                case XMLStreamReader.START_ELEMENT:
                    String curTag = reader.getLocalName();
                    if (curTag.equals("page")) {
                        // new page => new DOM
                        doc = builder.newDocument();
                        curNode = doc.createElement(curTag);
                        doc.appendChild(curNode);
                    } else if (doc != null) {
                        // node in the DOM
                        Element newNode = doc.createElement(curTag);
                        curNode.appendChild(newNode);
                        curNode = newNode;
                    }
                    break;
                case XMLStreamReader.CDATA:
                case XMLStreamReader.CHARACTERS:
                    // text inside a node
                    if (doc != null && curNode != null) {
                        curNode.appendChild(doc.createTextNode(reader.getText()));
                    }
                    break;
                case XMLStreamReader.END_ELEMENT:
                    // end of input node
                    if (curNode != null) {
                        curNode = curNode.getParentNode();
                        if (curNode.getNodeType() != Node.ELEMENT_NODE) {
                            try {
                                // end of the page node, do some processing for this page.
                                completePage(doc);
                            } catch (Exception ex) {
                                Logger.getLogger(WkProcessor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            curNode = null;
                            doc = null;
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Display a DOM as text to standard output.
     *
     * @param doc XML to display.
     */
    private static void display(Document doc) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource domSource = new DOMSource(doc);
            transformer.transform(domSource, new StreamResult(System.out));
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    /**
     * Do processing after having read the page.
     *
     * @param doc XML document containing the page and its revisions.
     * @throws Exception
     */
    private void completePage(Document doc) throws Exception {
        Page page = new Page(doc);
        String title = page.getTitle();
        // skip non-main-namespace pages
        if (title.contains(":")) {
            System.out.format("Skipping: %s\n", title);
            return;
        }

        // writing revisions of a page in individual files is disabled (too slow)
        // page.writeRevisionsToDisk();
        // Collect data from the page text
        page.collectData();
        // add the page in the page parent-children map
        String parent = page.getParent();
        List<String> children = hierarchy.get(parent);
        if (children == null) {
            children = new ArrayList<>();
            hierarchy.put(parent, children);
        }
        children.add(title);

        // complete the list of countries
        if (page.isCountry()) {
            countries.add(title);
        }
        // collect size of page
        Figures fig = new Figures();
        fig.size = page.getSize();
        allFigures.put(title, fig);

        // write the page to disk for further processing
        if (hasFileOutput == true) {
            page.writePageToDisk(outputPath);
        }
    }

    /**
     * Display the consolidated statistics.
     */
    final void show() {
        show("", 1);
        // country stats
        System.out.println("=== Countries stats ===");
        for (String country : countries) {
            Figures figures = new Figures();
            collectCountryFigures(country, figures);
            System.out.format("%s;%d;%d\n", country, figures.count + 1, figures.size);
        }
    }

    /**
     * Get the numbers for a given country.
     *
     * @param country Country name.
     * @param figures Return values.
     */
    final void collectCountryFigures(String country, Figures figures) {
        List<String> children = hierarchy.get(country);
        figures.size += allFigures.get(country).size;
        if (children != null) {
            figures.count += children.size();
            for (String c : children) {
                collectCountryFigures(c, figures);
            }
        }
    }

    /**
     * Display hierarchy from a given page.
     *
     * @param key Page to display.
     * @param indent Indentation level.
     */
    void show(String key, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int cnt = 0; cnt < indent; ++cnt) {
            sb.append('-');
        }
        sb.append(' ').append(key);
        System.out.println(sb.toString());
        List<String> children = hierarchy.get(key);
        if (children != null) {
            for (String c : children) {
                show(c, indent + 1);
            }
        }
    }

    /**
     * Contains some numbers.
     *
     * To refactor.
     */
    private static class Figures {

        public Figures() {
        }
        public int count = 0;
        public int size = 0;
    }

}
