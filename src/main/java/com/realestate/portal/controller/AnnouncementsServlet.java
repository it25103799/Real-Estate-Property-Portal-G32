package com.realestate.portal.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * AnnouncementsServlet — Public read-only announcements board.
 * Renders full HTML directly — NO JSP dependency.
 * URL mapping: /announcements
 */
@WebServlet("/announcements")
public class AnnouncementsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String loggedUser = (String) session.getAttribute("loggedUser");
        String loggedRole = (String) session.getAttribute("loggedRole");
        if (loggedRole == null) loggedRole = "BUYER";

        String dashUrl;
        switch (loggedRole.toUpperCase()) {
            case "ADMIN":  dashUrl = request.getContextPath() + "/adminDashboard";  break;
            case "SELLER": dashUrl = request.getContextPath() + "/sellerDashboard"; break;
            default:       dashUrl = request.getContextPath() + "/buyerDashboard";  break;
        }
        String logoutUrl = request.getContextPath() + "/logout";

        List<String[]> announcements = loadAnnouncements(request);

        // Mark all current announcements as read for this user
        markAllRead(loggedUser, announcements, request);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        out.println("<title>Official Announcements - NESTIQ</title>");
        out.println("<link href=\"https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap\" rel=\"stylesheet\"/>");
        out.println("<style>");
        out.println(":root{--bg:#fff;--bg2:#f7f8fa;--ink:#0f1117;--line:#e8eaee;--accent:#1a56db;");
        out.println("--accent-l:rgba(26,86,219,.09);--green:#0d9e6e;--green-l:rgba(13,158,110,.1);");
        out.println("--amber:#d97706;--amber-l:rgba(217,119,6,.1);--red:#e02828;--red-l:rgba(224,40,40,.1);");
        out.println("--font-sans:'Outfit',sans-serif;--r:12px;}");
        out.println("[data-theme=dark]{--bg:#0f1117;--bg2:#1a1d27;--ink:#e8eaee;--line:#232736;");
        out.println("--accent-l:rgba(26,86,219,.18);--green-l:rgba(13,158,110,.18);");
        out.println("--amber-l:rgba(217,119,6,.18);--red-l:rgba(224,40,40,.18);}");
        out.println("*{box-sizing:border-box;}");
        out.println("body{font-family:var(--font-sans);background:var(--bg2);color:var(--ink);margin:0;min-height:100vh;transition:background .3s,color .3s;}");
        out.println(".navbar{background:var(--bg);border-bottom:1px solid var(--line);padding:0 40px;height:64px;display:flex;align-items:center;justify-content:space-between;position:sticky;top:0;z-index:100;box-shadow:0 2px 12px rgba(0,0,0,.05);}");
        out.println(".brand{font-size:1.4rem;font-weight:700;color:var(--accent);text-decoration:none;letter-spacing:-.5px;}");
        out.println(".nav-right{display:flex;align-items:center;gap:14px;}");
        out.println(".btn{background:var(--accent);color:#fff;padding:9px 18px;border:none;border-radius:7px;font-weight:600;cursor:pointer;font-family:var(--font-sans);font-size:.9rem;text-decoration:none;display:inline-block;transition:.2s;}");
        out.println(".btn:hover{opacity:.88;}");
        out.println(".btn-ghost{background:transparent;border:1.5px solid var(--line);color:var(--ink);padding:8px 16px;}");
        out.println(".btn-ghost:hover{border-color:var(--accent);color:var(--accent);}");
        out.println(".btn-red{background:var(--red);}");
        out.println(".theme-switch{width:50px;height:28px;background:var(--line);border-radius:30px;cursor:pointer;display:flex;align-items:center;padding:3px;transition:background .4s;border:none;}");
        out.println(".theme-thumb{width:22px;height:22px;background:#fff;border-radius:50%;box-shadow:0 2px 5px rgba(0,0,0,.2);display:flex;align-items:center;justify-content:center;font-size:.72rem;transition:transform .4s cubic-bezier(.34,1.56,.64,1);}");
        out.println("[data-theme=dark] .theme-switch{background:var(--accent);}");
        out.println("[data-theme=dark] .theme-thumb{transform:translateX(22px);background:var(--bg2);}");
        out.println(".hero{background:linear-gradient(135deg,#1a56db 0%,#1e3a8a 100%);padding:60px 40px 50px;text-align:center;color:#fff;}");
        out.println(".hero-badge{display:inline-flex;align-items:center;gap:6px;background:rgba(255,255,255,.18);border:1px solid rgba(255,255,255,.3);padding:5px 14px;border-radius:30px;font-size:.8rem;font-weight:600;letter-spacing:.5px;margin-bottom:18px;text-transform:uppercase;}");
        out.println(".hero h1{margin:0 0 10px;font-size:2.4rem;font-weight:700;letter-spacing:-.5px;}");
        out.println(".hero p{margin:0;opacity:.8;font-size:1.05rem;}");
        out.println(".page-wrap{max-width:860px;margin:0 auto;padding:48px 24px 80px;}");
        out.println(".filter-row{display:flex;align-items:center;gap:10px;margin-bottom:32px;flex-wrap:wrap;}");
        out.println(".filter-pill{padding:7px 18px;border-radius:20px;font-size:.85rem;font-weight:600;cursor:pointer;border:1.5px solid var(--line);background:var(--bg);color:var(--ink);transition:.2s;font-family:var(--font-sans);}");
        out.println(".filter-pill.active,.filter-pill:hover{background:var(--accent);color:#fff;border-color:var(--accent);}");
        out.println(".results-count{margin-left:auto;font-size:.82rem;opacity:.55;font-weight:500;}");
        out.println(".ann-card{background:var(--bg);border:1px solid var(--line);border-radius:var(--r);padding:28px 32px;margin-bottom:18px;box-shadow:0 2px 10px rgba(0,0,0,.04);transition:box-shadow .2s,transform .2s;border-left:5px solid transparent;}");
        out.println(".ann-card:hover{box-shadow:0 6px 24px rgba(0,0,0,.08);transform:translateY(-1px);}");
        out.println(".ann-card.HIGH{border-left-color:var(--red);}");
        out.println(".ann-card.MEDIUM{border-left-color:var(--amber);}");
        out.println(".ann-card.LOW{border-left-color:var(--green);}");
        out.println(".ann-meta{display:flex;align-items:center;gap:10px;margin-bottom:12px;flex-wrap:wrap;}");
        out.println(".badge{padding:3px 10px;border-radius:20px;font-size:.72rem;font-weight:700;letter-spacing:.5px;text-transform:uppercase;}");
        out.println(".badge-HIGH{background:var(--red-l);color:var(--red);}");
        out.println(".badge-MEDIUM{background:var(--amber-l);color:var(--amber);}");
        out.println(".badge-LOW{background:var(--green-l);color:var(--green);}");
        out.println(".ann-ts{font-size:.78rem;opacity:.5;font-weight:500;}");
        out.println(".ann-poster{font-size:.78rem;opacity:.55;}");
        out.println(".ann-title{font-size:1.15rem;font-weight:700;margin:0 0 10px;line-height:1.35;}");
        out.println(".ann-msg{font-size:.95rem;opacity:.78;line-height:1.65;margin:0;}");
        out.println(".section-label{font-size:.78rem;font-weight:700;text-transform:uppercase;letter-spacing:.8px;opacity:.45;margin-bottom:14px;}");
        out.println(".empty{text-align:center;padding:80px 20px;}");
        out.println(".empty-icon{font-size:3.5rem;margin-bottom:16px;}");
        out.println(".empty h3{margin:0 0 8px;font-size:1.2rem;}");
        out.println(".empty p{margin:0;opacity:.55;font-size:.95rem;}");
        out.println("@media(max-width:600px){.hero h1{font-size:1.7rem;}.ann-card{padding:20px 18px;}.navbar{padding:0 18px;}.page-wrap{padding:28px 14px 60px;}}");
        out.println("</style></head><body>");

        // Navbar
        out.println("<nav class=\"navbar\">");
        out.println("<a class=\"brand\" href=\"" + esc(dashUrl) + "\">&#127968; NESTIQ</a>");
        out.println("<div class=\"nav-right\">");
        out.println("<button class=\"theme-switch\" onclick=\"toggleTheme()\"><div class=\"theme-thumb\" id=\"theme-toggle\">&#127769;</div></button>");
        out.println("<a href=\"" + esc(dashUrl) + "\" class=\"btn btn-ghost\">&larr; My Dashboard</a>");
        out.println("<form action=\"" + esc(logoutUrl) + "\" method=\"post\" style=\"display:inline;\">");
        out.println("<button type=\"submit\" class=\"btn btn-red\">Logout</button></form>");
        out.println("</div></nav>");

        // Hero
        out.println("<div class=\"hero\">");
        out.println("<div class=\"hero-badge\">&#128226; Official Channel</div>");
        out.println("<h1>Administrative Announcements</h1>");
        out.println("<p>Important updates, notices, and news from the NESTIQ Platform Team</p>");
        out.println("</div>");

        // Content
        out.println("<div class=\"page-wrap\">");

        if (announcements.isEmpty()) {
            out.println("<div class=\"empty\"><div class=\"empty-icon\">&#128237;</div>");
            out.println("<h3>No Announcements Yet</h3>");
            out.println("<p>Check back later &mdash; the admin team will post important platform updates here.</p></div>");
        } else {
            out.println("<div class=\"filter-row\">");
            out.println("<button class=\"filter-pill active\" onclick=\"filterAnn(this,'ALL')\">All</button>");
            out.println("<button class=\"filter-pill\" onclick=\"filterAnn(this,'HIGH')\">&#128308; High Priority</button>");
            out.println("<button class=\"filter-pill\" onclick=\"filterAnn(this,'MEDIUM')\">&#128993; Medium</button>");
            out.println("<button class=\"filter-pill\" onclick=\"filterAnn(this,'LOW')\">&#128994; Low</button>");
            out.println("<span class=\"results-count\" id=\"ann-count\">" + announcements.size()
                    + " announcement" + (announcements.size() != 1 ? "s" : "") + "</span>");
            out.println("</div>");
            out.println("<div class=\"section-label\">Latest First</div>");
            out.println("<div id=\"ann-list\">");

            for (String[] ann : announcements) {
                String title    = ann.length > 1 ? esc(ann[1]) : "";
                String message  = ann.length > 2 ? esc(ann[2]) : "";
                String priority = ann.length > 3 ? esc(ann[3].trim()) : "MEDIUM";
                String poster   = ann.length > 4 ? esc(ann[4]) : "";
                String ts       = ann.length > 5 ? esc(ann[5]) : "";

                String priorityLabel;
                switch (priority) {
                    case "HIGH":  priorityLabel = "&#128308; High Priority"; break;
                    case "LOW":   priorityLabel = "&#128994; General";       break;
                    default:      priorityLabel = "&#128993; Important";     break;
                }

                out.println("<div class=\"ann-card " + priority + "\" data-priority=\"" + priority + "\">");
                out.println("<div class=\"ann-meta\">");
                out.println("<span class=\"badge badge-" + priority + "\">" + priorityLabel + "</span>");
                out.println("<span class=\"ann-ts\">&#128336; " + ts + "</span>");
                out.println("<span class=\"ann-poster\">&middot; &#128100; " + poster + "</span>");
                out.println("</div>");
                out.println("<h2 class=\"ann-title\">" + title + "</h2>");
                out.println("<p class=\"ann-msg\">" + message + "</p>");
                out.println("</div>");
            }
            out.println("</div>"); // #ann-list
        }
        out.println("</div>"); // .page-wrap

        // Self-contained scripts (no app.js dependency)
        out.println("<script>");
        out.println("(function(){if(localStorage.getItem('nestiq_theme')==='dark'){");
        out.println("document.documentElement.setAttribute('data-theme','dark');");
        out.println("var b=document.getElementById('theme-toggle');if(b)b.textContent='\u2600\uFE0F';}})();");
        out.println("function toggleTheme(){var h=document.documentElement,b=document.getElementById('theme-toggle');");
        out.println("if(h.getAttribute('data-theme')==='dark'){h.removeAttribute('data-theme');");
        out.println("localStorage.setItem('nestiq_theme','light');if(b)b.textContent='\uD83C\uDF19';}");
        out.println("else{h.setAttribute('data-theme','dark');localStorage.setItem('nestiq_theme','dark');if(b)b.textContent='\u2600\uFE0F';}}");
        out.println("function filterAnn(btn,priority){");
        out.println("document.querySelectorAll('.filter-pill').forEach(function(p){p.classList.remove('active');});");
        out.println("btn.classList.add('active');");
        out.println("var cards=document.querySelectorAll('#ann-list .ann-card'),visible=0;");
        out.println("cards.forEach(function(c){var m=priority==='ALL'||c.dataset.priority===priority;");
        out.println("c.style.display=m?'':'none';if(m)visible++;});");
        out.println("var e=document.getElementById('ann-count');");
        out.println("if(e)e.textContent=visible+' announcement'+(visible!==1?'s':'');}");
        out.println("</script>");
        out.println("</body></html>");
    }

    private List<String[]> loadAnnouncements(HttpServletRequest request) {
        List<String[]> list = new ArrayList<>();
        String filePath = getServletContext().getRealPath("/WEB-INF/announcements.txt");
        if (filePath == null) return list;
        File file = new File(filePath);
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", 6);
                if (parts.length == 6) list.add(0, parts); // newest first
            }
        } catch (IOException e) {
            System.err.println("AnnouncementsServlet error: " + e.getMessage());
        }
        return list;
    }


    // ── Mark all loaded announcements as read for this user ───────────
    private void markAllRead(String username, List<String[]> announcements, HttpServletRequest request) {
        if (announcements == null || announcements.isEmpty()) return;
        try {
            String path = getServletContext().getRealPath("/WEB-INF/announcement_reads.txt");
            if (path == null) return;
            File f = new File(path);

            // Load already-read IDs so we don't duplicate entries
            java.util.Set<String> already = new java.util.HashSet<>();
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(",", 2);
                        if (parts.length == 2 && parts[0].trim().equals(username)) {
                            already.add(parts[1].trim());
                        }
                    }
                }
            }

            // Append any new IDs
            try (java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(f, true))) {
                for (String[] ann : announcements) {
                    if (ann.length > 0) {
                        String id = ann[0].trim();
                        if (!already.contains(id)) {
                            bw.write(username + "," + id);
                            bw.newLine();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("AnnouncementsServlet: error writing announcement_reads: " + e.getMessage());
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}
