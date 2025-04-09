package Gui;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import Converter.Client;
import Converter.ClientListBuilder;

public class Controller {

    JFrame bG;

    public Controller(JFrame bG) {
        this.bG = bG;
    }

    public void delClient() {
        Set<Client> list = null;
        try {
            list = ((BoshiGui) BoshiGui.getComponent()).getClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Client> clientArray = new ArrayList<Client>(list);
        quickSort(clientArray);
        JComboBox clientBox = new JComboBox(clientArray.toArray());
        clientBox.setEditable(false);
        clientBox.setMaximumRowCount(5);
        int result = JOptionPane.showConfirmDialog(bG, clientBox, "Select Client To Delete", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Client currentClient = (Client) clientBox.getSelectedItem();
            int n = JOptionPane.showConfirmDialog(bG, "Are you sure to delete " + currentClient.getRealName() + " from database?", "Delete Client Confirmation", JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.YES_OPTION) {
                list.remove(currentClient);
            } else {
                return;
            }
        } else {
            return;
        }
        Document doc = null;
        try {
            doc = new ClientListBuilder().build(list);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        DOMImplementation impl = doc.getImplementation();
        DOMImplementationLS implLS = (DOMImplementationLS) impl.getFeature("LS", "3.0");
        LSSerializer ser = implLS.createLSSerializer();
        ser.getDomConfig().setParameter("format-pretty-print", true);
        String out = ser.writeToString(doc);
        Writer w;
        try {
            w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("clients.xml"), "UTF-16"));
            w.write(out);
            w.flush();
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(out);
    }

    public void modClient() {
        Set<Client> list = null;
        try {
            list = ((BoshiGui) BoshiGui.getComponent()).getClients();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Client> clientArray = new ArrayList<Client>(list);
        quickSort(clientArray);
        JComboBox clientBox = new JComboBox(clientArray.toArray());
        clientBox.setEditable(false);
        clientBox.setMaximumRowCount(5);
        int result = JOptionPane.showConfirmDialog(bG, clientBox, "Select Client To Modify", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Client currentClient = (Client) clientBox.getSelectedItem();
            CreateCompanyPanel.createAndShowGUI(currentClient);
        } else {
            return;
        }
    }

    private static void quickSort(ArrayList<Client> v) {
        quicksort(v, 0, v.size() - 1);
    }

    private static void quicksort(ArrayList<Client> v, int firstIndex, int lastIndex) {
        String key;
        int leftPtr, rightPtr, middle;
        if (firstIndex < lastIndex) {
            middle = (firstIndex + lastIndex) / 2;
            swap(v, firstIndex, middle);
            key = v.get(firstIndex).getID();
            leftPtr = firstIndex + 1;
            rightPtr = lastIndex;
            while (leftPtr <= rightPtr) {
                while ((leftPtr <= lastIndex) && ((v.get(leftPtr)).getID().compareTo(key) <= 0)) leftPtr++;
                while ((rightPtr >= firstIndex) && ((v.get(rightPtr)).getID().compareTo(key) > 0)) rightPtr--;
                if (leftPtr < rightPtr) swap(v, leftPtr, rightPtr);
            }
            swap(v, firstIndex, rightPtr);
            quicksort(v, firstIndex, rightPtr - 1);
            quicksort(v, rightPtr + 1, lastIndex);
        }
    }

    private static void swap(ArrayList<Client> a, int first, int second) {
        Client temp = a.get(first);
        a.set(first, a.get(second));
        a.set(second, temp);
    }
}
