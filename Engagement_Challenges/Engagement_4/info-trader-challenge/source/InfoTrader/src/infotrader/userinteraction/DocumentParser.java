package infotrader.userinteraction;

import infotrader.dataprocessing.DocumentDisplayGenerator;
import infotrader.sitedatastorage.ReadDirStruct;
import infotrader.dataprocessing.NodeCreationException;
import infotrader.dataprocessing.SiteMapGenerator;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import infotrader.parser.exception.InfoTraderParserException;
import infotrader.parser.exception.InfoTraderWriterException;
import infotrader.parser.exception.WriterCancelledException;
import infotrader.parser.model.Address;
import infotrader.parser.model.Corporation;
import infotrader.parser.model.InfoTrader;
import infotrader.parser.model.Header;
import infotrader.parser.model.Multimedia;
import infotrader.parser.model.Note;
import infotrader.parser.model.Source;
import infotrader.parser.model.SourceSystem;
import infotrader.parser.model.StringWithCustomTags;
import infotrader.parser.parser.InfoTraderParser;
import infotrader.parser.writer.InfoTraderWriter;
import static infotrader.messaging.controller.module.RunInfoTrader.clean;
import infotrader.sitedatastorage.DocumentStore;
import infotrader.sitedatastorage.DocumentStore.FileLink;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class DocumentParser extends HttpServlet {

    SiteMapGenerator te;
    DocumentStore dstore;

    public DocumentParser() throws IOException, NodeCreationException {

        dstore = new DocumentStore();

        te = new SiteMapGenerator(dstore);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String reqdate = sdf.format(cal.getTime());
        te.init(reqdate);
        te.genSiteMap();
        te.commit_changes_to_sitemap();

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected synchronized void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            //STAC:All request go through here -- synchronized keyword insures a singleton server, less likely to have unintended bug
            StringBuffer requestURL = request.getRequestURL();
            URL url = new URL(requestURL.toString());
            System.out.println("path:" + url.getPath());

            String path = url.getPath();
            switch (path) {
                case "/gdoc.cgi":
                    getDoc(request, response);
                    break;
                case "/doc.cgi":
                    putDoc(request, response);
                    break;

                default:
                    throw new IllegalArgumentException("ERROR:Unknown request type");
            }
        } catch (InfoTraderParserException ex) {
            response.getWriter().println(ex.getMessage());
        } catch (InfoTraderWriterException ex) {
            response.getWriter().println(ex.getMessage());
        } catch (NodeCreationException ex) {
            response.getWriter().println(ex.getMessage());
        }
    }

    //STAC:This function implements document retrieval functionality. You pass it a document name and it reads the document in from disk
    //and sends the contents back to you.  It also allows you to specify a 'getAll' param. When this param is true, getDoc
    //will follow the links in the document you requested and <bold>recursively</bold> get all of the contents of the documents linked to as well.
    //This results in red herring vulnerability that allows one to enter a cycle. It is a red herring because the response 
    //is quick and does not violate time/space budgets. It will, however, result in a StackOverFlowException which is 
    //handled gracefully by the InfoTrader server.
    private void getDoc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InfoTraderParserException, InfoTraderWriterException {

        boolean recurse = false;
        String name = request.getParameter("name");
        DocumentDisplayGenerator display = new DocumentDisplayGenerator();

        if (name.startsWith("Sitemap")) {
            dstore.loadSitemap(display);
        } else {
            String getAll = request.getParameter("getAll");
            if (getAll.equalsIgnoreCase("true")) {
                recurse = true;
            }

            Map<String, DocumentStore.FileLink> links = new HashMap<String, DocumentStore.FileLink>();

            dstore.loadDoc(response, name, links, recurse, display);
        }
        response.getWriter().print(display.getResult());
    }

    private void putDoc(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InfoTraderParserException, WriterCancelledException, InfoTraderWriterException, NodeCreationException {
        //STAC:1 InfoTrader Receives putDocs request. Results in callback to POST handler servlet. Writes out file POST on input stream to local file store.
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String reqdate = sdf.format(cal.getTime());
        //BufferedReader reader = request.getReader();
        ServletInputStream inputStream = request.getInputStream();
        //BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] contents = new byte[1024];

        int bytesRead = 0;
        String strFileContents = "";
        while ((bytesRead = in.read(contents)) != -1) {
            strFileContents += new String(contents, 0, bytesRead);
        }

        String decode = java.net.URLDecoder.decode(strFileContents, StandardCharsets.UTF_8.name());

        long reqtime = System.currentTimeMillis();
        //Turn the request params into a Mime formatted string for parsing by a Mime parser

        //STAC:We write the email to disk -- later we delete it, when we have finished procesing
        String timeString = Long.toString(reqtime);
        clean("./message");
        File message = new File("message/" + timeString + ".ged");
        PrintWriter out = new PrintWriter(message);
        out.print(decode);
        out.close();

        //STAC: Create a new copy of the sitemap
        te = new SiteMapGenerator(dstore);
        //STAC:2 Registers newly uploaded document 
        te.init(reqdate);

        //STAC:3 Calls parser to extract doc name, intended location, links in document
        InfoTraderParser gp = new InfoTraderParser();
        gp.load(message);

        InfoTrader g = gp.infoTrader;
        Header header = g.getHeader();
        SourceSystem data = header.getSourceSystem();
        StringWithCustomTags productName = data.getProductName();
        System.out.println("Document name:" + productName);

        String systemId = data.getSystemId();
        System.out.println("Folder to store document:" + systemId);
        if (ReadDirStruct.restricteddir.containsKey(systemId)) {
            throw new NodeCreationException(systemId + "not a valid dir");
        }
        //STAC:Add the doc to the sitemap data structure
        te.create(productName.toString(), "Document", systemId);

        /*Map<String, Note> notes1 = g.getNotes();
         Collection<Note> values = notes1.values();
         Iterator<Note> it = values.iterator();
         while (it.hasNext()) {
         Note nextnote = it.next();
         //Collection<Note> values = g.getNotes().values().;
         System.out.println("noteheader:" + nextnote.toString());
         }*/
        //STAC:Process the links
        Map<String, Source> sources = g.getSources();
        Collection<Source> sourcesvalues = sources.values();
        Iterator<Source> its = sourcesvalues.iterator();
        Map<String, Source> links = new HashMap<String, Source>();
        if (its.hasNext()) {
            Source next = its.next();
            System.out.println("File to link to:" + next.getTitle());
            if (!links.containsKey(next.getTitle().get(0))) {
                //STAC:Add the link to the sitemap data structure
                te.create(next.getTitle().get(0), "HyperLink", productName.toString());
                links.put(next.getTitle().get(0), next);
            }
            /*Iterator<Note> itns = next.getNotes().iterator();
             while (itns.hasNext()) {
             Note nextnote = itns.next();
             System.out.println("notesrc:" + nextnote.toString());
             }*/
        }

        //STAC: Store the uploaded document to the data store
        //STAC: Intentional minor bug -- document is stored to data store even if it does not get added to sitemap without causing an exception
        dstore.writeDoc(g, reqdate);
        //STAC: Generate out sitemap
        //STAC: If exception occurs -- it will be handled by Netty and the lines after this one will not be committed
        te.genSiteMap();

        //STAC: Don't add anything to the sitemap permanently that causes an error
        //The commit_changes_to_sitemap command makes the new sitemap entries permanent -- we should not get here if error occured
        te.commit_changes_to_sitemap();

        //STAC: We made it, no exceptions
        response.getWriter().println("OK");
    }

    private static String readFile(String file) throws IOException {

        String encode = URLEncoder.encode(file.toString(), "UTF-8");

        BufferedReader reader = new BufferedReader(new FileReader("reports/" + encode));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }

}
