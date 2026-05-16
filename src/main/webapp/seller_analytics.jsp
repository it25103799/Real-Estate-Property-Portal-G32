<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Property Analytics - NESTIQ</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <style>
        :root {
            --bg: #ffffff; --bg2: #f7f8fa; --ink: #0f1117; --line: #e8eaee;
            --accent: #1a56db; --font-sans: 'Outfit', sans-serif; --r: 10px;
            --green: #0d9e6e; --red: #e02828; --amber: #f59e0b;
        }
        [data-theme="dark"] {
            --bg: #111827; --bg2: #1f2937; --ink: #f9fafb; --line: #374151;
            --accent: #3b82f6; --green: #10b981; --red: #ef4444; --amber: #fbbf24;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: var(--font-sans); background: var(--bg2); color: var(--ink);
               padding: 30px; transition: background 0.3s, color 0.3s; }

        .page-wrapper { max-width: 1400px; margin: 0 auto; }

        /* ── Header ── */
        .header { display: flex; justify-content: space-between; align-items: center;
                  margin-bottom: 30px; }
        .header h1 { font-size: 1.6rem; font-weight: 700; }
        .header h1 span { color: var(--accent); }
        .btn { padding: 9px 18px; border-radius: 8px; border: 1px solid var(--line);
               background: var(--accent); color: #fff; font-family: var(--font-sans);
               font-size: 0.85rem; font-weight: 600; cursor: pointer;
               transition: opacity 0.2s; }
        .btn:hover { opacity: 0.85; }
        .btn-outline { background: var(--bg); color: var(--ink); }

        /* ── Summary cards ── */
        .summary-grid { display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px; margin-bottom: 30px; }
        .sum-card { background: var(--bg); border: 1px solid var(--line);
                    border-radius: var(--r); padding: 20px;
                    box-shadow: 0 4px 16px rgba(0,0,0,.04);
                    position: relative; overflow: hidden; }
        .sum-card::before { content: ''; position: absolute; top: 0; left: 0;
                             width: 4px; height: 100%; background: var(--accent); }
        .sum-card.views::before  { background: var(--accent); }
        .sum-card.inq::before    { background: var(--amber); }
        .sum-card.book::before   { background: var(--green); }
        .sum-card.fav::before    { background: var(--red); }
        .sum-icon { font-size: 1.6rem; margin-bottom: 10px; }
        .sum-value { font-size: 2rem; font-weight: 700; line-height: 1; margin-bottom: 4px; }
        .sum-label { font-size: 0.78rem; text-transform: uppercase; letter-spacing: 0.5px;
                     opacity: 0.6; font-weight: 500; }

        /* ── Table card ── */
        .card { background: var(--bg); border: 1px solid var(--line);
                border-radius: var(--r); padding: 24px;
                box-shadow: 0 4px 16px rgba(0,0,0,.04); }
        .card-title { font-size: 1.1rem; font-weight: 700; margin-bottom: 20px;
                      padding-bottom: 14px; border-bottom: 2px solid var(--line); }

        .table-wrap { overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
        thead th { background: var(--bg2); padding: 12px 14px; text-align: left;
                   font-weight: 600; font-size: 0.78rem; text-transform: uppercase;
                   letter-spacing: 0.4px; opacity: 0.7; border-bottom: 1px solid var(--line); }
        tbody tr { border-bottom: 1px solid var(--line); transition: background 0.15s; }
        tbody tr:hover { background: var(--bg2); }
        tbody td { padding: 14px 14px; vertical-align: middle; }

        /* ── Metric pills ── */
        .pill { display: inline-flex; align-items: center; gap: 5px;
                padding: 4px 12px; border-radius: 20px; font-weight: 700;
                font-size: 0.85rem; }
        .pill.views  { background: rgba(26,86,219,0.10); color: var(--accent); }
        .pill.inq    { background: rgba(245,158,11,0.12); color: var(--amber); }
        .pill.book   { background: rgba(13,158,110,0.12); color: var(--green); }
        .pill.fav    { background: rgba(224,40,40,0.10); color: var(--red); }

        /* ── Mini bar ── */
        .bar-wrap { display: flex; align-items: center; gap: 8px; min-width: 100px; }
        .bar-bg   { flex: 1; height: 6px; background: var(--line); border-radius: 3px; overflow: hidden; }
        .bar-fill { height: 100%; border-radius: 3px; transition: width 0.5s ease; }

        /* ── Status badge ── */
        .badge { display: inline-block; padding: 3px 10px; border-radius: 20px;
                 font-size: 0.78rem; font-weight: 700; }
        .badge-sale { background: rgba(26,86,219,0.10); color: var(--accent);
                      border: 1px solid rgba(26,86,219,0.25); }
        .badge-sold { background: rgba(13,158,110,0.12); color: var(--green);
                      border: 1px solid rgba(13,158,110,0.25); }
        .badge-rent { background: rgba(245,158,11,0.12); color: var(--amber);
                      border: 1px solid rgba(245,158,11,0.25); }

        /* ── Empty state ── */
        .empty { text-align: center; padding: 60px 20px; opacity: 0.55; }
        .empty-icon { font-size: 3rem; margin-bottom: 12px; }

        /* ── Dark mode toggle ── */
        .theme-btn { background: var(--line); color: var(--ink); border: 1px solid var(--line);
                     padding: 7px 14px; border-radius: 8px; cursor: pointer;
                     font-family: var(--font-sans); font-size: 0.85rem; }
    </style>
</head>
<body>
<div class="page-wrapper">

    <!-- Header -->
    <div class="header">
        <h1>📊 Property <span>Analytics</span></h1>
        <div style="display:flex; gap:10px; align-items:center;">
            <button class="theme-btn" onclick="toggleTheme()">🌙 Theme</button>
            <button class="btn btn-outline" onclick="window.location.href='sellerDashboard'">← Back to Dashboard</button>
        </div>
    </div>

    <!-- Summary Cards -->
    <div class="summary-grid">
        <div class="sum-card views">
            <div class="sum-icon">👁️</div>
            <div class="sum-value">${totalViews}</div>
            <div class="sum-label">Total Property Views</div>
        </div>
        <div class="sum-card inq">
            <div class="sum-icon">📩</div>
            <div class="sum-value">${totalInquiries}</div>
            <div class="sum-label">Total Inquiries Received</div>
        </div>
        <div class="sum-card book">
            <div class="sum-icon">📋</div>
            <div class="sum-value">${totalBookings}</div>
            <div class="sum-label">Total Bookings Made</div>
        </div>
        <div class="sum-card fav">
            <div class="sum-icon">❤️</div>
            <div class="sum-value">${totalFavorites}</div>
            <div class="sum-label">Total Times Saved</div>
        </div>
    </div>

    <!-- Per-Property Table -->
    <div class="card">
        <div class="card-title">📋 Per-Property Performance Breakdown</div>

        <c:choose>
            <c:when test="${empty analyticsRows}">
                <div class="empty">
                    <div class="empty-icon">🏠</div>
                    <div style="font-weight:600; margin-bottom:6px;">No properties listed yet.</div>
                    <div style="font-size:0.9rem;">Add your first property from the dashboard to start tracking.</div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Property</th>
                            <th>Status</th>
                            <th>Price</th>
                            <th>👁️ Views</th>
                            <th>📩 Inquiries</th>
                            <th>📋 Bookings</th>
                            <th>❤️ Saved</th>
                            <th>Engagement Score</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:set var="maxViews" value="${totalViews > 0 ? totalViews : 1}" />
                    <c:forEach var="row" items="${analyticsRows}" varStatus="loop">
                        <tr>
                            <td><small style="opacity:0.5; font-weight:600;">${loop.index + 1}</small></td>
                            <td>
                                <div style="font-weight:600;">${row.title}</div>
                                <div style="font-size:0.78rem; opacity:0.6; margin-top:2px;">
                                    📍 ${row.location} &nbsp;·&nbsp; ${row.type}
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${row.status == 'Sold' || row.status == 'sold'}">
                                        <span class="badge badge-sold">✅ SOLD</span>
                                    </c:when>
                                    <c:when test="${row.status == 'For Rent' || row.status == 'for rent'}">
                                        <span class="badge badge-rent">🔑 FOR RENT</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-sale">🏷️ FOR SALE</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <strong style="color:var(--accent);">
                                    LKR <fmt:formatNumber value="${row.price}" pattern="#,##0"/>
                                </strong>
                            </td>
                            <td>
                                <span class="pill views">👁️ ${row.viewCount}</span>
                            </td>
                            <td>
                                <span class="pill inq">📩 ${row.inquiryCount}</span>
                            </td>
                            <td>
                                <span class="pill book">📋 ${row.bookingCount}</span>
                            </td>
                            <td>
                                <span class="pill fav">❤️ ${row.favoriteCount}</span>
                            </td>
                            <td>
                                <%-- Engagement score = weighted sum: views*1 + inquiries*3 + bookings*5 + favs*2 --%>
                                <c:set var="score" value="${row.viewCount * 1 + row.inquiryCount * 3 + row.bookingCount * 5 + row.favoriteCount * 2}" />
                                <c:set var="maxScore" value="${totalViews * 1 + totalInquiries * 3 + totalBookings * 5 + totalFavorites * 2}" />
                                <c:set var="maxScore" value="${maxScore > 0 ? maxScore : 1}" />
                                <c:set var="pct" value="${score * 100 / maxScore}" />
                                <div class="bar-wrap">
                                    <div class="bar-bg">
                                        <div class="bar-fill"
                                             style="width:${pct}%; background: ${pct > 60 ? 'var(--green)' : pct > 30 ? 'var(--amber)' : 'var(--accent)'};">
                                        </div>
                                    </div>
                                    <span style="font-size:0.78rem; font-weight:700; min-width:28px;">${score}</span>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                </div>

                <!-- Legend -->
                <div style="margin-top:18px; padding-top:14px; border-top:1px solid var(--line);
                            font-size:0.78rem; opacity:0.55; display:flex; gap:20px; flex-wrap:wrap;">
                    <span>Engagement Score formula: Views × 1 + Inquiries × 3 + Bookings × 5 + Saves × 2</span>
                    <span>Table sorted by views (highest first)</span>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

</div>

<script>
    // Dark mode toggle (same as rest of app)
    function toggleTheme() {
        const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
        document.documentElement.setAttribute('data-theme', isDark ? 'light' : 'dark');
        localStorage.setItem('theme', isDark ? 'light' : 'dark');
    }
    // Apply saved theme on load
    (function() {
        const saved = localStorage.getItem('theme');
        if (saved) document.documentElement.setAttribute('data-theme', saved);
    })();
</script>
</body>
</html>
