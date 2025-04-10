public class Test {                public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                    try {
                        osid.shared.Id id = new PID(getFedoraProperty(mRepository, "DisseminationURLInfoPartId"));
                        osid.repository.PartIterator partIterator = mRecord.getParts();
                        while (partIterator.hasNextPart()) {
                            osid.repository.Part part = partIterator.nextPart();
                            {
                                String fedoraUrl = part.getValue().toString();
                                URL url = new URL(fedoraUrl);
                                URLConnection connection = url.openConnection();
                                System.out.println("FEDORA ACTION: Content-type:" + connection.getContentType() + " for url :" + fedoraUrl);
                                tufts.Util.openURL(fedoraUrl);
                                break;
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
}