// ── DARK MODE LOGIC ───────────────────────────────────
if (window.localStorage.getItem('nestiq_theme') === 'dark') {
    window.document.documentElement.setAttribute('data-theme', 'dark');
    window.document.addEventListener("DOMContentLoaded", () => {
        const btn = window.document.getElementById('theme-toggle');
        if(btn) btn.textContent = '☀️';
    });
}

function toggleTheme() {
    const html = window.document.documentElement;
    const btn = window.document.getElementById('theme-toggle');
    if (html.getAttribute('data-theme') === 'dark') {
        html.removeAttribute('data-theme');
        window.localStorage.setItem('nestiq_theme', 'light');
        if(btn) btn.textContent = '🌙';
    } else {
        html.setAttribute('data-theme', 'dark');
        window.localStorage.setItem('nestiq_theme', 'dark');
        if(btn) btn.textContent = '☀️';
    }
}

// ── DATA ──────────────────────────────────────────────
// Sri Lankan names + Sri Lanka-focused face photos (Unsplash source query).
// (Using `source.unsplash.com` keeps it simple without needing local image assets.)
const AGENTS = [
    { id: 1, name: "Senuri Fernando", title: "Senior Luxury Specialist", agency: "Nestiq Colombo 07", phone: "+94 77 101 2020", email: "s.fernando@nestiq.lk", img: "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=600&q=80&fit=crop", listings: 24, sold: 187, rating: 4.9, spec: "luxury", years: 12 },
    { id: 2, name: "Dinesh Perera", title: "Investment & Commercial", agency: "Nestiq Kandy", phone: "+94 71 202 3030", email: "d.perera@nestiq.lk", img: "https://images.unsplash.com/photo-1560250097-0b93528c311a?w=600&q=80&fit=crop", listings: 18, sold: 143, rating: 4.8, spec: "investment", years: 9 },
    { id: 3, name: "Kavindi De Silva", title: "Residential Expert", agency: "Nestiq Kurunegala", phone: "+94 70 303 4040", email: "k.desilva@nestiq.lk", img: "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=600&q=80&fit=crop", listings: 31, sold: 210, rating: 4.9, spec: "residential", years: 15 },
    { id: 4, name: "Nuwan Rathnayake", title: "Commercial Specialist", agency: "Nestiq Galle", phone: "+94 76 404 5050", email: "n.rathnayake@nestiq.lk", img: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=600&q=80&fit=crop", listings: 12, sold: 98, rating: 4.7, spec: "commercial", years: 7 },
    { id: 5, name: "Anjali Jayasooriya", title: "Luxury Condo Specialist", agency: "Nestiq Mount Lavinia", phone: "+94 77 505 6060", email: "a.jayasooriya@nestiq.lk", img: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=600&q=80&fit=crop", listings: 20, sold: 156, rating: 4.8, spec: "luxury", years: 10 },
    { id: 6, name: "Mohammed Rizwan", title: "Investment Properties", agency: "Nestiq Negombo", phone: "+94 75 606 7070", email: "m.rizwan@nestiq.lk", img: "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=600&q=80&fit=crop", listings: 15, sold: 121, rating: 4.7, spec: "investment", years: 8 },
    { id: 7, name: "Thilini Wijeratne", title: "Residential Advisor", agency: "Nestiq Malabe", phone: "+94 76 909 1010", email: "t.wijeratne@nestiq.lk", img: "https://images.unsplash.com/photo-1554151228-14d9def656e4?w=600&q=80&fit=crop", listings: 22, sold: 132, rating: 4.8, spec: "residential", years: 8 },
    { id: 8, name: "Isuru Bandara", title: "Commercial Leasing", agency: "Nestiq Colombo 03", phone: "+94 77 808 1212", email: "i.bandara@nestiq.lk", img: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&q=80&fit=crop", listings: 14, sold: 76, rating: 4.6, spec: "commercial", years: 6 },
    { id: 9, name: "Ruwan Gunawardena", title: "Luxury Beachfront Expert", agency: "Nestiq Bentota", phone: "+94 77 111 2222", email: "r.gunawardena@nestiq.lk", img: "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=600&q=80&fit=crop", listings: 18, sold: 112, rating: 4.9, spec: "luxury", years: 11 },
    { id: 10, name: "Nilanthi Wickramasinghe", title: "Investment Strategist", agency: "Nestiq Colombo 01", phone: "+94 71 333 4444", email: "n.wickramasinghe@nestiq.lk", img: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=600&q=80&fit=crop", listings: 25, sold: 165, rating: 4.8, spec: "investment", years: 14 },
    { id: 11, name: "Prasanna Kumarage", title: "Residential Sales Head", agency: "Nestiq Maharagama", phone: "+94 76 555 6666", email: "p.kumarage@nestiq.lk", img: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=600&q=80&fit=crop", listings: 40, sold: 280, rating: 5.0, spec: "residential", years: 18 },
    { id: 12, name: "Kusal Janith", title: "Commercial Portfolio Manager", agency: "Nestiq Ratnapura", phone: "+94 70 777 8888", email: "k.janith@nestiq.lk", img: "https://images.unsplash.com/photo-1531427186611-ecfd6d936c79?w=600&q=80&fit=crop", listings: 10, sold: 65, rating: 4.7, spec: "commercial", years: 5 }
];

const TESTIMONIALS = [
    { name:"Chamathka Silva", role:"Bought in Colombo 07", img:"https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&q=80", text:"Nestiq made finding our dream apartment effortless. Senuri guided us with exceptional patience and expertise — we couldn't be happier.", stars:5 },
    { name:"Kasun & Tharushi", role:"Sold in Kurunegala", img:"https://images.unsplash.com/photo-1552374196-c4e7ffc6e126?w=100&q=80", text:"Sold our villa in under two weeks for above the asking price. Dinesh's market knowledge and professional network are truly unmatched.", stars:5 },
    { name:"Ashan Dias", role:"Renting in Malabe", img:"https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100&q=80", text:"The entire process was transparent, fast and stress-free. Kavindi found us the perfect annex near the campus. Exceptional service from start to finish.", stars:5 }
];

let currentFilters = { status: 'all', type: 'all', city: 'all', beds: 'any', minPrice: '', maxPrice: '' };
let savedIds = [];

// ── ROUTER ────────────────────────────
function showPage(name) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    const pageObj = document.getElementById('page-' + name);
    if(pageObj) pageObj.classList.add('active');

    document.querySelectorAll('.nav-links a').forEach(a => a.classList.remove('active'));
    const navItem = document.getElementById('nav-' + name);
    if(navItem) navItem.classList.add('active');
    window.scrollTo(0,0);

    if (name === 'listings') applyFilters();
    if (name === 'agents') renderAgentsFull();
    if (name === 'home') initHome();
}

// ── HOME PAGE FILTER STATE ──────────────────────────
let homeFilterType = 'all';
let homeSearchCity = '';

// ── INIT HOME ──────────────────────────
function initHome() {
    renderTestimonials();
    renderHomeAgents();
    homeFilterType = 'all';
    homeSearchCity = '';
    renderHomeFeaturedProperties();
}

function filterHome(btn, filterType) {
    document.querySelectorAll('.filter-bar .filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    homeFilterType = filterType;
    renderHomeFeaturedProperties();
}

function searchHomeByCity(searchTerm, searchType) {
    homeSearchCity = searchTerm.toLowerCase().trim();
    homeFilterType = searchType.toLowerCase().trim();
    renderHomeFeaturedProperties();

    const messageContainer = document.getElementById('search-message-container');
    if (messageContainer) {
        if (homeSearchCity === '') {
            messageContainer.innerHTML = '';
            return;
        }

        const cityProperties = window.properties.filter(p => p.location.toLowerCase().includes(homeSearchCity));
        if (cityProperties.length > 0) {
            const typeProperties = cityProperties.filter(p => homeFilterType === '' || p.type.toLowerCase() === homeFilterType);
            if (typeProperties.length > 0) {
                messageContainer.innerHTML = `<div class="search-message" style="color: var(--green); background-color: var(--green-l);">Properties Found</div>`;
            } else {
                messageContainer.innerHTML = `<div class="search-message" style="color: var(--amber); background-color: var(--amber-l);">No Properties found with the category of ${searchType}</div>`;
            }
        } else {
            messageContainer.innerHTML = `<div class="search-message" style="color: var(--red); background-color: var(--amber-l);">No Properties found in the city of ${searchTerm}</div>`;
        }
    }
}

function renderHomeFeaturedProperties() {
    const grid = document.getElementById('home-prop-grid');
    if (!grid || typeof window.properties === 'undefined') return;

    // Filter properties based on both the type filter and city search
    let filtered = window.properties.filter(p => {
        // Combined filter for status (sale/rent) and type (apartment/house)
        const statusOrType = homeFilterType.toLowerCase();
        let matchFilter;
        if (statusOrType === 'all' || statusOrType === '') {
            matchFilter = true;
        } else if (statusOrType === 'sale' || statusOrType === 'rent') {
            // The filter is for status
            matchFilter = p.status && p.status.toLowerCase().includes(statusOrType);
        } else {
            // The filter is for property type
            matchFilter = p.type && p.type.toLowerCase().includes(statusOrType);
        }

        // City search filter
        let matchCity = homeSearchCity === '' || (p.location && p.location.toLowerCase().includes(homeSearchCity));

        return matchFilter && matchCity;
    });

    // Re-render the grid
    if (filtered.length === 0) {
        grid.innerHTML = '<p style="grid-column: 1 / -1; text-align: center; color: var(--ink3); padding: 40px;">No properties match your search.</p>';
        return;
    }

    grid.innerHTML = filtered.map(p => {
        const displayPrice = typeof p.price === 'number' ? p.price.toLocaleString() : p.price;
        const safeStatus = p.status ? p.status.toLowerCase() : 'sale';
        const isRent = safeStatus.includes('rent');
        const tagClass = isRent ? 'tag-rent' : 'tag-sale';
        const tagText = isRent ? 'For Rent' : 'For Sale';

        return `
        <div class="prop-card" onclick="openDetail('${p.id}')" style="cursor: pointer;">
            <div class="prop-img-wrap">
                <img src="${p.image}" alt="${p.title}"/>
                <div class="prop-tags">
                    <span class="prop-tag ${tagClass}">${tagText}</span>
                </div>
            </div>
            <div class="prop-body">
                <div class="prop-price">$${displayPrice}</div>
                <div class="prop-name">${p.title}</div>
                <div class="prop-loc">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0118 0z"/><circle cx="12" cy="10" r="3"/></svg>
                    ${p.location}
                </div>
                <div class="prop-divider"></div>
                <div class="prop-meta">
                    <div class="prop-meta-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 10V4h-5"/><path d="M15 10l-6 6-4-4"/></svg>
                        ${p.bedrooms} Beds
                    </div>
                    <div class="prop-meta-item">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 10V4h-5"/><path d="M15 10l-6 6-4-4"/></svg>
                        ${p.bathrooms} Baths
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');
}

function renderTestimonials() {
    const grid = document.getElementById('testi-grid');
    if(!grid) return;
    grid.innerHTML = TESTIMONIALS.map(t => `
    <div class="testi-card">
        <div class="testi-stars">${'★'.repeat(t.stars)}</div>
        <p class="testi-text">"${t.text}"</p>
        <div class="testi-author">
            <img src="${t.img}" alt="${t.name}"/>
            <div>
                <div class="testi-name">${t.name}</div>
                <div class="testi-role">${t.role}</div>
            </div>
        </div>
    </div>`).join('');
}

function renderHomeAgents() {
    const grid = document.getElementById('home-agents-grid');
    if(!grid) return;
    grid.innerHTML = AGENTS.slice(0, 4).map((a, i) => `
    <div class="agent-card" style="animation-delay:${i*0.06}s">
        <div class="agent-card-img"><img src="${a.img}" alt="${a.name}" loading="lazy"/></div>
        <div class="agent-card-body">
            <div class="agent-card-name">${a.name}</div>
            <div class="agent-card-role">${a.title}</div>
            <div style="font-size:.78rem;color:var(--ink4);margin-bottom:14px">${a.agency} · ${a.years} yrs exp</div>
        </div>
    </div>`).join('');
}

// ── AGENTS PAGE ENGINE ──────────────────────
let currentAgentFilter = 'all';

function agentCardTemplate(a, i = 0) {
    const safeRating = (typeof a.rating === 'number') ? a.rating.toFixed(1) : (a.rating || '4.8');
    return `
    <div class="agent-card" style="animation-delay:${i * 0.05}s">
        <div class="agent-card-img">
            <img src="${a.img}" alt="${a.name}" loading="lazy" onerror="this.onerror=null; this.src='https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=600&q=80&fit=crop';"/>
        </div>
        <div class="agent-card-body">
            <div class="agent-card-name">${a.name}</div>
            <div class="agent-card-role">${a.title}</div>
            <div style="font-size:.78rem;color:var(--ink4);margin-bottom:14px">${a.agency} · ${a.years} yrs exp</div>
            <div class="agent-card-stats">
                <div class="ac-stat">
                    <div class="ac-stat-val">${a.listings}</div>
                    <div class="ac-stat-label">Listings</div>
                </div>
                <div class="ac-stat">
                    <div class="ac-stat-val">${a.sold}</div>
                    <div class="ac-stat-label">Sold</div>
                </div>
                <div class="ac-stat">
                    <div class="ac-stat-val">${safeRating}</div>
                    <div class="ac-stat-label">Rating</div>
                </div>
            </div>
        </div>
    </div>`;
}

function renderAgentsFull() {
    const grid = document.getElementById('agents-grid');
    if (!grid) return;

    const list = (currentAgentFilter === 'all')
        ? AGENTS
        : AGENTS.filter(a => String(a.spec).toLowerCase() === String(currentAgentFilter).toLowerCase());

    if (!list || list.length === 0) {
        grid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--ink3); padding: 40px; font-weight: 500;">No agents found in this category.</p>`;
        return;
    }

    grid.innerHTML = list.map((a, i) => agentCardTemplate(a, i)).join('');
}

function filterAgents(btn, spec) {
    document.querySelectorAll('#page-agents .filter-btn').forEach(b => b.classList.remove('active'));
    if (btn) btn.classList.add('active');
    currentAgentFilter = spec || 'all';
    renderAgentsFull();
}

// 🔥 THE FACE ROUTER 🔥
function getAgentForSeller(sellerName) {
    if (!sellerName || sellerName === "null") return AGENTS[0];
    let name = sellerName.toLowerCase();

    // Force specific faces based on test accounts
    if (name.includes('silva') && !name.includes('kavindi')) return AGENTS[3]; // Nuwan face
    if (name.includes('kavindi')) return AGENTS[2]; // Kavindi face
    if (name.includes('dinesh')) return AGENTS[5];  // Rizwan face
    if (name.includes('rizwan')) return AGENTS[1];

    // Default dynamic face for anyone else
    let sum = 0;
    for(let i = 0; i < name.length; i++) sum += name.charCodeAt(i);
    return AGENTS[sum % AGENTS.length];
}

// ── LISTINGS ENGINE ──────────────────────
let currentListingsPage = 0;
let itemsPerPage = 10;
let allFilteredProperties = [];
let currentSort = 'default';  // 'default'|'price_asc'|'price_desc'|'newest'|'views'
let currentViewMode = 'grid'; // 'grid'|'list'

function toggleChip(btn, category, value) {
    let siblings = btn.parentElement.querySelectorAll('.filter-chip');
    siblings.forEach(s => s.classList.remove('active'));
    btn.classList.add('active');
    currentFilters[category] = value;
    currentListingsPage = 0;  // Reset to first page when filtering
    applyFilters();
}

function setBeds(btn, value) {
    let siblings = btn.parentElement.querySelectorAll('.bed-btn');
    siblings.forEach(s => s.classList.remove('active'));
    btn.classList.add('active');
    currentFilters.beds = value;
    currentListingsPage = 0;  // Reset to first page when filtering
    applyFilters();
}

function resetFilters() {
    currentFilters = { status: 'all', type: 'all', city: 'all', beds: 'any', minPrice: '', maxPrice: '' };
    currentListingsPage = 0;  // Reset to first page
    document.querySelectorAll('.filter-chip, .bed-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.filter-chip[onclick*="\'all\'"]').forEach(c => c.classList.add('active'));
    document.querySelectorAll('.bed-btn[onclick*="\'any\'"]').forEach(b => b.classList.add('active'));
    if(document.getElementById('filterMinPrice')) document.getElementById('filterMinPrice').value = '';
    if(document.getElementById('filterMaxPrice')) document.getElementById('filterMaxPrice').value = '';
    applyFilters();
}

function applyFilters() {
    if(typeof window.properties === 'undefined') return;

    const minInput = document.getElementById('filterMinPrice');
    const maxInput = document.getElementById('filterMaxPrice');
    if (minInput) currentFilters.minPrice = minInput.value;
    if (maxInput) currentFilters.maxPrice = maxInput.value;

    let filtered = window.properties.filter(p => {
        let matchStatus = currentFilters.status === 'all' || (p.status && p.status.toLowerCase().includes(currentFilters.status.toLowerCase()));
        let matchType = currentFilters.type === 'all' || (p.type && p.type.toLowerCase() === currentFilters.type.toLowerCase());
        let matchCity = currentFilters.city === 'all' || (p.location && p.location.toLowerCase().includes(currentFilters.city.toLowerCase()));

        // Price Range Filtering
        let matchPrice = true;
        let numPrice = typeof p.price === 'string' ? parseInt(p.price.replace(/[^0-9]/g, ''), 10) : p.price;
        if (currentFilters.minPrice && numPrice < parseInt(currentFilters.minPrice, 10)) matchPrice = false;
        if (currentFilters.maxPrice && numPrice > parseInt(currentFilters.maxPrice, 10)) matchPrice = false;

        // Bedrooms Filtering
        let matchBeds = true;
        if (currentFilters.beds !== 'any') {
            let minBeds = parseInt(currentFilters.beds, 10);
            let propBeds = parseInt(p.bedrooms, 10);
            matchBeds = propBeds >= minBeds;
        }

        return matchStatus && matchType && matchCity && matchPrice && matchBeds;
    });

    allFilteredProperties = sortProperties(filtered);
    currentListingsPage = 0;
    renderListings();
}

function renderListings() {
    const grid = document.getElementById('listings-grid');
    const countEl = document.getElementById('listings-count');
    const loadMoreBtn = document.getElementById('listings-load-more');
    if (!grid) return;

    if (allFilteredProperties.length === 0) {
        grid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: var(--ink3); padding: 40px; font-weight: 500;">No properties match your filters.</p>';
        if (countEl) countEl.innerText = "0";
        if (loadMoreBtn) loadMoreBtn.style.display = 'none';
        return;
    }

    // Calculate pagination
    const startIndex = currentListingsPage * itemsPerPage;
    const endIndex = startIndex + itemsPerPage;
    const paginatedList = allFilteredProperties.slice(startIndex, endIndex);

    // Build HTML for current page – supports grid and list view modes
    const html = paginatedList.map(p => {
        const displayPrice = typeof p.price === 'number' ? p.price.toLocaleString() : p.price;
        const safeStatus   = p.status ? p.status.toLowerCase() : 'sale';
        const isRent       = safeStatus.includes('rent');
        const realSeller   = (p.seller && p.seller !== "null" && p.seller.trim() !== "") ? p.seller : "Verified Seller";
        const agent        = getAgentForSeller(realSeller);
        const typeLabel    = p.type ? p.type.charAt(0).toUpperCase() + p.type.slice(1) : 'Property';
        const statusTag    = isRent ? 'For Rent' : 'For Sale';
        const tagClass     = isRent ? 'tag-rent' : 'tag-sale';
        const rentSuffix   = isRent ? '<span style="font-size:0.58em;font-weight:400;color:var(--ink4)">/mo</span>' : '';

        if (currentViewMode === 'list') {
            // ── LIST ROW ──
            return `<div class="prop-card prop-card--list" onclick="openDetail('${p.id}')">
                <div class="plc-img">
                    <img src="${p.image}" alt="${p.title}">
                    <span class="prop-tag ${tagClass} plc-tag">${statusTag}</span>
                </div>
                <div class="plc-body">
                    <div class="plc-type">🏠 ${typeLabel}</div>
                    <div class="plc-title">${p.title}</div>
                    <div class="plc-loc">📍 ${p.location}</div>
                    <div class="plc-meta">
                        <span>🛏️ ${p.bedrooms} Beds</span>
                        <span>🛁 ${p.bathrooms} Baths</span>
                        <span>👁️ ${p.views || '—'} Views</span>
                    </div>
                </div>
                <div class="plc-right">
                    <div class="plc-price">$${displayPrice}${rentSuffix}</div>
                    <div class="plc-agent">
                        <img src="${agent.img}" alt="${realSeller}">
                        <span>${realSeller}</span>
                    </div>
                </div>
            </div>`;
        }

        // ── GRID CARD (unchanged) ──
        return `<div class="prop-card" onclick="openDetail('${p.id}')" style="cursor:pointer;">
            <div class="prop-img-wrap">
                <img src="${p.image}" alt="${p.title}">
                <div class="prop-tags">
                    <span class="prop-tag ${tagClass}">${statusTag}</span>
                </div>
            </div>
            <div class="prop-body">
                <div class="prop-price">$${displayPrice} ${rentSuffix}</div>
                <div class="prop-name">${p.title}</div>
                <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px;">
                    <span style="font-size:0.78rem;font-weight:600;color:var(--accent);background:var(--accent-l);padding:3px 10px;border-radius:6px;text-transform:capitalize;">🏠 ${typeLabel}</span>
                </div>
                <div class="prop-loc">📍 ${p.location}</div>
                <div class="prop-divider"></div>
                <div class="prop-meta">
                    <div class="prop-meta-item">🛏️ ${p.bedrooms} Beds</div>
                    <div class="prop-meta-item">🛁 ${p.bathrooms} Baths</div>
                    <div class="prop-meta-item">👁️ ${p.views || '—'} Views</div>
                </div>
                <div class="prop-agent-row">
                    <div class="prop-agent">
                        <img src="${agent.img}" alt="Agent">
                        <div class="prop-agent-name">${realSeller}</div>
                    </div>
                </div>
            </div>
        </div>`;
    }).join('');

    if (startIndex === 0) {
        // First page - replace content
        grid.innerHTML = html;
    } else {
        // Load more - append content
        grid.innerHTML += html;
    }

    if (countEl) countEl.innerText = allFilteredProperties.length;

    // Show/hide Load More button
    if (loadMoreBtn) {
        if (endIndex >= allFilteredProperties.length) {
            loadMoreBtn.style.display = 'none';
        } else {
            loadMoreBtn.style.display = 'flex';
        }
    }
}

function loadMoreListings() {
    currentListingsPage++;
    renderListings();
}

// ── DETAIL PAGE ENGINE ──────────────────────
function openDetail(id) {
    if(typeof window.properties === 'undefined') return;
    const p = window.properties.find(prop => String(prop.id) === String(id));
    if (!p) { console.error("Property not found!"); return; }

    document.getElementById('detail-main-img').src = p.image;
    document.getElementById('detail-title').innerText = p.title;
    document.getElementById('detail-address-text').innerText = p.location;

    const displayPrice = typeof p.price === 'number' ? p.price.toLocaleString() : p.price;
    document.getElementById('detail-price').innerText = "$" + displayPrice;

    const favInput = document.getElementById('fav-property-id');
    if (favInput) favInput.value = String(id);

    // Set description
    const detailDescEl = document.getElementById('detail-desc');
    if (detailDescEl) {
        detailDescEl.innerText = p.description && p.description.trim() !== "" ? p.description : "No Description yet..";
    }

    // Set bedrooms and bathrooms in detail-specs
    const detailSpecsEl = document.getElementById('detail-specs');
    if (detailSpecsEl) {
        detailSpecsEl.innerHTML = `
            <div class="spec-box">
                <div class="spec-icon">🛏️</div>
                <div class="spec-value">${p.bedrooms}</div>
                <div class="spec-label">Bedrooms</div>
            </div>
            <div class="spec-box">
                <div class="spec-icon">🛁</div>
                <div class="spec-value">${p.bathrooms}</div>
                <div class="spec-label">Bathrooms</div>
            </div>
            <div class="spec-box">
                <div class="spec-icon">📐</div>
                <div class="spec-value">1,200</div>
                <div class="spec-label">Sq Ft</div>
            </div>
            <div class="spec-box">
                <div class="spec-icon">🏡</div>
                <div class="spec-value">${p.type}</div>
                <div class="spec-label">Type</div>
            </div>
        `;
    }

    // Face Router Execution for Details Page
    const realSeller = (p.seller && p.seller !== "null" && p.seller.trim() !== "") ? p.seller : "Verified Seller";
    const agent = getAgentForSeller(realSeller);

    document.getElementById('detail-agent-img').src = agent.img;
    if(document.getElementById('detail-agent-name')) document.getElementById('detail-agent-name').innerText = realSeller;

    window.setTimeout(() => {
        if(document.getElementById('inq-prop-id')) document.getElementById('inq-prop-id').value = String(id);
        if(document.getElementById('inq-prop-title')) document.getElementById('inq-prop-title').value = p.title;
        if(document.getElementById('inq-agent-name')) document.getElementById('inq-agent-name').value = realSeller;
    }, 400);

    showPage('detail');
    renderPropertyReviews(id);
    window.scrollTo({ top: 0, behavior: 'smooth' });
} // <--- End of openDetail function

// ── AUTH ROLE SELECTOR ENGINE ─────────────────────────
function selectRole(btn, role) {
    document.querySelectorAll('.role-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
    const hiddenInput = document.getElementById('hiddenRole');
    if(hiddenInput) hiddenInput.value = role;
}

// ── UI HELPERS ───────────────────────────────
function showToast(icon, msg, isError=false) {
    const t = document.getElementById('toast');
    if(!t) return;
    document.getElementById('toast-icon').textContent = icon;
    document.getElementById('toast-msg').textContent = msg;
    t.style.background = isError ? '#c53030' : 'var(--ink)';
    t.classList.add('show');
    window.setTimeout(() => t.classList.remove('show'), 3000);
}

window.addEventListener('scroll', () => {
    const nav = document.getElementById('navbar');
    if(nav) nav.classList.toggle('scrolled', window.scrollY > 20);
});

// ── SYSTEM BOOT ─────────────────────────────────
window.document.addEventListener("DOMContentLoaded", () => {
    initHome();

    // Force the Dark Mode button to listen for clicks safely!
    const themeBtn = window.document.getElementById('theme-toggle');
    if (themeBtn) {
        themeBtn.onclick = toggleTheme;
    }

    // Listen for the main hero search form submission
    const heroSearchForm = document.querySelector('.hero-search-form');
    if (heroSearchForm) {
        heroSearchForm.addEventListener('submit', function(event) {
            event.preventDefault(); // Stop the form from reloading the page
            const locationInput = heroSearchForm.querySelector('input[name="location"]');
            const typeInput = heroSearchForm.querySelector('select[name="type"]');
            if (locationInput && typeInput) {
                searchHomeByCity(locationInput.value, typeInput.value);
            }
        });
    }
});

// ── REVIEWS ENGINE ────────────────────────────
function renderPropertyReviews(propId) {
    if (!window.allReviews) return;
    const filtered = window.allReviews.filter(r => String(r.propId) === String(propId));
    const container = document.getElementById('reviews-container');
    if (!container) return;

    const reviewPropIdInput = document.getElementById('review-prop-id');
    if (reviewPropIdInput) reviewPropIdInput.value = propId;

    container.innerHTML = filtered.length === 0 ?
        "<p style='color: var(--ink4); padding: 10px;'>No reviews yet.</p>" :
        filtered.map(r => {
            const isOwner = (window.currentUser && String(window.currentUser).trim() === String(r.name).trim());
            return `
            <div style="border-bottom: 1px solid var(--line); padding: 20px 0;">
                <div class="rev-header">
                    <div>
                        <strong style="color: var(--ink);">${r.name} ${r.isVerified ? '✅' : ''}</strong>
                        <div style="color: var(--amber); font-size: 0.75rem; margin-top: 2px;">${'★'.repeat(r.rating)}</div>
                    </div>
                    ${isOwner ? `
                    <div class="rev-menu-wrap">
                        <button class="rev-dots-btn" onclick="toggleRevMenu(event, '${r.id}')">⋮</button>
                        <div id="drop-${r.id}" class="rev-dropdown">
                            <button class="rev-drop-item" onclick="editReview('${r.id}', '${propId}')">✏️ Update</button>
                            <button class="rev-drop-item del" onclick="deleteReview('${r.id}', '${propId}')">🗑️ Delete</button>
                        </div>
                    </div>` : ''}
                </div>
                <div id="comment-container-${r.id}">
                    <p style="color: var(--ink3); margin-top: 10px; font-size: 0.95rem; line-height: 1.5;">${r.comment}</p>
                </div>
            </div>`;
        }).join('');
}

function toggleRevMenu(event, revId) {
    event.stopPropagation();
    document.querySelectorAll('.rev-dropdown').forEach(d => d.classList.remove('show'));
    const drop = document.getElementById('drop-' + revId);
    if (drop) drop.classList.toggle('show');
}

function deleteReview(revId, propId) {
    if (window.confirm("Delete this review forever?")) {
        const form = document.createElement('form');
        form.method = 'POST'; form.action = 'reviews';
        form.innerHTML = `
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="reviewId" value="${revId}">
            <input type="hidden" name="propertyId" value="${propId}">
        `;
        document.body.appendChild(form);
        form.submit();
    }
}

function editReview(revId, propId) {
    const container = document.getElementById('comment-container-' + revId);
    if (!container) return;
    const oldText = container.querySelector('p').innerText;

    container.innerHTML = `
        <div style="margin-top: 10px; display: flex; flex-direction: column; gap: 8px;">
            <textarea id="inline-edit-${revId}" class="contact-form-input" style="min-height: 80px; border: 1.5px solid var(--accent); color: var(--ink);">${oldText}</textarea>
            <div style="display: flex; gap: 8px;">
                <button onclick="saveInlineEdit('${revId}', '${propId}')" style="background: var(--accent); color:white; padding:6px 12px; border-radius:6px; font-size:0.75rem; font-weight:600; cursor:pointer;">Save Changes</button>
                <button onclick="window.location.reload()" style="background:none; border:1px solid var(--line); color:var(--ink3); padding:6px 12px; border-radius:6px; font-size:0.75rem; cursor:pointer;">Cancel</button>
            </div>
        </div>
    `;
    const editBox = document.getElementById('inline-edit-' + revId);
    if (editBox) editBox.focus();
}

function saveInlineEdit(revId, propId) {
    const editBox = document.getElementById('inline-edit-' + revId);
    if (!editBox) return;
    const newMsg = editBox.value;

    if (newMsg.trim() === "") {
        window.alert("Feedback cannot be empty!");
        return;
    }

    const form = document.createElement('form');
    form.method = 'POST'; form.action = 'reviews';
    form.innerHTML = `
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="reviewId" value="${revId}">
        <input type="hidden" name="propertyId" value="${propId}">
        <input type="hidden" name="comment" value="${newMsg}">
    `;
    document.body.appendChild(form);
    form.submit();
}

window.onclick = () => document.querySelectorAll('.rev-dropdown').forEach(d => d.classList.remove('show'));

function renderNotifications() {
    if (!window.allNotifications || !window.currentUser) return;

    // 1. Filter: Only show messages where I am the RECEIVER
    const myMail = window.allNotifications.filter(n => n.receiver === window.currentUser);

    // 2. Update the Red Badge
    const badge = document.getElementById('notif-count');
    if (badge) {
        if (myMail.length > 0) {
            badge.innerText = myMail.length;
            badge.style.display = 'block';
        } else {
            badge.style.display = 'none';
        }
    }

    // 3. Build the HTML for the list
    const listContainer = document.getElementById('notif-list');
    if (listContainer) {
        if (myMail.length === 0) {
            listContainer.innerHTML = '<p style="padding: 20px; text-align: center; color: var(--ink4); font-size: 0.85rem;">No new messages</p>';
        } else {
            listContainer.innerHTML = myMail.map(n => `
                <div class="notif-item" onclick="openNotifThread('${n.threadId || ''}')" style="padding: 12px; border-bottom: 1px solid var(--line2); display: flex; align-items: center; gap: 12px; cursor: pointer;">
                    <div style="background: var(--accent-l); width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 1.2rem;">
                        ${n.type === 'INQUIRY' ? '📩' : '💬'}
                    </div>
                    <div style="flex: 1;">
                        <div style="font-size: 0.85rem; font-weight: 700; color: var(--ink);">${n.sender}</div>
                        <div style="font-size: 0.75rem; color: var(--ink3); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px;">
                            ${n.message}
                        </div>
                        <div style="font-size: 0.65rem; color: var(--accent); font-weight: 600; margin-top: 2px;">
                            Re: ${n.property}
                        </div>
                    </div>
                </div>
            `).join('');
        }
    }
}

// ── NOTIFICATION PANEL TOGGLE (shared pages) ──
function toggleNotif(event) {
    if (event && event.stopPropagation) event.stopPropagation();
    const panel = document.getElementById('notif-panel');
    if (!panel) return;
    const isVisible = panel.style.display === 'block';
    panel.style.display = isVisible ? 'none' : 'block';
}

window.addEventListener('click', () => {
    const panel = document.getElementById('notif-panel');
    if (panel) panel.style.display = 'none';
});

function openNotifThread(threadId) {
    if (!threadId) return;

    // If we are already on a dashboard that can open the modal, open directly.
    if (window.currentRole && String(window.currentRole).toUpperCase() === 'SELLER' && typeof window.openChat === 'function') {
        window.openChat(threadId);
        const panel = document.getElementById('notif-panel');
        if (panel) panel.style.display = 'none';
        return;
    }
    if (window.currentRole && String(window.currentRole).toUpperCase() === 'BUYER' && typeof window.openBuyerChat === 'function') {
        window.openBuyerChat(threadId);
        const panel = document.getElementById('notif-panel');
        if (panel) panel.style.display = 'none';
        return;
    }

    // Otherwise route to the right dashboard and auto-open.
    const role = window.currentRole ? String(window.currentRole).toUpperCase() : '';
    if (role === 'SELLER') {
        window.location.href = 'sellerDashboard?threadId=' + encodeURIComponent(threadId);
    } else {
        window.location.href = 'buyerDashboard?threadId=' + encodeURIComponent(threadId);
    }
}

// Run this logic as soon as the page loads
window.document.addEventListener("DOMContentLoaded", renderNotifications);
// ── SORT ─────────────────────────────────
function sortProperties(arr) {
    if (currentSort === 'default') return arr;
    return [...arr].sort((a, b) => {
        const toNum = v => (typeof v === 'string' ? parseInt(v.replace(/[^0-9]/g, ''), 10) : (v || 0));
        if (currentSort === 'price_asc')  return toNum(a.price) - toNum(b.price);
        if (currentSort === 'price_desc') return toNum(b.price) - toNum(a.price);
        if (currentSort === 'newest')     return (parseInt(b.id, 10) || 0) - (parseInt(a.id, 10) || 0);
        if (currentSort === 'views')      return (parseInt(b.views, 10) || 0) - (parseInt(a.views, 10) || 0);
        return 0;
    });
}

function sortListings(value) {
    currentSort = value;
    currentListingsPage = 0;
    applyFilters();   // re-filter + sort + render
}

// ── VIEW TOGGLE ──────────────────────────
function setViewMode(mode) {
    if (currentViewMode === mode) return;
    currentViewMode = mode;
    const grid = document.getElementById('listings-grid');
    if (grid) grid.classList.toggle('prop-grid--list', mode === 'list');
    document.querySelectorAll('.view-btn').forEach((btn, i) => {
        btn.classList.toggle('active', (mode === 'grid' && i === 0) || (mode === 'list' && i === 1));
    });
    currentListingsPage = 0;
    renderListings();
}
