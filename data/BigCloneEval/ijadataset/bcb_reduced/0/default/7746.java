import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Api {

    private int id;

    private InterfaceServeur serveur = null;

    private static String url;

    private String user;

    private GUI_Client parent;

    public Api(String ur, GUI_Client par) {
        url = ur;
        parent = par;
    }

    public boolean connect(String us, String mdp) {
        try {
            if (serveur == null) serveur = (InterfaceServeur) Naming.lookup(url);
            if (serveur.verifUser(us, mdp)) {
                user = us;
                id = serveur.ajouter_client();
                System.out.println("Le client " + user + " est connect�");
                return true;
            } else {
                JOptionPane.showConfirmDialog(null, "La connexion a �chou�e : utilisateur ou mot de passe incorrect", "Erreur", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
                System.out.println("La connexion a �chou�e... Veuillez recommencer");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            JOptionPane.showConfirmDialog(null, "Le serveur est actuellement indisponible", "Erreur", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void deconnection() {
        try {
            serveur.supprimer_client(id);
        } catch (RemoteException e) {
            JOptionPane.showConfirmDialog(null, "Le serveur est actuellement indisponible", "Erreur", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void envoyer_Msg(String ty, String qt, String refe, String com) {
        try {
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            Document document = constructeur.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
            Element racine = document.createElement("messages");
            racine.setAttribute("id_client", String.valueOf(id));
            Element op = document.createElement("message");
            racine.appendChild(op);
            Element Euser = document.createElement("user");
            Euser.setTextContent(user);
            op.appendChild(Euser);
            Element type = document.createElement("type");
            type.setTextContent(ty);
            op.appendChild(type);
            Element qte = document.createElement("quantite");
            qte.setTextContent(qt);
            op.appendChild(qte);
            Element ref = document.createElement("reference");
            ref.setTextContent(refe);
            op.appendChild(ref);
            Element comm = document.createElement("commentaire");
            comm.setTextContent(com);
            op.appendChild(comm);
            document.appendChild(racine);
            serveur.deposer_Msg(document);
        } catch (RemoteException e) {
            JOptionPane.showConfirmDialog(null, "Le serveur est actuellement indisponible", "Erreur", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            parent.setAnnulerEnvoiMsg(true);
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String recevoir_Msg() {
        try {
            String msg = serveur.recuperer_Msg(id);
            while (msg == null) {
                System.out.println("Pas de message... Attente de 2 secondes...");
                Thread.sleep(2000);
                msg = serveur.recuperer_Msg(id);
            }
            return msg;
        } catch (RemoteException e) {
            JOptionPane.showConfirmDialog(null, "Le serveur est actuellement indisponible", "Erreur", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return "";
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
