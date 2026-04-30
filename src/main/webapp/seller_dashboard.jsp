<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Seller Dashboard - NESTIQ</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600&display=swap" rel="stylesheet"/>
    <style>
        /* Reusing your slick NESTIQ variables */
        :root {
            --bg: #ffffff; --bg2: #f7f8fa; --ink: #0f1117; --line: #e8eaee;
            --accent: #1a56db; --font-sans: 'Outfit', sans-serif; --r: 10px;
            --green: #0d9e6e; --red: #e02828;
        }
        [data-theme="dark"] {
            --bg: #0f1117; --bg2: #1a1d27; --ink: #ffffff; --line: #232736;
        }
        body { font-family: var(--font-sans); background: var(--bg2); color: var(--ink); margin: 0; padding: 40px; transition: background 0.3s, color 0.3s; }

        .dashboard-container { max-width: 1000px; margin: 0 auto; }
        .header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }

        .card { background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); padding: 30px; margin-bottom: 30px; box-shadow: 0 4px 16px rgba(0,0,0,.04); }
        .card-title { font-size: 1.2rem; font-weight: 600; margin-bottom: 20px; margin-top: 0; }

        /* Forms & Buttons */
        .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        .form-group { display: flex; flex-direction: column; gap: 6px; }
        label { font-size: 0.85rem; font-weight: 500; }
        input, select, textarea { padding: 10px; border: 1.5px solid var(--line); border-radius: 6px; background: var(--bg); color: var(--ink); font-family: var(--font-sans); outline: none; }
        input:focus, select:focus, textarea:focus { border-color: var(--accent); }
        .btn { background: var(--accent); color: white; padding: 10px 20px; border: none; border-radius: 6px; font-weight: 600; cursor: pointer; transition: 0.2s; }
        .btn:hover { opacity: 0.9; }

        /* The Data Table */
        table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.9rem; }
        th, td { padding: 14px; border-bottom: 1px solid var(--line); }
        th { font-weight: 600; color: var(--accent); }
        .btn-edit { background: none; border: 1px solid var(--accent); color: var(--accent); padding: 6px 12px; border-radius: 4px; cursor: pointer; font-size: 0.8rem; }
        .btn-edit:hover { background: var(--accent); color: white; }

        /* The Edit Modal */
        .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: none; align-items: center; justify-content: center; z-index: 1000; }
        .modal-overlay.open { display: flex; }
        .modal-box { background: var(--bg); padding: 30px; border-radius: var(--r); width: 100%; max-width: 500px; }
        .close-btn { float: right; cursor: pointer; font-weight: bold; font-size: 1.2rem; color: var(--ink); }

        /* Container styling to match your dark theme */
        /* Container styling adapting to Light/Dark Mode */
        .profile-section {
            background: var(--bg); /* Automatically shifts between light/dark background */
            border: 1px solid var(--line); /* Adds a subtle, elegant outline */
            box-shadow: 0 4px 16px rgba(0,0,0,.04); /* Soft shadow to lift it off the page */
            padding: 25px;
            border-radius: var(--r);
            margin-bottom: 30px;
            color: var(--ink); /* Automatically shifts text color */
            max-width: 400px;
        }

        .profile-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
        }

        .profile-header h3 {
            margin: 0;
            font-size: 1.2rem;
        }

        /* 1. How it looks normally (Read-Only) */
        .readonly-input {
            background-color: transparent;
            border: 1px solid transparent;
            color: var(--ink); /* Adaptive text */
            font-size: 1rem;
            padding: 5px 0;
            outline: none;
        }

        /* 2. How it looks when editing */
        .editable-input {
            background-color: var(--bg2); /* Adaptive input background */
            border: 1px solid var(--line);
            border-radius: 4px;
            color: var(--ink); /* Adaptive text */
            padding: 10px;
            font-size: 1rem;
            outline: none;
        }

        .editable-input:focus {
            border-color: var(--accent); /* Nestiq Blue highlight when typing */
        }

        /* Button Styling */
        .edit-btn, .save-btn {
            background-color: #3b82f6;
            color: white;
            border: none;
            padding: 8px 16px;
            border-radius: 5px;
            cursor: pointer;
            font-weight: bold;
            transition: 0.2s;
        }

        .edit-btn:hover, .save-btn:hover {
            background-color: #2563eb;
        }

        .save-btn {
            width: 100%;
            margin-top: 10px;
            background-color: #10b981; /* Green color for saving */
        }

        .save-btn:hover {
            background-color: #059669;
        }

        /* ── INQUIRY CHAT MODAL (WhatsApp-like) ── */
        .chat-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: none; align-items: center; justify-content: center; z-index: 1200; padding: 18px; }
        .chat-overlay.open { display: flex; }
        .chat-box { background: var(--bg); border: 1px solid var(--line); border-radius: var(--r); width: 100%; max-width: 920px; height: min(78vh, 620px); box-shadow: 0 20px 70px rgba(0,0,0,.25); overflow: hidden; display: grid; grid-template-columns: 320px 1fr; }
        .chat-left { border-right: 1px solid var(--line); background: var(--bg2); padding: 18px; overflow-y: auto; }
        .chat-right { display: flex; flex-direction: column; height: 100%; min-height: 0; }
        .chat-top { padding: 16px 18px; border-bottom: 1px solid var(--line); display: flex; align-items: center; justify-content: space-between; gap: 10px; }
        .chat-peer { display: flex; align-items: center; gap: 10px; }
        .avatar { width: 34px; height: 34px; border-radius: 50%; background: var(--line); display: flex; align-items: center; justify-content: center; color: var(--ink); font-weight: 700; }
        .peer-name { font-weight: 700; }
        .peer-sub { font-size: 0.8rem; opacity: 0.75; }
        .chat-msgs { flex: 1; padding: 18px; overflow-y: auto; background: var(--bg); min-height: 0; }
        .bubble { max-width: 72%; padding: 10px 12px; border-radius: 12px; margin: 8px 0; border: 1px solid var(--line); }
        .bubble-left { background: var(--bg2); margin-right: auto; border-top-left-radius: 6px; }
        .bubble-right { background: rgba(26,86,219,0.10); margin-left: auto; border-top-right-radius: 6px; }
        .bubble-meta { font-size: 0.72rem; opacity: 0.75; margin-bottom: 4px; }
        .chat-compose { border-top: 1px solid var(--line); padding: 12px; display: flex; gap: 10px; background: var(--bg2); }
        .chat-input { flex: 1; resize: none; min-height: 42px; max-height: 110px; padding: 10px 12px; border-radius: 8px; border: 1.5px solid var(--line); background: var(--bg); color: var(--ink); }
        .chat-send { background: var(--accent); color: white; padding: 10px 16px; border-radius: 8px; font-weight: 700; }

        /* Small right-side scroll bar (chat messages) */
        .chat-msgs { scrollbar-width: thin; scrollbar-color: rgba(145,152,168,.8) transparent; }
        .chat-msgs::-webkit-scrollbar { width: 6px; }
        .chat-msgs::-webkit-scrollbar-track { background: transparent; }
        .chat-msgs::-webkit-scrollbar-thumb { background: rgba(145,152,168,.75); border-radius: 10px; }
        .chat-msgs::-webkit-scrollbar-thumb:hover { background: rgba(145,152,168,.95); }

       /* ── TELEGRAM STYLE THEME TOGGLE ── */
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
    </style>
</head>
<body data-theme="light" id="body-theme">

<div class="dashboard-container">
    <div class="header">
            <h2>👋 Welcome, ${sessionScope.loggedUser}</h2>
            <div style="display: flex; align-items: center; gap: 15px;">

                <div class="theme-switch" onclick="toggleTheme()" title="Toggle Dark Mode">
                    <div class="theme-switch-thumb" id="theme-toggle">🌙</div>
                </div>

                <!-- Bell Notifications (same as homepage) -->
                <div class="nav-notif" id="bell-container" onclick="toggleNotif(event)" style="position: relative; cursor: pointer; margin-right: 2px; display:flex; align-items:center;">
                    <span class="notif-icon" style="font-size: 1.5rem;">🔔</span>
                    <div id="notif-count" class="notif-badge" style="position:absolute; top:-5px; right:-5px; background:#e02828; color:white; font-size:0.65rem; padding:2px 6px; border-radius:10px; font-weight:bold; border:2px solid var(--bg); display:none;">0</div>
                    <div class="notif-panel" id="notif-panel" style="position:absolute; top:120%; right:0; width:320px; background:var(--bg); border:1px solid var(--line); border-radius:16px; box-shadow:0 32px 80px rgba(0,0,0,.16); display:none; z-index:2000; overflow:hidden;">
                        <div class="notif-header" style="padding:16px; border-bottom:1px solid var(--line); font-weight:700; font-size:0.95rem; background:var(--bg2);">Notifications</div>
                        <div id="notif-list" style="max-height:350px; overflow-y:auto;">
                            <p style="padding: 20px; text-align: center; color: rgba(0,0,0,.45); font-size: 0.85rem;">No new messages</p>
                        </div>
                        <div class="notif-footer" style="padding:12px; text-align:center; font-size:0.8rem; color:var(--accent); background:var(--bg2); border-top:1px solid var(--line);">Nestiq Secure Messaging</div>
                    </div>
                </div>

                <button class="btn" onclick="window.location.href='properties'" style="background: var(--line); color: var(--ink);">🏠 Go to Homepage</button>
                <form action="logout" method="post" style="display:inline;">
                    <button type="submit" class="btn" style="background: #e02828;">Logout</button>
                </form>
            </div>
    </div>

    <div class="profile-section">
        <div class="profile-header">
            <h3>Personal Information</h3>
            <button type="button" id="superEditBtn" class="edit-btn" onclick="toggleEditMode()">Edit</button>
        </div>

        <form action="UpdateProfileServlet" method="POST" id="profileForm">
            <input type="hidden" name="oldEmail" value="<%= session.getAttribute("loggedEmail") %>">

            <div class="form-group">
                <label style="color: var(--ink); opacity: 0.6; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 5px;">NAME</label>
                <input type="text" name="newName" value="<%= session.getAttribute("loggedUser") %>" readonly class="readonly-input">
            </div>

            <div class="form-group" style="margin-top: 15px;">
                <label style="color: var(--ink); opacity: 0.6; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 5px;">EMAIL</label>
                <input type="email" name="newEmail" value="<%= session.getAttribute("loggedEmail") %>" readonly class="readonly-input">
            </div>

            <div class="form-group" style="position: relative; margin-top: 15px;">
                <label style="color: var(--ink); opacity: 0.6; font-size: 0.85rem; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 5px;">PASSWORD</label>
                <input type="password" id="pwdInput" name="newPassword" value="<%= session.getAttribute("loggedPassword") %>" readonly class="readonly-input" style="padding-right: 35px; width: 100%; box-sizing: border-box;">

                <span onclick="togglePassword()" style="position: absolute; right: 10px; top: 32px; cursor: pointer; color: #a0aabf; transition: 0.2s;">
                    <svg id="eyeOpen" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                        <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                    <svg id="eyeClosed" xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="display: none;">
                        <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                        <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                </span>
            </div>

            <button type="submit" id="saveProfileBtn" class="save-btn" style="display: none;">Save Changes</button>
        </form>
        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid var(--line); text-align: center;">
                    <form action="DeleteAccountServlet" method="POST" style="margin: 0;" onsubmit="return confirm('⚠️ WARNING: Are you absolutely sure you want to permanently delete your account? This action cannot be undone and you will lose all saved properties.');">
                        <input type="hidden" name="userEmail" value="<%= session.getAttribute("loggedEmail") %>">
                        <button type="submit" style="background: transparent; color: var(--red); border: none; font-size: 0.85rem; font-weight: 600; cursor: pointer; text-decoration: underline;">
                            Delete My Account
                        </button>
                    </form>
        </div>
    </div>

    <div class="card">
        <h3 class="card-title">List a New Property</h3>
        <p style="color: #0d9e6e; font-weight: bold;">${successMessage}</p>
        <form action="addProperty" method="post" class="form-grid">
            <div class="form-group"><label>Property Title</label><input type="text" name="title" required></div>
            <div class="form-group"><label id="add-price-label">Price ($)</label><input type="number" name="price" required></div>
            <div class="form-group"><label>Location / City</label><input type="text" name="location" required></div>
            <div class="form-group"><label>Type</label>
                <select name="type"><option>Apartment</option><option>House</option><option>Villa</option><option>Studio</option></select>
            </div>
            <div class="form-group"><label>Bed Rooms</label><input type="number" name="bedrooms" min="0" required></div>
            <div class="form-group"><label>Bath Tubs</label><input type="number" name="bathrooms" min="0" required></div>
            <div class="form-group"><label>Status</label>
                <select name="status" id="add-status-select" onchange="updatePriceLabel('add')">
                    <option>For Sale</option>
                    <option>For Rent</option>
                </select>
            </div>
            <div class="form-group"><label>Description</label><textarea name="description" rows="3"></textarea></div>
            <div class="form-group" style="display: flex; align-items: flex-end;">
                <button type="submit" class="btn" style="width: 100%;">➕ Add Property</button>
            </div>
        </form>
    </div>

    <div class="card">
        <h3 class="card-title">My Managed Properties</h3>
        <table>
            <thead>
                <tr><th>ID</th><th>Title</th><th>Price</th><th>Location</th><th>Type</th><th>Beds</th><th>Baths</th><th>Actions</th></tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${not empty myProperties}">
                        <c:forEach var="p" items="${myProperties}">
                            <tr>
                                <td><small>${p.id}</small></td>
                                <td>${p.title}</td>
                                <td>$${p.price}</td>
                                <td>${p.location}</td>
                                <td>${p.type}</td>
                                <td>${p.bedrooms}</td>
                                <td>${p.bathrooms}</td>
                                <td style="display: flex; gap: 8px;">
                                   <button class="btn-edit" onclick="openEditModal('${p.id}', '${p.title}', '${p.price}', '${p.location}', '${p.type}', '${p.status}', '${p.bedrooms}', '${p.bathrooms}', '${p.description}')">✏️ Edit</button>

                                   <form action="deleteProperty" method="post" style="margin: 0;" onsubmit="return confirm('Are you absolutely sure you want to delete this property? This cannot be undone!');">
                                       <input type="hidden" name="propertyId" value="${p.id}">
                                       <button type="submit" class="btn-edit" style="color: var(--red); border-color: var(--red);">🗑️ Delete</button>
                                   </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr><td colspan="8" style="text-align:center;">You haven't listed any properties yet.</td></tr>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </div>

    <div class="card" id="reviews-section">
        <h3 class="card-title">💬 Property Reviews</h3>
        <div style="color: var(--ink); opacity: 0.75; font-size: 0.9rem; margin-top: -10px; margin-bottom: 16px;">
            Reviews posted by buyers for your listings.
        </div>

        <c:choose>
            <c:when test="${empty myProperties}">
                <div style="text-align:center; padding: 18px; opacity: 0.8;">
                    No properties found. Add a property to start receiving reviews.
                </div>
            </c:when>
            <c:otherwise>
                <div style="display:flex; flex-direction:column; gap: 16px;">
                    <c:forEach var="p" items="${myProperties}">
                        <div style="border: 1px solid var(--line); border-radius: var(--r); background: var(--bg2); padding: 16px;">
                            <div style="display:flex; justify-content:space-between; gap: 12px; flex-wrap: wrap;">
                                <div>
                                    <div style="font-weight: 800;">${p.title}</div>
                                    <div style="font-size: 0.85rem; opacity: 0.8;">Property ID: <small>${p.id}</small></div>
                                </div>
                                <div style="font-size: 0.85rem; opacity: 0.8;">
                                    ${p.location} · ${p.type}
                                </div>
                            </div>

                            <c:set var="revList" value="${reviewsByProperty[p.id]}"/>
                            <c:choose>
                                <c:when test="${empty revList}">
                                    <div style="margin-top: 12px; padding: 12px; border-radius: 8px; background: var(--bg); border: 1px solid var(--line); opacity: 0.9;">
                                        No reviews yet.
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div style="margin-top: 12px; display:flex; flex-direction:column; gap: 10px;">
                                        <c:forEach var="r" items="${revList}">
                                            <div style="padding: 12px; border-radius: 8px; background: var(--bg); border: 1px solid var(--line);">
                                                <div style="display:flex; justify-content:space-between; align-items:flex-start; gap: 10px; flex-wrap: wrap;">
                                                    <div style="font-weight: 700;"><c:out value="${r.buyerName}"/></div>
                                                    <div style="display: flex; align-items: center; gap: 10px;">
                                                        <div style="color: #d97706; letter-spacing: 1px; font-size: 0.9rem;">
                                                            <c:forEach begin="1" end="${r.rating}" var="i">★</c:forEach>
                                                            <c:forEach begin="1" end="${5 - r.rating}" var="i">☆</c:forEach>
                                                        </div>
                                                        <form action="deleteReview" method="post" onsubmit="return confirm('Are you sure you want to delete this review?');">
                                                            <input type="hidden" name="reviewId" value="${r.reviewID}">
                                                            <button type="submit" class="btn-edit" style="color: var(--red); border-color: var(--red);">Remove</button>
                                                        </form>
                                                    </div>
                                                </div>
                                                <div style="margin-top: 6px; opacity: 0.9; white-space: pre-wrap;">
                                                    <c:out value="${r.comment}"/>
                                                </div>
                                            </div>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </c:forEach>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="card">
        <div style="display:flex; align-items:center; justify-content:space-between; gap: 12px; flex-wrap: wrap;">
            <h3 class="card-title" style="margin-bottom: 0;">📩 Buyer Inquiries</h3>
            <div style="display:flex; gap: 10px; align-items:center;">
                <select id="inqSelect" style="min-width: 320px; max-width: 520px;">
                    <option value="">Select an inquiry...</option>
                    <c:forEach var="t" items="${sellerThreads}">
                        <option value="${t.id}">${t.buyerName} — ${t.propertyTitle}</option>
                    </c:forEach>
                </select>
                <button class="btn" type="button" onclick="openInquiryFromSelect()">Open</button>
            </div>
        </div>

        <div style="margin-top: 14px; color: var(--ink); opacity: 0.75; font-size: 0.9rem;">
            <c:choose>
                <c:when test="${empty sellerThreads}">No inquiries yet.</c:when>
                <c:otherwise>Tip: pick an inquiry to view the chat thread and reply.</c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div class="modal-overlay" id="editModal">
    <div class="modal-box">
        <span class="close-btn" onclick="closeEditModal()">×</span>
        <h3 class="card-title">Update Property</h3>
        <form action="updateProperty" method="post" style="display: flex; flex-direction: column; gap: 16px;">
            <input type="hidden" name="propertyId" id="edit-id">

            <div class="form-group"><label>Title</label><input type="text" name="title" id="edit-title" required></div>
            <div class="form-group"><label id="edit-price-label">Price ($)</label><input type="number" name="price" id="edit-price" required></div>
            <div class="form-group"><label>Location</label><input type="text" name="location" id="edit-location" required></div>
            <div class="form-grid">
                <div class="form-group"><label>Type</label>
                    <select name="type" id="edit-type"><option>Apartment</option><option>House</option><option>Villa</option><option>Studio</option></select>
                </div>
                <div class="form-group"><label>Bed Rooms</label><input type="number" name="bedrooms" id="edit-bedrooms" min="0" required></div>
                <div class="form-group"><label>Bath Tubs</label><input type="number" name="bathrooms" id="edit-bathrooms" min="0" required></div>
                <div class="form-group"><label>Status</label>
                    <select name="status" id="edit-status-select" onchange="updatePriceLabel('edit')">
                        <option>For Sale</option>
                        <option>For Rent</option>
                    </select>
                </div>
            </div>
            <div class="form-group"><label>Description</label><textarea name="description" id="edit-description" rows="3"></textarea></div>
            <button type="submit" class="btn">💾 Save Changes</button>
        </form>
    </div>
</div>

<script src="app.js"></script>

<script>
    // Bulletproof direct function - customized for superEditBtn and emoji-free text!
    function toggleEditMode() {
        const btn = document.getElementById('superEditBtn'); // Ensure ID matches!
        const inputs = document.querySelectorAll('#profileForm input[type="text"], #profileForm input[type="email"], #profileForm input[type="password"]');
        const saveBtn = document.getElementById('saveProfileBtn');

        // Check if the save button is currently visible
        const isEditing = saveBtn.style.display === 'block';

        if (!isEditing) {
            // Switch TO Edit Mode
            inputs.forEach(input => {
                input.removeAttribute('readonly');
                input.classList.remove('readonly-input');
                input.classList.add('editable-input');
            });
            saveBtn.style.display = 'block';
            btn.innerHTML = 'Cancel'; // Emoji-free text!
            btn.style.backgroundColor = '#ef4444'; // Make button red
        } else {
            // Switch BACK to Read-Only Mode
            inputs.forEach(input => {
                input.setAttribute('readonly', true);
                input.classList.remove('editable-input');
                input.classList.add('readonly-input');
            });
            saveBtn.style.display = 'none';
            btn.innerHTML = 'Edit'; // Emoji-free text!
            btn.style.backgroundColor = '#3b82f6'; // Make button blue

            // Revert changes if they click cancel without saving
            document.getElementById('profileForm').reset();
        }
    }
    // Show/Hide Password Toggle
        function togglePassword() {
            const pwdInput = document.getElementById('pwdInput');
            const eyeOpen = document.getElementById('eyeOpen');
            const eyeClosed = document.getElementById('eyeClosed');

            // If it's currently hidden, reveal it!
            if (pwdInput.type === 'password') {
                pwdInput.type = 'text';
                eyeOpen.style.display = 'none';
                eyeClosed.style.display = 'block';
            }
            // If it's revealed, hide it!
            else {
                pwdInput.type = 'password';
                eyeOpen.style.display = 'block';
                eyeClosed.style.display = 'none';
            }
        }
</script>

<script>
    // Identity bridge for notifications on this page too
    window.currentUser = "${sessionScope.loggedUser}";
    window.currentRole = "${sessionScope.loggedRole}";
    window.allNotifications = [];
    <c:forEach items="${allNotifications}" var="n">
        window.allNotifications.push({
            sender: "${n.sender}",
            receiver: "${n.receiver}",
            message: "${n.content}",
            property: "${n.propTitle}",
            type: "${n.type}",
            threadId: "${n.threadId}"
        });
    </c:forEach>
</script>
<script>
    function openEditModal(id, title, price, location, type, status, bedrooms, bathrooms, description) {
        document.getElementById('edit-id').value = id;
        document.getElementById('edit-title').value = title;
        document.getElementById('edit-price').value = price;
        document.getElementById('edit-location').value = location;
        document.getElementById('edit-type').value = type;
        document.getElementById('edit-status-select').value = status; // Set status first
        document.getElementById('edit-bedrooms').value = bedrooms;
        document.getElementById('edit-bathrooms').value = bathrooms;
        document.getElementById('edit-description').value = description;

        updatePriceLabel('edit'); // Call to update label based on status
        document.getElementById('editModal').classList.add('open');
    }

    function closeEditModal() {
        document.getElementById('editModal').classList.remove('open');
    }

    function updatePriceLabel(formType) {
        let statusSelectId = formType === 'add' ? 'add-status-select' : 'edit-status-select';
        let priceLabelId = formType === 'add' ? 'add-price-label' : 'edit-price-label';

        let statusSelect = document.getElementById(statusSelectId);
        let priceLabel = document.getElementById(priceLabelId);

        if (statusSelect && priceLabel) {
            if (statusSelect.value === 'For Rent') {
                priceLabel.innerText = 'Monthly Price ($)';
            } else {
                priceLabel.innerText = 'Price ($)';
            }
        }
    }

    // Initial call for add form on page load
    document.addEventListener('DOMContentLoaded', () => {
        updatePriceLabel('add');
    });
</script>

<!-- Inquiry Chat Modal -->
<div class="chat-overlay" id="chatOverlay" onclick="closeChatIfOutside(event)">
    <div class="chat-box" onclick="event.stopPropagation()">
        <div class="chat-left">
            <div style="font-weight: 800; margin-bottom: 12px;">Inquiry Details</div>
            <div style="display:flex; flex-direction:column; gap: 10px;">
                <div>
                    <div style="font-size:0.75rem; opacity:0.7; text-transform: uppercase; letter-spacing: .6px;">Buyer</div>
                    <div id="chatBuyerName" style="font-weight:700;"></div>
                </div>
                <div>
                    <div style="font-size:0.75rem; opacity:0.7; text-transform: uppercase; letter-spacing: .6px;">Email</div>
                    <div id="chatBuyerEmail"></div>
                </div>
                <div>
                    <div style="font-size:0.75rem; opacity:0.7; text-transform: uppercase; letter-spacing: .6px;">Phone</div>
                    <div id="chatBuyerPhone"></div>
                </div>
                <div style="padding-top: 10px; border-top: 1px solid var(--line);">
                    <div style="font-size:0.75rem; opacity:0.7; text-transform: uppercase; letter-spacing: .6px;">Property</div>
                    <div id="chatPropTitle" style="font-weight:700;"></div>
                    <div id="chatPropId" style="font-size:0.85rem; opacity:0.8;"></div>
                </div>
                <div style="padding-top: 10px; border-top: 1px solid var(--line);">
                    <div style="font-size:0.75rem; opacity:0.7; text-transform: uppercase; letter-spacing: .6px;">Thread</div>
                    <div id="chatThreadId" style="font-size:0.85rem; opacity:0.85; word-break: break-all;"></div>
                </div>
            </div>
        </div>

        <div class="chat-right">
            <div class="chat-top">
                <div class="chat-peer">
                    <div class="avatar">👤</div>
                    <div>
                        <div class="peer-name" id="chatHeaderName">Buyer</div>
                        <div class="peer-sub" id="chatHeaderSub">Inquiry chat</div>
                    </div>
                </div>
                <button class="btn" type="button" style="background: var(--line); color: var(--ink);" onclick="closeChat()">Close</button>
            </div>

            <div class="chat-msgs" id="chatMsgs"></div>

            <form class="chat-compose" action="replyInquiry" method="post" onsubmit="return validateChatSend();">
                <input type="hidden" name="threadId" id="chatThreadIdInput">
                <textarea class="chat-input" name="message" id="chatInput" placeholder="Type a reply..." required></textarea>
                <button class="chat-send" type="submit">Send</button>
            </form>
        </div>
    </div>
</div>

<script>
    // Build a JS map of seller threads from server-side data
    window.sellerThreads = {};
    <c:forEach var="t" items="${sellerThreads}">
        window.sellerThreads["${t.id}"] = {
            id: "${t.id}",
            propertyId: "${t.propertyId}",
            propertyTitle: "${t.propertyTitle}",
            buyerName: "${t.buyerName}",
            buyerEmail: "${t.buyerEmail}",
            buyerPhone: "${t.buyerPhone}",
            createdDate: "${t.createdDate}",
            status: "${t.status}"
        };
    </c:forEach>

    function openInquiryFromSelect() {
        const sel = document.getElementById('inqSelect');
        if (!sel || !sel.value) return;
        openChat(sel.value);
    }

    function openChat(threadId) {
        const t = window.sellerThreads ? window.sellerThreads[threadId] : null;
        if (!t) return;

        document.getElementById('chatBuyerName').innerText = t.buyerName || '(No name)';
        document.getElementById('chatBuyerEmail').innerText = t.buyerEmail || '(No email)';
        document.getElementById('chatBuyerPhone').innerText = t.buyerPhone || '(No phone)';
        document.getElementById('chatPropTitle').innerText = t.propertyTitle || '(No title)';
        document.getElementById('chatPropId').innerText = t.propertyId ? ('Property ID: ' + t.propertyId) : '';
        document.getElementById('chatThreadId').innerText = t.id;

        document.getElementById('chatHeaderName').innerText = t.buyerName || 'Buyer';
        document.getElementById('chatHeaderSub').innerText = (t.createdDate ? ('Created: ' + t.createdDate) : 'Inquiry chat');

        document.getElementById('chatThreadIdInput').value = t.id;

        const msgs = document.getElementById('chatMsgs');
        const container = document.getElementById('thread-msgs-' + t.id);
        msgs.innerHTML = container ? container.innerHTML : '';

        document.getElementById('chatOverlay').classList.add('open');
        setTimeout(() => { msgs.scrollTop = msgs.scrollHeight; }, 50);
        setTimeout(() => { document.getElementById('chatInput').focus(); }, 80);

        // Mark as read so the bell bubble decreases
        try {
            fetch('markInquiryRead', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'threadId=' + encodeURIComponent(threadId)
            }).catch(() => {});
        } catch (e) {}
    }

    function closeChat() {
        document.getElementById('chatOverlay').classList.remove('open');
        document.getElementById('chatInput').value = '';
    }

    function closeChatIfOutside(event) {
        if (event.target && event.target.id === 'chatOverlay') closeChat();
    }

    function validateChatSend() {
        const text = document.getElementById('chatInput').value;
        return text && text.trim().length > 0;
    }

    // Auto-open from notification bell routing (sellerDashboard?threadId=...)
    document.addEventListener("DOMContentLoaded", () => {
        const params = new URLSearchParams(window.location.search);
        const threadId = params.get('threadId');
        if (threadId) {
            setTimeout(() => openChat(threadId), 150);
        }
    });
</script>

<script>
    // Auto-scroll to reviews section after a review deletion
    (function() {
        const params = new URLSearchParams(window.location.search);
        if (params.get('scrollTo') === 'reviews') {
            const reviewsSection = document.getElementById('reviews-section');
            if (reviewsSection) {
                reviewsSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
            // Clean the URL so refreshing doesn't re-scroll
            const cleanUrl = window.location.pathname;
            history.replaceState(null, '', cleanUrl);
        }
    })();
</script>

<!-- Hidden rendered messages per thread (safe HTML escaping) -->
<div style="display:none;">
    <c:forEach var="t" items="${sellerThreads}">
        <div id="thread-msgs-${t.id}">
            <c:forEach var="m" items="${t.messages}">
                <c:choose>
                    <c:when test="${m.senderRole == 'SELLER'}">
                        <div class="bubble bubble-right">
                            <div class="bubble-meta">
                                <strong><c:out value="${m.senderName}"/></strong>
                                · <c:out value="${m.timestamp}"/>
                            </div>
                            <div style="white-space: pre-wrap;"><c:out value="${m.content}"/></div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="bubble bubble-left">
                            <div class="bubble-meta">
                                <strong><c:out value="${m.senderName}"/></strong>
                                · <c:out value="${m.timestamp}"/>
                            </div>
                            <div style="white-space: pre-wrap;"><c:out value="${m.content}"/></div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
    </c:forEach>
</div>

</body>
</html>