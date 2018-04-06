import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
@WebServlet(
        name="ticketServlet",
        urlPatterns = {"/tickets"},
        loadOnStartup = 1
)
@MultipartConfig(
        fileSizeThreshold = 5_242_880,
        maxFileSize = 20_971_520L,
        maxRequestSize = 41_943_040L
)

public class TicketServlet extends HttpServlet{
    private volatile int TICKET_ID_SEQUENCE = 1;
    private Map<Integer,Ticket> ticketDatabase = new LinkedHashMap<>();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if(action == null)
            action = "list";
        switch (action){
            case "create":
                this.showTicketForm(resp);
                break;
            case "view":
                this.viewTicket(req,resp);
                break;
            case "download":
                this.downloadAttachment();
                break;
            default:
                this.listTickets(resp);
                break;
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if(action == null)
            action = "list";
        switch (action){
            case "create":
                this.createTicket(req,resp);
            case "list":
            default:
                resp.sendRedirect("tickets");

        }
    }

    public void showTicketForm(HttpServletResponse response) throws IOException {
        PrintWriter out = this.writeHead(response);
        out.append("<h2>Create a Ticket</h2>\r\n")
                .append("<form method=\"POST\" action=\"tickets\"")
                .append("enctype=\\\"multipart/form-data\\\">\\r\\n\"")
                .append("<input type=\"hidden\" name=\"action\"")
              .append("value=\"create\"/>\r\n")
                .append("Your Name<br/>\r\n")
        .append("<input type=\"text\" name=\"customerName\"/><br/><br/>\r\n")
        .append("Subject<br/>\r\n")
       .append("<input type=\"text\" name=\"subject\"/><br/><br/>\r\n")
        .append("Body<br/>\r\n")
        .append("<textarea name=\"body\" rows=\"5\" cols=\"30\">")
                .append("</textarea><br/><br/>\r\n")
        .append("<b>Attachments</b><br/>\r\n")
        .append("<input type=\"file\" name=\"file1\"/><br/><br/>\r\n")
        .append("<input type=\"submit\" value=\"Submit\"/>\r\n")
        .append("</form>\r\n");

        this.writeFooter(out);

    }
    public void viewTicket(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String idString = request.getParameter("ticketId");
        Ticket ticket = this.getTicket(idString,response);
        if(ticket == null)
            return;
        PrintWriter writer = this.writeHead(response);
        writer.append("<h2>Ticket #").append(idString)
                .append(": ").append(ticket.getSubject()).append("</h2>")
                .append("<i>Customer Name -").append(ticket.getCustomerName())
                .append("</i><br/>\r\n");
        writer.append(ticket.getBody()).append("<br/><br/>");

        if (ticket.getNumberOfAttachments() > 0){
            writer.append("Attachments: ");
            int i = 0;
            for(Attachment attachment:ticket.getAttachments()){
                if(i++ > 0)
                    writer.append(", ");
                writer.append("<a href=\"tickets?action=download&tickId=")
                        .append(idString).append("&attachment=")
                        .append(attachment.getName()).append("\">")
                        .append(attachment.getName()).append("</a>");
            }
            writer.append("<br/><br/>\r\n");
        }

        writer.append("<a href=\"tickets\">Return to list tickets</a>\r\n");

        this.writeFooter(writer);

    }
    public void downloadAttachment(){}
    public void listTickets(HttpServletResponse response) throws IOException {
        PrintWriter writer = this.writeHead(response);
        writer.append("<h2>Tickets</h2>\r\n");
        writer.append("<a href=\"tickets?action=create\">Create Ticket")
                .append("</a><br/><br/>\r\n");
        if(ticketDatabase.size()==0){
            writer.append("<i>There are no tickets in the system.</i>\r\n");
        } else {
            for(int id : this.ticketDatabase.keySet()) {
                String idString = Integer.toString(id);
                Ticket ticket = this.ticketDatabase.get(id);
                writer.append("Ticket #").append(idString)
                        .append(": <a href=\"tickets?action=view&ticketId=")
                        .append(idString).append("\">").append(ticket.getSubject())
                        .append("</a> (customer: ").append(ticket.getCustomerName())
                        .append(")<br/>\r\n");
            }
        }
        this.writeFooter(writer);
    }
    private void createTicket(HttpServletRequest request,
                              HttpServletResponse response)
            throws ServletException, IOException
    {
        Ticket ticket = new Ticket();
        ticket.setCustomerName(request.getParameter("customerName"));
        ticket.setSubject(request.getParameter("subject"));
        ticket.setBody(request.getParameter("body"));

        Part filePart = request.getPart("file1");
        if(filePart != null && filePart.getSize() > 0)
        {
            Attachment attachment = this.processAttachment(filePart);
            if(attachment != null)
                ticket.addAttachment(attachment);
        }

        int id;
        synchronized(this)
        {
            id = this.TICKET_ID_SEQUENCE++;
            this.ticketDatabase.put(id, ticket);
        }

        response.sendRedirect("tickets?action=view&ticketId=" + id);
    }

    private Attachment processAttachment(Part filePart)
            throws IOException
    {
        InputStream inputStream = filePart.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int read;
        final byte[] bytes = new byte[1024];

        while((read = inputStream.read(bytes)) != -1)
        {
            outputStream.write(bytes, 0, read);
        }

        Attachment attachment = new Attachment();
        attachment.setName(filePart.getSubmittedFileName());
        attachment.setContents(outputStream.toByteArray());

        return attachment;
    }
    public PrintWriter writeHead(HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=utf-8");
        PrintWriter writer = response.getWriter();
        writer.append("<!DOCTYPE html>")
                .append("<html>\r\n")
                .append("    <head>\r\n")
                .append("        <title>Customer Support</title>\r\n")
                .append("    </head>\r\n")
                .append("    <body>\r\n");
        return writer;

    }
    public void writeFooter(PrintWriter writer){
        writer.append("    </body>\r\n").append("</html>\r\n");
    }
    public Ticket getTicket(String idString,HttpServletResponse response) throws IOException {

        if(idString == null || idString.length() == 0){
            response.sendRedirect("tickets");
            return null;
        }
        try {
            Ticket ticket = this.ticketDatabase.get(Integer.parseInt(idString));
            if(ticket == null){
                response.sendRedirect("tickets");
                return null;
            }
            return ticket;
        }catch (Exception e){
            response.sendRedirect("tickets");
            return null;
        }
    }

}
