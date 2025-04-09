public class Test {    protected TreeModel createIndex(URL _url) throws Exception {
        LineNumberReader rin = new LineNumberReader(new InputStreamReader(_url.openStream()));
        int ps = -1;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(_("Index"));
        Stack stack = new Stack();
        stack.push(root);
        while (true) {
            String l = rin.readLine();
            if (l == null) break;
            l = FuLib.replace(l, "\t", "        ");
            int ns = 0;
            while ((ns < l.length()) && (l.charAt(ns) == ' ')) ns++;
            if (ns == l.length()) continue;
            ns /= 2;
            l = l.trim();
            if ("".equals(l)) continue;
            if (ns > ps) {
                ns = ps + 1;
                DefaultMutableTreeNode pn = (DefaultMutableTreeNode) stack.peek();
                DefaultMutableTreeNode cn = new DefaultMutableTreeNode(l);
                pn.add(cn);
                stack.push(cn);
            } else if (ns == ps) {
                stack.pop();
                if (stack.isEmpty()) break;
                DefaultMutableTreeNode pn = (DefaultMutableTreeNode) stack.peek();
                DefaultMutableTreeNode cn = new DefaultMutableTreeNode(l);
                pn.add(cn);
                stack.push(cn);
            } else if (ns < ps) {
                while (ns <= ps) {
                    stack.pop();
                    if (stack.isEmpty()) break;
                    ps--;
                }
                DefaultMutableTreeNode pn = (DefaultMutableTreeNode) stack.peek();
                DefaultMutableTreeNode cn = new DefaultMutableTreeNode(l);
                pn.add(cn);
                stack.push(cn);
            }
            ps = ns;
        }
        rin.close();
        return new DefaultTreeModel(root);
    }
}