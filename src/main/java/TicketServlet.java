import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

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
                this.listTickets();
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
                this.createTicket();
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


    }
    public void downloadAttachment(){}
    public void listTickets(){}
    public void createTicket(){}
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
