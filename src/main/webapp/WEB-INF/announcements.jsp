<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Official Announcements - NESTIQ</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <script src="app.js"></script>
    <style>
        :root {
            --bg: #ffffff; --bg2: #f7f8fa; --ink: #0f1117; --line: #e8eaee;
            --accent: #1a56db; --accent-l: rgba(26,86,219,0.09);
            --green: #0d9e6e; --green-l: rgba(13,158,110,0.10);
            --amber: #d97706; --amber-l: rgba(217,119,6,0.10);
            --red: #e02828; --red-l: rgba(224,40,40,0.10);
            --font-sans: 'Outfit', sans-serif; --r: 12px;
        }
        [data-theme="dark"] {
            --bg: #0f1117; --bg2: #1a1d27; --ink: #e8eaee; --line: #232736;
            --accent-l: rgba(26,86,219,0.18);
            --green-l: rgba(13,158,110,0.18);
            --amber-l: rgba(217,119,6,0.18);
            --red-l: rgba(224,40,40,0.18);
        }
        * { box-sizing: border-box; }
        body {
            font-family: var(--font-sans);
            background: var(--bg2);
            color: var(--ink);
            margin: 0;
            min-height: 100vh;
            transition: background 0.3s, color 0.3s;
        }

        /* ── NAVBAR ── */
        .navbar {
            background: var(--bg);
            border-bottom: 1px solid var(--line);
            padding: 0 40px;
            height: 64px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 100;
            box-shadow: 0 2px 12px rgba(0,0,0,0.05);
        }
        .navbar-brand {
            font-size: 1.4rem;
            font-weight: 700;
            color: var(--accent);
            text-decoration: none;
            letter-spacing: -0.5px;
        }
        .navbar-right { display: flex; align-items: center; gap: 14px; }
        .btn {
            background: var(--accent); color: white; padding: 9px 18px;
            border: none; border-radius: 7px; font-weight: 600;
            cursor: pointer; transition: 0.2s; font-family: var(--font-sans);
            font-size: 0.9rem; text-decoration: none; display: inline-block;
        }
        .btn:hover { opacity: 0.88; }
        .btn-ghost {
            background: transparent; border: 1.5px solid var(--line);
            color: var(--ink); padding: 8px 16px;
        }
        .btn-ghost:hover { border-color: var(--accent); color: var(--accent); }

        /* ── THEME TOGGLE ── */
        .theme-switch {
            position: relative; width: 50px; height: 28px;
            background: var(--line); border-radius: 30px; cursor: pointer;
            display: flex; align-items: center; padding: 3px;
            transition: background 0.4s; box-sizing: border-box;
        }
        .theme-switch-thumb {
            width: 22px; height: 22px; background: white; border-radius: 50%;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2); display: flex;
            align-items: center; justify-content: center; font-size: 0.72rem;
            transition: transform 0.4s cubic-bezier(0.34,1.56,0.64,1);
        }
        [data-theme="dark"] .theme-switch { background: var(--accent); }
        [data-theme="dark"] .theme-switch-thumb { transform: translateX(22px); background: var(--bg2); }

        /* ── HERO BANNER ── */
        .hero {
            background: linear-gradient(135deg, #1a56db 0%, #1e3a8a 100%);
            padding: 60px 40px 50px;
            text-align: center;
            color: white;
        }
        .hero-badge {
            display: inline-flex; align-items: center; gap: 6px;
            background: rgba(255,255,255,0.18); border: 1px solid rgba(255,255,255,0.3);
            padding: 5px 14px; border-radius: 30px;
            font-size: 0.8rem; font-weight: 600; letter-spacing: 0.5px;
            margin-bottom: 18px; text-transform: uppercase;
        }
        .hero h1 {
            margin: 0 0 10px; font-size: 2.4rem; font-weight: 700;
            letter-spacing: -0.5px;
        }
        .hero p { margin: 0; opacity: 0.8; font-size: 1.05rem; }

        /* ── MAIN LAYOUT ── */
        .page-wrap {
            max-width: 860px;
            margin: 0 auto;
            padding: 48px 24px 80px;
        }

        /* ── FILTER BAR ── */
        .filter-row {
            display: flex; align-items: center; gap: 10px;
            margin-bottom: 32px; flex-wrap: wrap;
        }
        .filter-pill {
            padding: 7px 18px; border-radius: 20px; font-size: 0.85rem;
            font-weight: 600; cursor: pointer; border: 1.5px solid var(--line);
            background: var(--bg); color: var(--ink); transition: 0.2s;
            font-family: var(--font-sans);
        }
        .filter-pill.active, .filter-pill:hover {
            background: var(--accent); color: white; border-color: var(--accent);
        }
        .results-count {
            margin-left: auto; font-size: 0.82rem;
            color: var(--ink); opacity: 0.55; font-weight: 500;
        }

        /* ── ANNOUNCEMENT CARD ── */
        .ann-card {
            background: var(--bg);
            border: 1px solid var(--line);
            border-radius: var(--r);
            padding: 28px 32px;
            margin-bottom: 18px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.04);
            transition: box-shadow 0.2s, transform 0.2s;
            border-left: 5px solid transparent;
        }
        .ann-card:hover {
            box-shadow: 0 6px 24px rgba(0,0,0,0.08);
            transform: translateY(-1px);
        }
        .ann-card.priority-HIGH  { border-left-color: var(--red); }
        .ann-card.priority-MEDIUM { border-left-color: var(--amber); }
        .ann-card.priority-LOW   { border-left-color: var(--green); }

        .ann-meta {
            display: flex; align-items: center; gap: 10px;
            margin-bottom: 12px; flex-wrap: wrap;
        }
        .priority-badge {
            padding: 3px 10px; border-radius: 20px;
            font-size: 0.72rem; font-weight: 700; letter-spacing: 0.5px;
            text-transform: uppercase;
        }
        .priority-HIGH   { background: var(--red-l);   color: var(--red);   }
        .priority-MEDIUM { background: var(--amber-l); color: var(--amber); }
        .priority-LOW    { background: var(--green-l); color: var(--green); }

        .ann-timestamp {
            font-size: 0.78rem; color: var(--ink); opacity: 0.5; font-weight: 500;
        }
        .ann-poster {
            font-size: 0.78rem; color: var(--ink); opacity: 0.55;
            display: flex; align-items: center; gap: 4px;
        }
        .ann-title {
            font-size: 1.15rem; font-weight: 700; color: var(--ink);
            margin: 0 0 10px; line-height: 1.35;
        }
        .ann-message {
            font-size: 0.95rem; color: var(--ink); opacity: 0.78;
            line-height: 1.65; margin: 0;
        }

        /* ── EMPTY STATE ── */
        .empty-state {
            text-align: center; padding: 80px 20px;
        }
        .empty-icon { font-size: 3.5rem; margin-bottom: 16px; }
        .empty-state h3 { margin: 0 0 8px; font-size: 1.2rem; }
        .empty-state p { margin: 0; opacity: 0.55; font-size: 0.95rem; }

        /* ── SECTION LABEL ── */
        .section-label {
            font-size: 0.78rem; font-weight: 700; text-transform: uppercase;
            letter-spacing: 0.8px; color: var(--ink); opacity: 0.45;
            margin-bottom: 14px;
        }

        @media (max-width: 600px) {
            .hero h1 { font-size: 1.7rem; }
            .ann-card { padding: 20px 18px; }
            .navbar { padding: 0 18px; }
            .page-wrap { padding: 28px 14px 60px; }
        }
    </style>
</head>
<body>

<!-- ── NAVBAR ── -->
<nav class="navbar">
    <a class="navbar-brand" href="#">🏠 NESTIQ</a>
    <div class="navbar-right">
        <div class="theme-switch" onclick="toggleTheme()" title="Toggle Dark Mode">
            <div class="theme-switch-thumb" id="theme-toggle">🌙</div>
        </div>

        <c:choose>
            <c:when test="${sessionScope.loggedRole == 'BUYER'}">
                <a href="buyerDashboard" class="btn btn-ghost">← My Dashboard</a>
            </c:when>
            <c:when test="${sessionScope.loggedRole == 'SELLER'}">
                <a href="sellerDashboard" class="btn btn-ghost">← My Dashboard</a>
            </c:when>
            <c:when test="${sessionScope.loggedRole == 'ADMIN'}">
                <a href="adminDashboard" class="btn btn-ghost">← Admin Panel</a>
            </c:when>
            <c:otherwise>
                <a href="login" class="btn btn-ghost">Login</a>
            </c:otherwise>
        </c:choose>

        <form action="logout" method="post" style="display:inline;">
            <button type="submit" class="btn" style="background: #e02828;">Logout</button>
        </form>
    </div>
</nav>

<!-- ── HERO BANNER ── -->
<div class="hero">
    <div class="hero-badge">📢 Official Channel</div>
    <h1>Administrative Announcements</h1>
    <p>Important updates, notices, and news from the NESTIQ Platform Team</p>
</div>

<!-- ── CONTENT ── -->
<div class="page-wrap">

    <c:choose>
        <c:when test="${not empty announcements}">

            <!-- Filter bar -->
            <div class="filter-row">
                <button class="filter-pill active" onclick="filterAnn(this, 'ALL')">All</button>
                <button class="filter-pill" onclick="filterAnn(this, 'HIGH')">🔴 High Priority</button>
                <button class="filter-pill" onclick="filterAnn(this, 'MEDIUM')">🟡 Medium</button>
                <button class="filter-pill" onclick="filterAnn(this, 'LOW')">🟢 Low</button>
                <span class="results-count" id="ann-count">${announcements.size()} announcement<c:if test="${announcements.size() != 1}">s</c:if></span>
            </div>

            <div class="section-label">Latest First</div>

            <!-- Cards -->
            <div id="ann-list">
                <c:forEach var="ann" items="${announcements}">
                    <%-- ann[0]=id, ann[1]=title, ann[2]=message, ann[3]=priority, ann[4]=postedBy, ann[5]=timestamp --%>
                    <div class="ann-card priority-${ann[3]}" data-priority="${ann[3]}">
                        <div class="ann-meta">
                            <span class="priority-badge priority-${ann[3]}">
                                <c:choose>
                                    <c:when test="${ann[3] == 'HIGH'}">🔴 High Priority</c:when>
                                    <c:when test="${ann[3] == 'MEDIUM'}">🟡 Important</c:when>
                                    <c:otherwise>🟢 General</c:otherwise>
                                </c:choose>
                            </span>
                            <span class="ann-timestamp">🕐 ${ann[5]}</span>
                            <span class="ann-poster">· 👤 ${ann[4]}</span>
                        </div>
                        <h2 class="ann-title">${ann[1]}</h2>
                        <p class="ann-message">${ann[2]}</p>
                    </div>
                </c:forEach>
            </div>

        </c:when>
        <c:otherwise>
            <div class="empty-state">
                <div class="empty-icon">📭</div>
                <h3>No Announcements Yet</h3>
                <p>Check back later — the admin team will post important platform updates here.</p>
            </div>
        </c:otherwise>
    </c:choose>

</div>

<script>
    // Priority filter
    function filterAnn(btn, priority) {
        document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
        btn.classList.add('active');

        const cards = document.querySelectorAll('#ann-list .ann-card');
        let visible = 0;
        cards.forEach(card => {
            const match = priority === 'ALL' || card.dataset.priority === priority;
            card.style.display = match ? '' : 'none';
            if (match) visible++;
        });

        const countEl = document.getElementById('ann-count');
        if (countEl) countEl.textContent = visible + ' announcement' + (visible !== 1 ? 's' : '');
    }

    // Set identity for shared app.js (dark mode, etc.)
    window.currentUser = "${sessionScope.loggedUser}";
    window.currentRole = "${sessionScope.loggedRole}";
</script>

</body>
</html>
