<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - NESTIQ</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&display=swap" rel="stylesheet"/>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>
    <style>
        :root {
            --bg: #ffffff; --bg2: #f7f8fa; --ink: #0f1117; --line: #e8eaee;
            --accent: #1a56db; --font-sans: 'Outfit', sans-serif; --r: 10px;
            --green: #0d9e6e; --red: #e02828; --amber: #d97706; --purple: #7c3aed;
        }
        [data-theme="dark"] {
            --bg: #0f1117; --bg2: #1a1d27; --ink: #ffffff; --line: #232736;
        }
        * { box-sizing: border-box; }
        body { font-family: var(--font-sans); background: var(--bg2); color: var(--ink); margin: 0; padding: 40px; transition: background 0.3s, color 0.3s; }
        .dashboard-container { max-width: 1400px; margin: 0 auto; }

        /* ── HEADER ── */
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; flex-wrap: wrap; gap: 16px; }
        .header h1 { margin: 0; font-size: 2.2rem; font-weight: 700; }
        .header-actions { display: flex; gap: 15px; align-items: center; flex-wrap: wrap; }

        /* ── NAV TABS ── */
        .nav-tabs { display: flex; gap: 4px; margin-bottom: 32px; background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); padding: 6px; width: fit-content; }
        .nav-tab { padding: 9px 20px; border-radius: 7px; border: none; background: transparent; color: var(--ink); opacity: 0.6; font-family: var(--font-sans); font-size: 0.9rem; font-weight: 600; cursor: pointer; transition: all 0.2s; white-space: nowrap; }
        .nav-tab:hover { opacity: 1; background: var(--bg2); }
        .nav-tab.active { background: var(--accent); color: white; opacity: 1; }

        /* ── PANELS ── */
        .panel { display: none; }
        .panel.active { display: block; }

        /* ── STAT CARDS ── */
        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 20px; margin-bottom: 40px; }
        .stat-card { background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); padding: 24px; box-shadow: 0 4px 16px rgba(0,0,0,.04); transition: all 0.3s; }
        .stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.08); }
        .stat-label { font-size: 0.82rem; color: var(--ink); opacity: 0.6; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }
        .stat-value { font-size: 2.4rem; font-weight: 700; color: var(--accent); line-height: 1; }
        .stat-sub { font-size: 0.8rem; color: var(--ink); opacity: 0.6; margin-top: 8px; }

        /* ── CARDS ── */
        .card { background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); padding: 30px; margin-bottom: 30px; box-shadow: 0 4px 16px rgba(0,0,0,.04); }
        .card-title { font-size: 1.15rem; font-weight: 600; margin-bottom: 20px; margin-top: 0; }
        .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }

        /* ── TABLE ── */
        table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }
        th, td { padding: 13px 14px; border-bottom: 1px solid var(--line); vertical-align: middle; }
        th { font-weight: 600; color: var(--accent); background: var(--bg2); }
        tr:hover td { background: var(--bg2); }
        .truncate { max-width: 220px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

        /* ── BUTTONS ── */
        .btn { background: var(--accent); color: white; padding: 10px 20px; border: none; border-radius: 6px; font-weight: 600; font-family: var(--font-sans); cursor: pointer; transition: 0.2s; font-size: 0.9rem; }
        .btn:hover { opacity: 0.88; }
        .btn-sm { padding: 5px 12px; font-size: 0.82rem; }
        .btn-danger { background: var(--red); }
        .btn-danger:hover { background: #c01f1f; }
        .btn-amber { background: var(--amber); }
        .btn-amber:hover { background: #b45309; }
        .btn-green { background: var(--green); }
        .btn-green:hover { background: #087a55; }
        .btn-purple { background: var(--purple); }
        .btn-purple:hover { background: #5b21b6; }
        .btn-ghost { background: none; border: 1.5px solid var(--line); color: var(--ink); padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; font-family: var(--font-sans); }
        .btn-ghost:hover { border-color: var(--ink); background: var(--bg2); }

        /* ── BADGES ── */
        .badge { display: inline-block; padding: 4px 12px; border-radius: 99px; font-size: 0.73rem; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; }
        .badge-buyer  { background: rgba(26, 86, 219, 0.1);  color: var(--accent); }
        .badge-seller { background: rgba(13, 158, 110, 0.1); color: var(--green);  }
        .badge-admin  { background: rgba(217, 119, 6, 0.1);  color: var(--amber);  }
        .badge-sale   { background: rgba(13, 158, 110, 0.1); color: var(--green);  }
        .badge-rent   { background: rgba(26, 86, 219, 0.1);  color: var(--accent); }
        .badge-verified { background: rgba(124, 58, 237, 0.1); color: var(--purple); }
        .badge-public   { background: rgba(15, 17, 23, 0.08);  color: var(--ink); opacity: 0.7; }

        /* ── STAR RATING ── */
        .stars { color: #f59e0b; letter-spacing: 1px; }

        /* ── SEARCH ── */
        .search-box { padding: 9px 14px; border: 1.5px solid var(--line); border-radius: var(--r); background: var(--bg); color: var(--ink); font-family: var(--font-sans); font-size: 0.9rem; min-width: 240px; }
        .search-box:focus { border-color: var(--accent); outline: none; }

        /* ── THEME SWITCH ── */
        .theme-switch { position: relative; width: 54px; height: 30px; background-color: var(--line); border-radius: 30px; cursor: pointer; display: flex; align-items: center; padding: 4px; transition: background-color 0.4s ease; }
        .theme-switch-thumb { width: 22px; height: 22px; background-color: white; border-radius: 50%; box-shadow: 0 2px 5px rgba(0,0,0,0.2); display: flex; align-items: center; justify-content: center; font-size: 0.75rem; transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }
        [data-theme="dark"] .theme-switch { background-color: var(--accent); }
        [data-theme="dark"] .theme-switch-thumb { transform: translateX(24px); background-color: var(--bg2); }

        /* ── FLASH ── */
        .flash-message { padding: 15px 20px; margin-bottom: 24px; border-radius: var(--r); font-weight: 600; border: 1px solid transparent; display: flex; justify-content: space-between; align-items: center; }
        .flash-success { background: rgba(13, 158, 110, 0.1); color: var(--green); border-color: var(--green); }
        .flash-error   { background: rgba(224, 40, 40, 0.1);  color: var(--red);   border-color: var(--red);   }

        /* ── ROLE CHANGE SELECT ── */
        .role-select { padding: 5px 10px; border: 1.5px solid var(--line); border-radius: 6px; background: var(--bg); color: var(--ink); font-family: var(--font-sans); font-size: 0.82rem; cursor: pointer; }

        /* ── CHARTS GRID ── */
        .charts-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px; margin-bottom: 30px; }
        .chart-card { background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); padding: 28px; box-shadow: 0 4px 16px rgba(0,0,0,.04); }
        .chart-card canvas { max-height: 260px; }

        /* ── ADMIN INFO ── */
        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; }
        .info-label { font-size: 0.82rem; color: var(--ink); opacity: 0.6; text-transform: uppercase; margin-bottom: 6px; }
        .info-value { font-size: 1.05rem; font-weight: 600; }

        /* ── EMPTY STATE ── */
        .empty-state { text-align: center; padding: 40px; color: var(--ink); opacity: 0.5; }

        /* ── REVIEW COMMENT ── */
        .review-comment { max-width: 280px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-style: italic; opacity: 0.8; }
    </style>
</head>
<body data-theme="light" id="body-theme">

<div class="dashboard-container">

    <!-- ═══════════════ HEADER ═══════════════ -->
    <div class="header">
        <div>
            <h1>👑 Admin Control Panel</h1>
            <p style="margin: 6px 0 0 0; opacity: 0.6;">Manage the entire NESTIQ platform</p>
        </div>
        <div class="header-actions">
            <div class="theme-switch" onclick="toggleTheme()" title="Toggle Dark Mode">
                <div class="theme-switch-thumb" id="theme-toggle">🌙</div>
            </div>
            <form action="logout" method="post" style="margin:0;">
                <button type="submit" class="btn" style="background: var(--red);">Logout</button>
            </form>
        </div>
    </div>

    <!-- ═══════════════ FLASH ═══════════════ -->
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="flash-message ${sessionScope.flashMessageType == 'success' ? 'flash-success' : 'flash-error'}">
            <span>${sessionScope.flashMessage}</span>
        </div>
        <c:remove var="flashMessage" scope="session"/>
        <c:remove var="flashMessageType" scope="session"/>
    </c:if>

    <!-- ═══════════════ STAT CARDS ═══════════════ -->
    <div class="stats-grid">
        <div class="stat-card" style="border-left: 4px solid var(--accent);">
            <div class="stat-label">Total Users</div>
            <div class="stat-value">${totalUsers}</div>
            <div class="stat-sub">Across all roles</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid #3b82f6;">
            <div class="stat-label">Buyers</div>
            <div class="stat-value">${totalBuyers}</div>
            <div class="stat-sub">Active buyers</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--green);">
            <div class="stat-label">Sellers</div>
            <div class="stat-value">${totalSellers}</div>
            <div class="stat-sub">Registered sellers</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--amber);">
            <div class="stat-label">Admins</div>
            <div class="stat-value">${totalAdmins}</div>
            <div class="stat-sub">System administrators</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--accent);">
            <div class="stat-label">Total Properties</div>
            <div class="stat-value">${totalProperties}</div>
            <div class="stat-sub">Listed on platform</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--green);">
            <div class="stat-label">Total Market Value</div>
            <div class="stat-value" style="font-size: 1.6rem;">$<fmt:formatNumber value="${totalPropertyValue}" pattern="#,##0"/></div>
            <div class="stat-sub">Combined listing value</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--purple);">
            <div class="stat-label">Total Reviews</div>
            <div class="stat-value">${totalReviews}</div>
            <div class="stat-sub">Platform-wide reviews</div>
        </div>
    </div>

    <!-- ═══════════════ NAV TABS ═══════════════ -->
    <div class="nav-tabs">
        <button class="nav-tab active" onclick="showPanel('users', this)">👥 Users</button>
        <button class="nav-tab" onclick="showPanel('properties', this)">🏘️ Properties</button>
        <button class="nav-tab" onclick="showPanel('reviews', this)">⭐ Reviews</button>
        <button class="nav-tab" onclick="showPanel('analytics', this)">📊 Analytics</button>
        <button class="nav-tab" onclick="showPanel('account', this)">👑 My Account</button>
    </div>

    <!-- ═══════════════ PANEL: USERS ═══════════════ -->
    <div class="panel active" id="panel-users">
        <div class="card">
            <div class="card-header">
                <div>
                    <h3 class="card-title" style="margin:0 0 4px 0;">👥 User Management</h3>
                    <p style="margin:0; opacity:0.6; font-size:0.9rem;">All registered users — search, change roles, or remove accounts</p>
                </div>
                <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                    <input type="text" id="user-search" class="search-box" placeholder="Search by name or email…" autocomplete="off"/>
                    <button class="btn btn-ghost btn-sm" onclick="exportTableToCSV('user-management-table','users.csv')">⬇ Export CSV</button>
                </div>
            </div>

            <table id="user-management-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Role</th>
                        <th>Change Role</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty allUsers}">
                            <c:forEach var="user" items="${allUsers}">
                                <tr>
                                    <td><strong>${user.username}</strong></td>
                                    <td>${user.email}</td>
                                    <td>${not empty user.phoneNumber ? user.phoneNumber : '—'}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${user.role eq 'BUYER'}"><span class="badge badge-buyer">🏠 Buyer</span></c:when>
                                            <c:when test="${user.role eq 'SELLER'}"><span class="badge badge-seller">💼 Seller</span></c:when>
                                            <c:when test="${user.role eq 'ADMIN'}"><span class="badge badge-admin">👑 Admin</span></c:when>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${user.role ne 'ADMIN'}">
                                                <form action="changeRole" method="post" style="display:inline-flex; gap:6px; align-items:center;">
                                                    <input type="hidden" name="userEmail" value="${user.email}"/>
                                                    <select name="newRole" class="role-select">
                                                        <option value="BUYER"  ${user.role eq 'BUYER'  ? 'selected' : ''}>Buyer</option>
                                                        <option value="SELLER" ${user.role eq 'SELLER' ? 'selected' : ''}>Seller</option>
                                                    </select>
                                                    <button type="submit" class="btn btn-amber btn-sm">Save</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span style="opacity:0.4; font-size:0.85rem;">Protected</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${user.role ne 'ADMIN'}">
                                                <form action="deleteUser" method="post" style="display:inline;" onsubmit="return confirm('Delete ${user.username}? This cannot be undone.');">
                                                    <input type="hidden" name="userEmail" value="${user.email}"/>
                                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise><span style="color:var(--amber); font-weight:600; font-size:0.85rem;">System User</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="6" class="empty-state">No users found</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <!-- ═══════════════ PANEL: PROPERTIES ═══════════════ -->
    <div class="panel" id="panel-properties">
        <div class="card">
            <div class="card-header">
                <div>
                    <h3 class="card-title" style="margin:0 0 4px 0;">🏘️ Property Management</h3>
                    <p style="margin:0; opacity:0.6; font-size:0.9rem;">All listings on the platform</p>
                </div>
                <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                    <input type="text" id="property-search" class="search-box" placeholder="Search by title or seller…" autocomplete="off"/>
                    <button class="btn btn-ghost btn-sm" onclick="exportTableToCSV('property-management-table','properties.csv')">⬇ Export CSV</button>
                </div>
            </div>

            <table id="property-management-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Title</th>
                        <th>Price</th>
                        <th>Location</th>
                        <th>Type</th>
                        <th>Status</th>
                        <th>Seller</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty allProperties}">
                            <c:forEach var="prop" items="${allProperties}">
                                <tr>
                                    <td><small style="opacity:0.5;">${prop.id}</small></td>
                                    <td><strong>${prop.title}</strong></td>
                                    <td>$<fmt:formatNumber value="${prop.price}" pattern="#,##0"/></td>
                                    <td>${prop.location}</td>
                                    <td>${prop.type}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${prop.status eq 'For Sale'}"><span class="badge badge-sale">For Sale</span></c:when>
                                            <c:otherwise><span class="badge badge-rent">For Rent</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>${prop.sellerName}</td>
                                    <td>
                                        <form action="deleteProperty" method="post" style="display:inline;" onsubmit="return confirm('Delete property &quot;${prop.title}&quot;?');">
                                            <input type="hidden" name="propertyId" value="${prop.id}"/>
                                            <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="8" class="empty-state">No properties found</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <!-- ═══════════════ PANEL: REVIEWS ═══════════════ -->
    <div class="panel" id="panel-reviews">
        <div class="card">
            <div class="card-header">
                <div>
                    <h3 class="card-title" style="margin:0 0 4px 0;">⭐ Review Management</h3>
                    <p style="margin:0; opacity:0.6; font-size:0.9rem;">All platform reviews — remove spam or inappropriate content</p>
                </div>
                <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
                    <input type="text" id="review-search" class="search-box" placeholder="Search by buyer or property ID…" autocomplete="off"/>
                    <button class="btn btn-ghost btn-sm" onclick="exportTableToCSV('review-management-table','reviews.csv')">⬇ Export CSV</button>
                </div>
            </div>

            <table id="review-management-table">
                <thead>
                    <tr>
                        <th>Review ID</th>
                        <th>Property ID</th>
                        <th>Buyer</th>
                        <th>Rating</th>
                        <th>Comment</th>
                        <th>Type</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:choose>
                        <c:when test="${not empty allReviews}">
                            <c:forEach var="rev" items="${allReviews}">
                                <tr>
                                    <td><small style="opacity:0.5;">${rev.reviewID}</small></td>
                                    <td><small>${rev.propertyID}</small></td>
                                    <td><strong>${rev.buyerName}</strong></td>
                                    <td>
                                        <span class="stars">
                                            <c:forEach begin="1" end="${rev.rating}">★</c:forEach><c:forEach begin="${rev.rating + 1}" end="5">☆</c:forEach>
                                        </span>
                                        <small style="opacity:0.6;"> ${rev.rating}/5</small>
                                    </td>
                                    <td class="review-comment" title="${rev.comment}">${rev.comment}</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${rev.verified}"><span class="badge badge-verified">✔ Verified</span></c:when>
                                            <c:otherwise><span class="badge badge-public">Public</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <form action="adminDeleteReview" method="post" style="display:inline;" onsubmit="return confirm('Remove this review by ${rev.buyerName}?');">
                                            <input type="hidden" name="reviewId" value="${rev.reviewID}"/>
                                            <button type="submit" class="btn btn-danger btn-sm">Remove</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr><td colspan="7" class="empty-state">No reviews found on the platform</td></tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </div>

    <!-- ═══════════════ PANEL: ANALYTICS ═══════════════ -->
    <div class="panel" id="panel-analytics">
        <div class="charts-grid">
            <div class="chart-card">
                <h3 class="card-title">👤 User Distribution</h3>
                <canvas id="userPieChart"></canvas>
            </div>
            <div class="chart-card">
                <h3 class="card-title">🏠 Properties by Type</h3>
                <canvas id="propertyTypeChart"></canvas>
            </div>
            <div class="chart-card">
                <h3 class="card-title">📋 Properties by Status</h3>
                <canvas id="propertyStatusChart"></canvas>
            </div>
            <div class="chart-card">
                <h3 class="card-title">⭐ Review Ratings Breakdown</h3>
                <canvas id="ratingsChart"></canvas>
            </div>
        </div>

        <!-- Platform Summary -->
        <div class="card" style="border-left: 4px solid var(--accent);">
            <h3 class="card-title">📈 Platform Summary</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px;">
                <div>
                    <div class="info-label">Avg. Property Price</div>
                    <div class="info-value" style="color: var(--accent);">
                        $<fmt:formatNumber value="${totalProperties > 0 ? totalPropertyValue / totalProperties : 0}" pattern="#,##0"/>
                    </div>
                </div>
                <div>
                    <div class="info-label">Buyer-to-Seller Ratio</div>
                    <div class="info-value" style="color: var(--green);">
                        ${totalSellers > 0 ? totalBuyers / totalSellers : totalBuyers} : 1
                    </div>
                </div>
                <div>
                    <div class="info-label">Listings per Seller</div>
                    <div class="info-value" style="color: var(--amber);">
                        <fmt:formatNumber value="${totalSellers > 0 ? totalProperties / totalSellers : 0}" pattern="#,##0.0"/>
                    </div>
                </div>
                <div>
                    <div class="info-label">Reviews per Property</div>
                    <div class="info-value" style="color: var(--purple);">
                        <fmt:formatNumber value="${totalProperties > 0 ? totalReviews / totalProperties : 0}" pattern="#,##0.0"/>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ═══════════════ PANEL: MY ACCOUNT ═══════════════ -->
    <div class="panel" id="panel-account">
        <div class="card" style="border-left: 4px solid var(--amber);">
            <h3 class="card-title">👑 Admin Information</h3>
            <div class="info-grid">
                <div>
                    <div class="info-label">Admin Name</div>
                    <div class="info-value">${sessionScope.loggedUser}</div>
                </div>
                <div>
                    <div class="info-label">Email Address</div>
                    <div class="info-value">${sessionScope.loggedEmail}</div>
                </div>
                <div>
                    <div class="info-label">Role</div>
                    <div class="info-value"><span class="badge badge-admin">👑 System Administrator</span></div>
                </div>
                <div>
                    <div class="info-label">Session Status</div>
                    <div class="info-value" style="color: var(--green);">● Active</div>
                </div>
            </div>
        </div>

        <div class="card">
            <h3 class="card-title">🔐 Admin Capabilities</h3>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 16px;">
                <div style="padding: 16px; background: var(--bg2); border-radius: var(--r); border: 1px solid var(--line);">
                    <div style="font-size: 1.5rem; margin-bottom: 8px;">👥</div>
                    <div style="font-weight: 600; margin-bottom: 4px;">User Management</div>
                    <div style="font-size: 0.85rem; opacity: 0.6;">View, search, change roles, and delete any non-admin user</div>
                </div>
                <div style="padding: 16px; background: var(--bg2); border-radius: var(--r); border: 1px solid var(--line);">
                    <div style="font-size: 1.5rem; margin-bottom: 8px;">🏘️</div>
                    <div style="font-weight: 600; margin-bottom: 4px;">Property Control</div>
                    <div style="font-size: 0.85rem; opacity: 0.6;">Remove any listing from the platform regardless of seller</div>
                </div>
                <div style="padding: 16px; background: var(--bg2); border-radius: var(--r); border: 1px solid var(--line);">
                    <div style="font-size: 1.5rem; margin-bottom: 8px;">⭐</div>
                    <div style="font-weight: 600; margin-bottom: 4px;">Review Moderation</div>
                    <div style="font-size: 0.85rem; opacity: 0.6;">Remove spam or inappropriate reviews platform-wide</div>
                </div>
                <div style="padding: 16px; background: var(--bg2); border-radius: var(--r); border: 1px solid var(--line);">
                    <div style="font-size: 1.5rem; margin-bottom: 8px;">📊</div>
                    <div style="font-weight: 600; margin-bottom: 4px;">Platform Analytics</div>
                    <div style="font-size: 0.85rem; opacity: 0.6;">Visual charts and live statistics across the entire platform</div>
                </div>
            </div>
        </div>
    </div>

</div><!-- /dashboard-container -->

<!-- ═══════════════ HIDDEN DATA FOR CHARTS (JSP → JS) ═══════════════ -->
<div id="chart-data"
     data-buyers="${totalBuyers}"
     data-sellers="${totalSellers}"
     data-admins="${totalAdmins}"
     data-total-reviews="${totalReviews}"
     style="display:none;">
    <%-- property type counts are computed in JS from table DOM --%>
</div>

<script>
/* ─── THEME ─── */
function toggleTheme() {
    const body = document.getElementById('body-theme');
    const isDark = body.getAttribute('data-theme') === 'dark';
    body.setAttribute('data-theme', isDark ? 'light' : 'dark');
    localStorage.setItem('theme', isDark ? 'light' : 'dark');
    document.getElementById('theme-toggle').innerHTML = isDark ? '🌙' : '☀️';
    rebuildCharts();
}

/* ─── TAB NAVIGATION ─── */
function showPanel(name, btn) {
    document.querySelectorAll('.panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
    document.getElementById('panel-' + name).classList.add('active');
    btn.classList.add('active');
    if (name === 'analytics') setTimeout(buildCharts, 80);
}

/* ─── LIVE SEARCH (users) ─── */
document.addEventListener('DOMContentLoaded', function () {
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.getElementById('body-theme').setAttribute('data-theme', savedTheme);
    document.getElementById('theme-toggle').innerHTML = savedTheme === 'dark' ? '☀️' : '🌙';

    document.getElementById('user-search').addEventListener('input', function () {
        filterTable('user-management-table', this.value, [0, 1]);
    });
    document.getElementById('property-search').addEventListener('input', function () {
        filterTable('property-management-table', this.value, [1, 6]);
    });
    document.getElementById('review-search').addEventListener('input', function () {
        filterTable('review-management-table', this.value, [1, 2]);
    });
});

function filterTable(tableId, term, cols) {
    const t = term.toLowerCase().trim();
    document.querySelectorAll('#' + tableId + ' tbody tr').forEach(row => {
        const match = cols.some(c => row.cells[c] && row.cells[c].textContent.toLowerCase().includes(t));
        row.style.display = (!t || match) ? '' : 'none';
    });
}

/* ─── EXPORT CSV ─── */
function exportTableToCSV(tableId, filename) {
    const rows = document.querySelectorAll('#' + tableId + ' tr');
    const csv = [];
    rows.forEach(row => {
        const cols = [];
        row.querySelectorAll('th, td').forEach(cell => {
            // skip the last column (action buttons)
            let text = cell.textContent.trim().replace(/\s+/g, ' ');
            cols.push('"' + text.replace(/"/g, '""') + '"');
        });
        csv.push(cols.slice(0, -1).join(',')); // drop Actions column
    });
    const blob = new Blob([csv.join('\n')], { type: 'text/csv' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = filename;
    a.click();
}

/* ─── CHARTS ─── */
let chartInstances = {};

function rebuildCharts() {
    Object.values(chartInstances).forEach(c => c.destroy());
    chartInstances = {};
    buildCharts();
}

function buildCharts() {
    const isDark = document.getElementById('body-theme').getAttribute('data-theme') === 'dark';
    const textColor = isDark ? '#ffffff' : '#0f1117';
    const gridColor = isDark ? '#232736' : '#e8eaee';

    const d = document.getElementById('chart-data');
    const buyers  = parseInt(d.dataset.buyers)  || 0;
    const sellers = parseInt(d.dataset.sellers) || 0;
    const admins  = parseInt(d.dataset.admins)  || 0;

    // ── Count property types from table ──
    const typeCounts = {};
    const statusCounts = { 'For Sale': 0, 'For Rent': 0 };
    document.querySelectorAll('#property-management-table tbody tr').forEach(row => {
        if (row.cells.length < 7) return;
        const type   = row.cells[4].textContent.trim();
        const status = row.cells[5].textContent.trim();
        typeCounts[type] = (typeCounts[type] || 0) + 1;
        if (status.includes('Sale')) statusCounts['For Sale']++;
        else statusCounts['For Rent']++;
    });

    // ── Count ratings from review table ──
    const ratingCounts = { '1★': 0, '2★': 0, '3★': 0, '4★': 0, '5★': 0 };
    document.querySelectorAll('#review-management-table tbody tr').forEach(row => {
        if (row.cells.length < 4) return;
        const ratingText = row.cells[3].textContent.trim();
        const match = ratingText.match(/(\d)\/5/);
        if (match) ratingCounts[match[1] + '★']++;
    });

    const defaults = { responsive: true, maintainAspectRatio: true };

    // 1. User Pie
    if (!chartInstances.userPie) {
        chartInstances.userPie = new Chart(document.getElementById('userPieChart'), {
            type: 'doughnut',
            data: {
                labels: ['Buyers', 'Sellers', 'Admins'],
                datasets: [{ data: [buyers, sellers, admins], backgroundColor: ['#1a56db', '#0d9e6e', '#d97706'], borderWidth: 0 }]
            },
            options: { ...defaults, plugins: { legend: { labels: { color: textColor, font: { family: 'Outfit', size: 13 } } } } }
        });
    }

    // 2. Property Type Bar
    if (!chartInstances.propType) {
        chartInstances.propType = new Chart(document.getElementById('propertyTypeChart'), {
            type: 'bar',
            data: {
                labels: Object.keys(typeCounts),
                datasets: [{ label: 'Properties', data: Object.values(typeCounts), backgroundColor: '#1a56db', borderRadius: 6 }]
            },
            options: {
                ...defaults,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { color: textColor, font: { family: 'Outfit' } }, grid: { color: gridColor } },
                    y: { ticks: { color: textColor, font: { family: 'Outfit' }, stepSize: 1 }, grid: { color: gridColor } }
                }
            }
        });
    }

    // 3. Status Pie
    if (!chartInstances.status) {
        chartInstances.status = new Chart(document.getElementById('propertyStatusChart'), {
            type: 'doughnut',
            data: {
                labels: Object.keys(statusCounts),
                datasets: [{ data: Object.values(statusCounts), backgroundColor: ['#0d9e6e', '#1a56db'], borderWidth: 0 }]
            },
            options: { ...defaults, plugins: { legend: { labels: { color: textColor, font: { family: 'Outfit', size: 13 } } } } }
        });
    }

    // 4. Ratings Bar
    if (!chartInstances.ratings) {
        chartInstances.ratings = new Chart(document.getElementById('ratingsChart'), {
            type: 'bar',
            data: {
                labels: Object.keys(ratingCounts),
                datasets: [{ label: 'Reviews', data: Object.values(ratingCounts), backgroundColor: '#f59e0b', borderRadius: 6 }]
            },
            options: {
                ...defaults,
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { color: textColor, font: { family: 'Outfit' } }, grid: { color: gridColor } },
                    y: { ticks: { color: textColor, font: { family: 'Outfit' }, stepSize: 1 }, grid: { color: gridColor } }
                }
            }
        });
    }
}
</script>

</body>
</html>
