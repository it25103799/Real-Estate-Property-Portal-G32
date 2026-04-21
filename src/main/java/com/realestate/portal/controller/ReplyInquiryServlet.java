package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@WebServlet("/replyInquiry")
public class ReplyInquiryServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String loggedUser = (String) session.getAttribute("loggedUser");
        String role = (String) session.getAttribute("loggedRole");

        if (loggedUser == null || role == null || (!"SELLER".equalsIgnoreCase(role) && !"BUYER".equalsIgnoreCase(role))) {
            response.sendRedirect("login");
            return;
        }

        String threadId = request.getParameter("threadId");
        String message = request.getParameter("message");

        if (threadId == null || threadId.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            response.sendRedirect("SELLER".equalsIgnoreCase(role) ? "sellerDashboard" : "buyerDashboard");
            return;
        }

        // Authorization: ensure the current user is part of this thread
        String threadsPath = getServletContext().getRealPath("/WEB-INF/inquiry_threads.tsv");
        File threadsFile = new File(threadsPath);
        boolean allowed = false;
        boolean isSellerSender = "SELLER".equalsIgnoreCase(role);
        String senderRole = isSellerSender ? "SELLER" : "BUYER";

        if (threadsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(threadsFile), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t", -1);
                    if (data.length >= 10 && threadId.equals(data[0])) {
                        // data[3] = sellerName, data[4] = buyerAccountName
                        if (isSellerSender && loggedUser.equals(data[3])) allowed = true;
                        if (!isSellerSender && loggedUser.equals(data[4])) allowed = true;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!allowed) {
            response.sendRedirect(isSellerSender ? "sellerDashboard" : "buyerDashboard");
            return;
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String msgPlain = message.replace("\r", " ").replace("\n", " ").trim();
        String msgB64 = Base64.getEncoder().encodeToString(msgPlain.getBytes("UTF-8"));

        // Append message
        String messagesPath = getServletContext().getRealPath("/WEB-INF/inquiry_messages.tsv");
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(messagesPath, true), "UTF-8"))) {
            out.println(threadId + "\t" + timestamp + "\t" + senderRole + "\t" + loggedUser.replace("\t", " ").trim() + "\t" + msgB64);
        } catch (Exception ignored) {}

        // Update status -> Responded (only when SELLER replies)
        if (isSellerSender && threadsFile.exists()) {
            File tmp = new File(threadsPath + ".tmp");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(threadsFile), "UTF-8"));
                 PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8"))) {

                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t", -1);
                    if (data.length >= 10 && threadId.equals(data[0])) {
                        data[9] = "Responded";
                        pw.println(String.join("\t", data));
                    } else {
                        pw.println(line);
                    }
                }
            } catch (Exception ignored) {}

            if (!tmp.renameTo(threadsFile)) {
                if (threadsFile.delete()) {
                    tmp.renameTo(threadsFile);
                }
            }
        }

        response.sendRedirect(isSellerSender ? "sellerDashboard" : "buyerDashboard");
    }
}

