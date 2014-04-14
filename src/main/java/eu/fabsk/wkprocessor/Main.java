/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fabsk.wkprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

/**
 *
 * @author fab
 */
public class Main {

    public static void main(String[] args) {
        try {
            // read from standard input
            WkProcessor processor = new WkProcessor();
            if (args.length == 0) {
                processor.parse(System.in);
                processor.show();
            } else {
                // read file in argument
                for (String arg : args) {
                    processor.parse(new FileInputStream(new File(arg)));
                    processor.show();
                }
            }
        } catch (XMLStreamException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
