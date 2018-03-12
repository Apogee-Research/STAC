/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infotrader.dataprocessing;

import infotrader.sitedatastorage.ReadDirStruct;
import infotrader.datamodel.Directory;
import infotrader.datamodel.DocumentI;
import infotrader.datamodel.HyperLink;
import infotrader.datamodel.Node;
import infotrader.datamodel.NodeBase;
import infotrader.datamodel.SerializationPosition;
import infotrader.sitedatastorage.DocumentStore;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class SiteMapGenerator {

    //STAC: THE STAC DATA STRUCTURES!!!!!!!!!!!!
    //STAC: THERE ARE FOUR -- THE listofelements, THE tlistofelements, THH model, AND THE doc
    //STAC: The order in which these structrues are used: 
    //1.listofelements->2.tlistofelements->3.model->4.doc->5.listofelements
    //What this means:
    //1.Start with listofelements, the permanent vector/array/linear list of items to add to sitemap -- initially populated
    //at server startup
    //2.transfer permanent list to temporary tlistofelements when processing a user's add doc request (we add to temp list in case exception occurs, allows easy undo
    //if we did not do this, any user error that we did not anticipate could,potentially, cause a STAC exception and make the sitemap unusable for all future requests)
    //3. transfer from simple list state (tlistofelements) to internal tree (model) -- -- tree model is harder
    //to manage from transaction point-of-view than a simple list, so we build this at request time
    //4. doc is the serialization strcuture, XML in this case. We traverse the internal tree to generate the XML structure
    //5. after successful serialization, we transfer our tlistofelements -> listofelements -- this makes
    //our changes permanent
    //STAC:The internal tree representation we traverse when performing serialization algorithm
    public static TreeModel model;
    //The XML tree structure that holds serialized output
    public static Document doc;

    String reqdate;

    //STAC: This is where the actual files are stored
    DocumentStore dstore;

    //STAC:Temporary list of sitemap elements -- it is created everytime a doc is added to sitemap
    //STAC: IT IS TRANSFERRED to a permanent (still in-memory) version, variable below, afer sitemap generation succeeds
    ArrayList<SiteElement> tlistofelements;
    //STAC: The permanent (i.e. in memory only) verion of the sitemap
    private static ArrayList<SiteElement> listofelements = new ArrayList();

    public SiteMapGenerator(DocumentStore dstore) {
        this.dstore = dstore;
        tlistofelements = new ArrayList();

        //STAC: Create a transactional sitemap
        //STAC: It's important to create a temporary transactional version of the sitemap in case an error occurs
        //STAC: we don't want an exception causing a bad document to be permanently added to sitemap
        Iterator<SiteElement> iterator = listofelements.iterator();
        while (iterator.hasNext()) {
            SiteElement n = iterator.next();
            tlistofelements.add(n);
        }
    }

    public void commit_changes_to_sitemap() {

        //STAC: Set the transactional sitemap to become the new permanent sitemap at this point
        //STAC: should be called last and be skipped if exception occurs
        listofelements = tlistofelements;
    }

    //STAC: An object that holds the list of elements in the order they were added
    //STAC:This list is later used to create sitemap internal representation tree
    static class SiteElement {

        String name;
        String type;
        String parent;

        public SiteElement(String name, String type, String parent) {
            this.name = name;
            this.type = type;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {
            SiteElement se = (SiteElement) o;
            if (se.type.equalsIgnoreCase("Hyperlink") || se.type.equalsIgnoreCase("Directory")) {
                return false;
            }
            return name.equalsIgnoreCase(se.name);
        }

    }

    public void init(String reqdate) {

        populatefromCache();

        NodeBase.nodes = new HashMap<>();
        this.reqdate = reqdate;
    }

    public static List<SiteElement> items;

    public static void createCache(String name, String type, String parent) {

        if (items == null) {
            items = new ArrayList<>();
        }

        boolean add = true;
        SiteElement se = new SiteElement(name, type, parent);

        if (add) {
            items.add(se);
        }
    }

    public void populatefromCache() {

        if (items == null) {
            ReadDirStruct.load(dstore);
        }

        Iterator<SiteElement> iterator = items.iterator();
        while (iterator.hasNext()) {
            //permlistofelements.add(iterator.next());
            tlistofelements.add(iterator.next());
        }

    }

    public void create(String name, String type, String parent) {

        boolean add = true;
        SiteElement se = new SiteElement(name, type, parent);
        Iterator<SiteElement> iterator = tlistofelements.iterator();
        while (iterator.hasNext()) {
            SiteElement n = iterator.next();
            if (n.name.equalsIgnoreCase(name) && n.type.equalsIgnoreCase(type)) {
                if (parent == null && n.parent == null || parent.equalsIgnoreCase(n.parent)) {
                    add = false;
                }
            }
        }
        if (add) {
            tlistofelements.add(se);
        }
    }

    //STAC:4.1 - Iterates over list data structure containing all nodes from user/disk
    //build internal tree representation
    private void createNodes(DefaultMutableTreeNode top) throws NodeCreationException {

        Iterator<SiteElement> it = tlistofelements.iterator();
        while (it.hasNext()) {
            SiteElement next = it.next();
            createNode(next);
        }
    }

    //STAC:4.1 (continued): For each list entry,process 
    //and add to internal tree data structure that will be serialized in later step
    private String createNode(SiteElement next) throws NodeCreationException {
        DefaultMutableTreeNode node = null;
        DefaultMutableTreeNode foundParent = (DefaultMutableTreeNode) model.getRoot();

        //System.out.println("createNode:" + next.name);
        boolean exists = checkifExists(next.name);

        if (next.type.equalsIgnoreCase("Directory")) {
            if (next.parent != null) {
                Node p = getNode(next.parent);
                if (p == null || !(p instanceof Directory)) {
                    throw new NodeCreationException("ERROR:Parent directory does not exist");
                }

            }
            node = new DefaultMutableTreeNode(new Directory(next.name));
        }

        if (next.type.equalsIgnoreCase("Document")) {

            Node p = getNode(next.parent);
            //Throw error if no parent directory, null ok -- indicates append to root
            if (p == null || !(p instanceof Directory)) {
                throw new NodeCreationException("ERROR:Parent directory does not exist");
            }
            //not important:Uncomment to force blank document to be entered last to trigger vulnerability
            /*if(next.name.length()==0){
             next.name = next.parent.concat(te.reqdate);            
             }*/
            node = new DefaultMutableTreeNode(new DocumentI(next.name));
        }

        if (next.type.equalsIgnoreCase("HyperLink")) {
            Node p = getNode(next.parent);
            if (p == null || !(p instanceof DocumentI)) {
                throw new NodeCreationException("ERROR:Parent document does not exist");
            }
            SiteElement siteElement = new SiteMapGenerator.SiteElement(next.name, null, null);
            //STAC:4.1.1 - This ensures hyperlink points to valid file
            //It will think the empty string is valid.
            if (tlistofelements.contains(siteElement)) {
                node = new DefaultMutableTreeNode(new HyperLink(next.name));
                //System.out.println("add:" + next.name);
                exists = false;
            } else {
                exists = true;
            }
            //System.out.println("-------------------------------------");
        }

        //STAC:If node was successfully created and does not exist already then...(next comment)
        if (node != null && !exists) {
            //STAC:Find the node's parent and append it
            foundParent = findParent(next.parent);
            if (foundParent != null) {
                foundParent.add(node);
            }
        }
        return "OK";
    }

    private static boolean checkifExists(String nodename) {

        return NodeBase.nodes.containsKey(nodename);

    }

    private static Node getNode(String nodename) {

        return NodeBase.nodes.get(nodename);

    }

    private static DefaultMutableTreeNode findParent(String pnodename) {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

        Enumeration breadthFirstEnumeration = root.breadthFirstEnumeration();

        ////System.out.println("pnodename:" + pnodename);
        while (breadthFirstEnumeration.hasMoreElements() && pnodename != null) {
            DefaultMutableTreeNode nextElement = (DefaultMutableTreeNode) breadthFirstEnumeration.nextElement();
            ////System.out.println("pnodename ch:" + nextElement.getUserObject());
            Object n = nextElement.getUserObject();
            //System.out.println(n.toString());
            //Object str = n.toString();

            //if(pnodename.equals(str)){
            if (n instanceof Directory || n instanceof DocumentI) {
                if (n.toString().equalsIgnoreCase(pnodename)) {
                    return nextElement;
                }
            }
        }

        return root;

    }

    public void genSiteMap() throws NodeCreationException {
        //STAC:model is the internal tree structure used to resresent the sitemap structure.
        //This is not to be confused with the serialized XML tree. The XML is serialized from this tree.
        //A bug in the serialization logic cause the vulnerability
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("");
        model = new DefaultTreeModel(top);
        createNodes(top);

        try {

            //STAC:4.2 Make the XML File that we will serialize the internal tree out to
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            doc = factory.newDocumentBuilder().newDocument();

            // Get tree root...
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

            //STAC:4.2 Serialize our iternal tree to the XML doc
            serializeTree(root, doc);

            NodeList childNodes = doc.getChildNodes();
            Element item = (Element) childNodes.item(0);
            //STAC:don't output an empty string to the XML root as name param -- potential giveway of vulnerability
            item.setAttribute("name", reqdate);
            // Save the document to disk...
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty(OutputKeys.METHOD, "xml");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            doc.normalizeDocument();
            DOMSource domSource = new DOMSource(doc);
            StreamResult sr = new StreamResult(new File("Sitemap.xml"));
            tf.transform(domSource, sr);

        } catch (ParserConfigurationException | TransformerException ex) {
            ex.printStackTrace();
        }
    }

    public static void serializeNode(SerializationPosition state) {

        Element serializedXMLElement = null;
        //Some debugging lines
        //System.out.println("processing node: " + ((Node) state.currPos.getUserObject()));
        //System.out.println("level:"+level);

        Node userObject = (Node) state.currPos.getUserObject();
        //SATC: Determined using reflection 
        if ((Node) state.currPos.getUserObject() instanceof HyperLink) {
            //STAC:other than empty string (which is the name of the root dir) or
            //node links to a document that is not present on the server, 
            //don’t serialize a bad hyperlink
            if (infotrader.datamodel.HyperLink.links_to_existing()) {

                serializedXMLElement = ((Node) state.currPos.getUserObject()).serialize((Node) state.currPos.getUserObject(), state);
                //STAC:Missing return after this line allows fall through to next if stmt
            }
        }
        //STAC:4.3.1.1 This check doesn’t omit the empty string.  As such, it allows the root node to be reprocessed if it
        //is set in the SerializationPosition state by the Hyperlink serialization logic above
        if (infotrader.datamodel.Directory.isa(state.currPos.getUserObject().toString(), state) && serializedXMLElement == null) {
            // STAC: exists allows the hyperlink with empty string name, but no other HyperLinks since
            // the only other ones that could reach here are links to
            // Documents and a Document cannot have the same name as
            // a Directory.
            serializedXMLElement = infotrader.datamodel.Directory.exists(state.currPos.getUserObject().toString(), state);

            if (serializedXMLElement == null) {
                serializedXMLElement = userObject.serialize((Node) userObject, state);
            }

        } else if ((Node) state.currPos.getUserObject() instanceof infotrader.datamodel.DocumentI && serializedXMLElement == null) { // Determined using reflection, not name
            org.w3c.dom.Node parentNode = state.doc.getParentNode();

            boolean exists = false;
            //STAC:4.3.2 -- STOP THE INFINITE LOOP
            //Stop loop by traversing back up the serialization (xml) tree and finding any duplicate document serializations that have
            //allready occurred
            while (parentNode != null) {
                //System.out.println("doc_parent:"+parentNode);
                NamedNodeMap attributes = parentNode.getAttributes();
                if (attributes != null) {
                    org.w3c.dom.Node namedItem = attributes.getNamedItem("name");
                    if (namedItem != null && parentNode.getNodeName().equalsIgnoreCase("doc")) {
                        String nodeValue = namedItem.getNodeValue();
                        if (nodeValue.equalsIgnoreCase(state.currPos.toString())) {
                            exists = true;
                            break;
                        }
                    }
                }
                parentNode = parentNode.getParentNode();
            }
            if (!exists) {
                serializedXMLElement = ((Node) state.currPos.getUserObject()).serialize((Node) state.currPos.getUserObject(), state);
            } else {
                //Redraw the docuemnt anyway, even if duplicate found, just dont traverse its children
                Element oneTimeSerializedXMLElement = ((Node) state.currPos.getUserObject()).serialize((Node) state.currPos.getUserObject(), state);
                state.doc.appendChild(oneTimeSerializedXMLElement);
            }
        }
//System.out.println("parentElement:"+parentElement);
//System.out.println("done processing node:checking children ");
        if (serializedXMLElement != null) {
            //STAC:Append serialized item to XML doc
            state.doc.appendChild(serializedXMLElement);

            //level++;
            Enumeration kiddies = state.currPos.children();
            
            
            while (kiddies.hasMoreElements()) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) kiddies.nextElement();
                SerializationPosition dup = state.dup();
                dup.currPos = child;
                dup.doc = serializedXMLElement;
                //dup.level = level;
                //STAC:Recursively continue to process the tree
                serializeNode(dup);
            }
        }
    }

    //STAC:4.3: Traverse tree and Serialize our internal tree to XML
    protected static void serializeTree(DefaultMutableTreeNode treeNode, Document doc) {

        SerializationPosition s = new SerializationPosition();

        String value = treeNode.getUserObject().toString();
        Element rootElement = doc.createElement("directory");

        s.doc = rootElement;

        doc.appendChild(rootElement);

        Attr attrName = doc.createAttribute("name");

        attrName.setNodeValue(value);
        rootElement.getAttributes().setNamedItem(attrName);

        //STAC:4.3.1 From root, traverse tree. Recursively serialize each node
        Enumeration kiddies = treeNode.children();
        while (kiddies.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) kiddies.nextElement();
            //parseTreeNode(child, rootElement);
            s.currPos = child;

            serializeNode(s);
        }

    }

    protected static void parseTreeNode(DefaultMutableTreeNode treeNode, Element doc) {

        Object value = treeNode.getUserObject();

        Element parentElement = null;
        if (value instanceof DocumentI) {
            parentElement = doc.getOwnerDocument().createElement("doc");

            DocumentI book = (DocumentI) value;
            // Apply properties to root element...
            Attr attrName = doc.getOwnerDocument().createAttribute("name");

            attrName.setNodeValue(book.getCatagory());
            parentElement.getAttributes().setNamedItem(attrName);

        } else if (value instanceof Directory) {
            parentElement = doc.getOwnerDocument().createElement("directory");

            Directory book = (Directory) value;
            // Apply properties to root element...
            Attr attrName = doc.getOwnerDocument().createAttribute("name");
            attrName.setNodeValue(book.getName());
            parentElement.getAttributes().setNamedItem(attrName);
        }

        doc.appendChild(parentElement);

        Enumeration kiddies = treeNode.children();
        while (kiddies.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) kiddies.nextElement();
            parseTreeNode(child, parentElement);
        }
    }

    public static TreeNode visitAllNodes(String term) {
        TreeNode root = (TreeNode) model.getRoot();
        return visitAllNodes(new TreePath(root), term);
    }

    public static TreeNode visitAllNodes(TreePath parent, String term) {

        TreeNode nodet = (TreeNode) parent.getLastPathComponent();
        ////System.out.println(nodet);

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodet;
        Object userObject = node.getUserObject();
        //if(userObject instanceof Node){

        //Node nx = (Node)userObject;
        //String name = nx.getName();
        if (nodet.toString().equalsIgnoreCase(term) && !(nodet instanceof HyperLink)) {
            return nodet;
        }
        // }

        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements();) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                TreeNode v = visitAllNodes(path, term);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

}
