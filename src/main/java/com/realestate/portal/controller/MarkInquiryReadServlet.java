package com.realestate.portal.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/markInquiryRead")
public class MarkInquiryReadServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        String user = (session != null) ? (String) session.getAttribute("loggedUser") : null;
        String role = (session != null) ? (String) session.getAttribute("loggedRole") : null;

        if (user == null || role == null) {
            response.setStatus(401);
            return;
        }

        String threadId = request.getParameter("threadId");
        if (threadId == null || threadId.trim().isEmpty()) {
            response.setStatus(400);
            return;
        }

        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String readsPath = getServletContext().getRealPath("/WEB-INF/inquiry_reads.tsv");
        File readsFile = new File(readsPath);
        File tmp = new File(readsPath + ".tmp");

        boolean updated = false;
        if (readsFile.exists()) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(readsFile), "UTF-8"));
                 PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmp), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] data = line.split("\t", -1);
                    if (data.length >= 3 && user.equals(data[0]) && threadId.equals(data[1])) {
                        pw.println(user + "\t" + threadId + "\t" + now);
                        updated = true;
                    } else {
                        pw.println(line);
                    }
                }
            } catch (Exception ignored) {}
        }

        if (!updated) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(tmp, readsFile.exists()), "UTF-8"))) {
                if (!readsFile.exists()) {
                    // if we couldn't read existing, start fresh
                }
                pw.println(user + "\t" + threadId + "\t" + now);
            } catch (Exception ignored) {}
        }

        if (!tmp.renameTo(readsFile)) {
            if (readsFile.exists()) readsFile.delete();
            tmp.renameTo(readsFile);
        }

        response.setStatus(204);
    }
}

