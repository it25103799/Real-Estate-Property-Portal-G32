package com.realestate.portal.controller;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/submitInquiry")
public class SubmitInquiryServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String buyerName = (String) session.getAttribute("loggedUser");

        if (buyerName == null) {
            response.sendRedirect("login");
            return;
        }

        // Grab the hidden data from the form
        String propertyId = request.getParameter("propertyId");
        String propertyTitle = request.getParameter("propertyTitle");
        String agentName = request.getParameter("agentName");

        // ── SERVER-SIDE GUARD: block inquiries on sold properties ──
        if (propertyId != null && !propertyId.trim().isEmpty()) {
            String propPath = getServletContext().getRealPath("/WEB-INF/properties.txt");
            File propFile = new File(propPath);
            if (propFile.exists()) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(propFile), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.trim().isEmpty()) continue;
                        String[] data = line.split(",");
                        if (data.length >= 6 && data[0].trim().equals(propertyId.trim())) {
                            String propStatus = data[5].trim();
                            if ("sold".equalsIgnoreCase(propStatus)) {
                                // Property is sold — reject the inquiry silently and redirect
                                response.sendRedirect("index?error=sold_property");
                                return;
                            }
                            break;
                        }
                    }
                } catch (IOException e) {
                    // If we can't read the file, proceed cautiously (don't block)
                }
            }
        }
        // ── END SOLD GUARD ──

        // Grab the buyer-filled fields from the form
        String senderName = request.getParameter("senderName");
        String senderEmail = request.getParameter("senderEmail");
        String senderPhone = request.getParameter("senderPhone");
        String message = request.getParameter("message");

        // Generate today's date automatically
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String status = "Pending"; // New inquiries always start as Pending!

        // ===== New threaded inquiry storage (tab-separated + base64 message) =====
        // threads.tsv format:
        // threadId \t propertyId \t propertyTitle \t sellerName \t buyerAccount \t buyerName \t buyerEmail \t buyerPhone \t createdDate \t status
        String threadId = UUID.randomUUID().toString();
        String safePropertyId = (propertyId == null) ? "" : propertyId.replace("\t", " ").trim();
        String safePropertyTitle = (propertyTitle == null) ? "" : propertyTitle.replace("\t", " ").trim();
        String safeSellerName = (agentName == null) ? "" : agentName.replace("\t", " ").trim();
        String safeBuyerAccount = buyerName.replace("\t", " ").trim();
        String safeBuyerName = (senderName == null) ? "" : senderName.replace("\t", " ").trim();
        String safeBuyerEmail = (senderEmail == null) ? "" : senderEmail.replace("\t", " ").trim();
        String safeBuyerPhone = (senderPhone == null) ? "" : senderPhone.replace("\t", " ").trim();

        String threadRecord =
                threadId + "\t" +
                        safePropertyId + "\t" +
                        safePropertyTitle + "\t" +
                        safeSellerName + "\t" +
                        safeBuyerAccount + "\t" +
                        safeBuyerName + "\t" +
                        safeBuyerEmail + "\t" +
                        safeBuyerPhone + "\t" +
                        date + "\t" +
                        status;

        // messages.tsv format:
        // threadId \t timestamp \t senderRole \t senderName \t contentB64
        String msgPlain = (message == null) ? "" : message.replace("\r", " ").replace("\n", " ").trim();
        String msgB64 = Base64.getEncoder().encodeToString(msgPlain.getBytes("UTF-8"));
        String messageRecord =
                threadId + "\t" +
                        timestamp + "\t" +
                        "BUYER" + "\t" +
                        safeBuyerName + "\t" +
                        msgB64;

        String threadsPath = getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv");
        String messagesPath = getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv");

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(threadsPath, true), "UTF-8"))) {
            out.println(threadRecord);
        } catch (Exception e) {}

        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesPath, true), "UTF-8"))) {
            out.println(messageRecord);
        } catch (Exception e) {}

        // Send them straight to the dashboard to see their new inquiry!
        response.sendRedirect("buyerDashboard");
    }
}