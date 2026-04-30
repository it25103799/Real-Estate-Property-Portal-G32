<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard - NESTIQ</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600&display=swap" rel="stylesheet"/>
    <style>
        :root {
            --bg: #ffffff; --bg2: #f7f8fa; --ink: #0f1117; --line: #e8eaee;
            --accent: #1a56db; --font-sans: 'Outfit', sans-serif; --r: 10px;
            --green: #0d9e6e; --red: #e02828; --amber: #d97706;
        }
        [data-theme="dark"] {
            --bg: #0f1117; --bg2: #1a1d27; --ink: #ffffff; --line: #232736;
        }
        body { font-family: var(--font-sans); background: var(--bg2); color: var(--ink); margin: 0; padding: 40px; transition: background 0.3s, color 0.3s; }
        .dashboard-container { max-width: 1400px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 40px; }
        .header h1 { margin: 0; font-size: 2.2rem; font-weight: 700; }
        .header-actions { display: flex; gap: 15px; align-items: center; }

        .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 20px; margin-bottom: 40px; }
        .stat-card {
            background: var(--bg); border: 1px solid var(--line); border-radius: var(--r);
            padding: 24px; box-shadow: 0 4px 16px rgba(0,0,0,.04);
            transition: all 0.3s;
        }
        .stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,.08); }
        .stat-label { font-size: 0.85rem; color: var(--ink); opacity: 0.6; text-transform: uppercase; margin-bottom: 8px; }
        .stat-value { font-size: 2.4rem; font-weight: 700; color: var(--accent); }
        .stat-sub { font-size: 0.8rem; color: var(--ink); opacity: 0.6; margin-top: 8px; }

        .card { background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); padding: 30px; margin-bottom: 30px; box-shadow: 0 4px 16px rgba(0,0,0,.04); }
        .card-title { font-size: 1.2rem; font-weight: 600; margin-bottom: 20px; margin-top: 0; }

        table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }
        th, td { padding: 14px; border-bottom: 1px solid var(--line); }
        th { font-weight: 600; color: var(--accent); background: var(--bg2); }
        tr:hover { background: var(--bg2); }

        .btn { background: var(--accent); color: white; padding: 10px 20px; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.2s; }
        .btn:hover { opacity: 0.9; }
        .btn-danger { background: var(--red); }
        .btn-danger:hover { background: #c01f1f; }
        .btn-ghost { background: none; border: 1.5px solid var(--line); color: var(--ink); padding: 8px 16px; border-radius: 6px; cursor: pointer; font-weight: 600; }
        .btn-ghost:hover { border-color: var(--ink); background: var(--bg2); }

        .badge {
            display: inline-block; padding: 4px 12px; border-radius: 99px; font-size: 0.75rem; font-weight: 600;
            text-transform: uppercase; letter-spacing: 0.5px;
        }
        .badge-buyer { background: rgba(26, 86, 219, 0.1); color: var(--accent); }
        .badge-seller { background: rgba(13, 158, 110, 0.1); color: var(--green); }
        .badge-admin { background: rgba(217, 119, 6, 0.1); color: var(--amber); }
        .badge-sale { background: rgba(13, 158, 110, 0.1); color: var(--green); }
        .badge-rent { background: rgba(26, 86, 219, 0.1); color: var(--accent); }

        .theme-switch {
            position: relative; width: 54px; height: 30px; background-color: var(--line);
            border-radius: 30px; cursor: pointer; display: flex; align-items: center;
            padding: 4px; transition: background-color 0.4s ease; box-sizing: border-box;
        }
        .theme-switch-thumb {
            width: 22px; height: 22px; background-color: white; border-radius: 50%;
            box-shadow: 0 2px 5px rgba(0,0,0,0.2); display: flex; align-items: center;
            justify-content: center; font-size: 0.75rem;
            transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        [data-theme="dark"] .theme-switch { background-color: var(--accent); }
        [data-theme="dark"] .theme-switch-thumb { transform: translateX(24px); background-color: var(--bg2); }

        .search-box { width: 100%; max-width: 400px; padding: 10px 14px; border: 1.5px solid var(--line); border-radius: var(--r); background: var(--bg); color: var(--ink); font-family: var(--font-sans); }
        .search-box:focus { border-color: var(--accent); outline: none; }

        /* Flash Message Styling */
        .flash-message {
            padding: 15px 20px;
            margin-bottom: 20px;
            border-radius: var(--r);
            font-weight: 600;
            border: 1px solid transparent;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .flash-success {
            background-color: rgba(13, 158, 110, 0.1);
            color: var(--green);
            border-color: var(--green);
        }
        .flash-error {
            background-color: rgba(224, 40, 40, 0.1);
            color: var(--red);
            border-color: var(--red);
        }
    </style>
</head>
<body data-theme="light" id="body-theme">

<div class="dashboard-container">
    <!-- HEADER -->
    <div class="header">
        <div>
            <h1>👑 Admin Control Panel</h1>
            <p style="margin: 8px 0 0 0; color: var(--ink); opacity: 0.6;">Manage the entire NESTIQ system</p>
        </div>
        <div class="header-actions">
            <div style="display: flex; flex-direction: column; align-items: flex-start; margin-right: 18px;">
                <label for="user-search" style="font-size: 0.95rem; color: var(--ink); opacity: 0.8; margin-bottom: 4px;">Search Buyers & Sellers</label>
                <input type="text" id="user-search" class="search-box" placeholder="you@example.com / Username" style="margin-bottom: 0;" autocomplete="off" />
            </div>
            <div class="theme-switch" onclick="toggleTheme()" title="Toggle Dark Mode">
                <div class="theme-switch-thumb" id="theme-toggle">🌙</div>
            </div>
            <form action="logout" method="post" style="margin: 0;">
                <button type="submit" class="btn" style="background: var(--red);">Logout</button>
            </form>
        </div>
    </div>

    <!-- FLASH MESSAGE -->
    <c:if test="${not empty sessionScope.flashMessage}">
        <div class="flash-message ${sessionScope.flashMessageType == 'success' ? 'flash-success' : 'flash-error'}">
            <span>${sessionScope.flashMessage}</span>
        </div>
        <c:remove var="flashMessage" scope="session"/>
        <c:remove var="flashMessageType" scope="session"/>
    </c:if>

    <!-- STATISTICS -->
    <div class="stats-grid">
        <div class="stat-card" style="border-left: 4px solid var(--accent);">
            <div class="stat-label">Total Users</div>
            <div class="stat-value">${totalUsers}</div>
            <div class="stat-sub">Across all roles</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--accent);">
            <div class="stat-label">Buyers</div>
            <div class="stat-value">${totalBuyers}</div>
            <div class="stat-sub">Active buyers in system</div>
        </div>
        <div class="stat-card" style="border-left: 4px solid var(--green);">
            <div class="stat-label">Sellers</div>
            <div class="stat-value">${totalSellers}</div>
            <div class="stat-sub">Registered property sellers</div>
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
            <div class="stat-label">Total Property Value</div>
            <div class="stat-value">$<fmt:formatNumber value="${totalPropertyValue}" pattern="#,##0" /></div>
            <div class="stat-sub">Combined market value</div>
        </div>
    </div>

    <!-- USER MANAGEMENT -->
    <div class="card">
        <h3 class="card-title">👥 User Management</h3>
        <p style="color: var(--ink); opacity: 0.75; margin-bottom: 20px;">All registered users in the system</p>

        <table id="user-management-table">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Role</th>
                    <th>Action</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty allUsers}">
                        <c:forEach var="user" items="${allUsers}">
                            <tr>
                                <td><strong>${user.username}</strong></td>
                                <td>${user.password}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${user.role eq 'BUYER'}">
                                            <span class="badge badge-buyer">🏠 Buyer</span>
                                        </c:when>
                                        <c:when test="${user.role eq 'SELLER'}">
                                            <span class="badge badge-seller">💼 Seller</span>
                                        </c:when>
                                        <c:when test="${user.role eq 'ADMIN'}">
                                            <span class="badge badge-admin">👑 Admin</span>
                                        </c:when>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${user.role ne 'ADMIN'}">
                                        <form action="deleteUser" method="post" style="display: inline;" onsubmit="return confirm('Are you sure you want to delete this user?');">
                                            <input type="hidden" name="userEmail" value="${user.password}">
                                            <button type="submit" class="btn btn-danger" style="padding: 6px 12px; font-size: 0.85rem;">Delete</button>
                                        </form>
                                    </c:if>
                                    <c:if test="${user.role eq 'ADMIN'}">
                                        <span style="color: var(--amber); font-weight: 600;">System User</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="4" style="text-align:center; color: var(--ink); opacity: 0.6;">No users found</td></tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- PROPERTY MANAGEMENT -->
    <div class="card">
        <h3 class="card-title">🏘️ Property Management</h3>
        <p style="color: var(--ink); opacity: 0.75; margin-bottom: 20px;">All properties listed on the platform</p>

        <table>
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
                                <td><small>${prop.id}</small></td>
                                <td><strong>${prop.title}</strong></td>
                                <td>$<fmt:formatNumber value="${prop.price}" pattern="#,##0.00" /></td>
                                <td>${prop.location}</td>
                                <td>${prop.type}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${prop.status eq 'For Sale'}">
                                            <span class="badge badge-sale">For Sale</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge-rent">For Rent</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${prop.sellerName}</td>
                                <td>
                                    <form action="deleteProperty" method="post" style="display: inline;" onsubmit="return confirm('Are you sure you want to delete this property?');">
                                        <input type="hidden" name="propertyId" value="${prop.id}">
                                        <button type="submit" class="btn btn-danger" style="padding: 6px 12px; font-size: 0.85rem;">Delete</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="8" style="text-align:center; color: var(--ink); opacity: 0.6;">No properties found</td></tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <!-- ADMIN INFO PANEL -->
    <div class="card" style="background: rgba(255, 193, 7, 0.05); border-left: 4px solid var(--amber);">
        <h3 class="card-title">ℹ️ Admin Information</h3>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px;">
            <div>
                <div style="font-size: 0.85rem; color: var(--ink); opacity: 0.6; text-transform: uppercase; margin-bottom: 8px;">Admin Name</div>
                <div style="font-size: 1.1rem; font-weight: 600;">${sessionScope.loggedUser}</div>
            </div>
            <div>
                <div style="font-size: 0.85rem; color: var(--ink); opacity: 0.6; text-transform: uppercase; margin-bottom: 8px;">Email Address</div>
                <div style="font-size: 1rem;">${sessionScope.loggedEmail}</div>
            </div>
            <div>
                <div style="font-size: 0.85rem; color: var(--ink); opacity: 0.6; text-transform: uppercase; margin-bottom: 8px;">Role</div>
                <div style="font-size: 1rem;">
                    <span class="badge badge-admin">👑 System Administrator</span>
                </div>
            </div>
        </div>
    </div>

</div>

<script>
    function toggleTheme() {
        const html = document.getElementById('body-theme');
        const isDark = html.getAttribute('data-theme') === 'dark';
        html.setAttribute('data-theme', isDark ? 'light' : 'dark');
        localStorage.setItem('theme', isDark ? 'light' : 'dark');
    }

    // Load theme preference on page load
    document.addEventListener('DOMContentLoaded', function() {
        const savedTheme = localStorage.getItem('theme') || 'light';
        document.getElementById('body-theme').setAttribute('data-theme', savedTheme);
        if (savedTheme === 'dark') {
            document.getElementById('theme-toggle').innerHTML = '☀️';
        }

        // User search functionality
        document.getElementById('user-search').addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase().trim();
            const rows = document.querySelectorAll('#user-management-table tbody tr');
            rows.forEach(row => {
                const username = row.cells[0].textContent.toLowerCase();
                const email = row.cells[1].textContent.toLowerCase();
                if (username.includes(searchTerm) || email.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    });
</script>

</body>
</html>

