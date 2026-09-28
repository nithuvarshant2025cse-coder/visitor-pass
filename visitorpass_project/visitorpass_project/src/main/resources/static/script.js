const $ = id => document.getElementById(id);
let flats = [], residents = [], approvals = [];

/* ---------- helpers ---------- */
async function api(url, method = 'GET', body) {
  const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' },
    body: body ? JSON.stringify(body) : undefined });
  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) { /* not json */ }
  if (!res.ok) throw new Error((data && data.message) || 'Request failed (' + res.status + ')');
  return data;
}
let toastTimer;
function toast(msg, ok = true) {
  const t = $('toast'); t.textContent = msg; t.className = ok ? 'ok' : 'err'; t.style.display = 'block';
  clearTimeout(toastTimer); toastTimer = setTimeout(() => t.style.display = 'none', 3500);
}
async function run(fn) { try { await fn(); } catch (e) { toast(e.message, false); } }
const fmt = s => s ? s.replace('T', ' ').slice(0, 16) : '-';
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

/* ---------- tabs ---------- */
document.querySelectorAll('.tab').forEach(b => b.onclick = () => {
  document.querySelectorAll('.tab').forEach(x => x.classList.remove('active'));
  document.querySelectorAll('.panel').forEach(x => x.classList.remove('active'));
  b.classList.add('active'); $(b.dataset.tab).classList.add('active');
  run(() => ({ flats: loadFlats, residents: loadResidents, approvals: loadApprovals, gate: loadLog })[b.dataset.tab]());
});

/* ---------- FLATS ---------- */
async function loadFlats() {
  flats = await api('/api/flats');
  $('flatRows').innerHTML = flats.map(f => `<tr><td>${f.id}</td><td>${esc(f.block)}</td><td>${esc(f.flatNumber)}</td>
    <td><button class="small" onclick="editFlat(${f.id})">Edit</button>
    <button class="small danger" onclick="delFlat(${f.id})">Delete</button></td></tr>`).join('')
    || '<tr><td colspan="4">No flats yet</td></tr>';
  $('resFlat').innerHTML = '<option value="">Select flat</option>' +
    flats.map(f => `<option value="${f.id}">${esc(f.block)}-${esc(f.flatNumber)}</option>`).join('');
}
function resetFlat() { $('flatId').value = $('flatBlock').value = $('flatNumber').value = '';
  $('flatSave').textContent = 'Add Flat'; $('flatCancel').classList.add('hidden'); }
window.editFlat = id => { const f = flats.find(x => x.id === id);
  $('flatId').value = f.id; $('flatBlock').value = f.block; $('flatNumber').value = f.flatNumber;
  $('flatSave').textContent = 'Update Flat'; $('flatCancel').classList.remove('hidden'); };
window.delFlat = id => confirm('Delete this flat?') && run(async () => { await api('/api/flats/' + id, 'DELETE');
  toast('Flat deleted'); await loadFlats(); });
$('flatCancel').onclick = resetFlat;
$('flatSave').onclick = () => run(async () => {
  const body = { block: $('flatBlock').value, flatNumber: $('flatNumber').value }, id = $('flatId').value;
  await api(id ? '/api/flats/' + id : '/api/flats', id ? 'PUT' : 'POST', body);
  toast(id ? 'Flat updated' : 'Flat added'); resetFlat(); await loadFlats();
});

/* ---------- RESIDENTS ---------- */
async function loadResidents() {
  await loadFlats();
  residents = await api('/api/residents');
  $('resRows').innerHTML = residents.map(r => `<tr><td>${r.id}</td><td>${esc(r.name)}</td><td>${esc(r.phone)}</td>
    <td>${esc(r.email)}</td><td>${esc(r.flat.block)}-${esc(r.flat.flatNumber)}</td>
    <td><button class="small" onclick="editRes(${r.id})">Edit</button>
    <button class="small danger" onclick="delRes(${r.id})">Delete</button></td></tr>`).join('')
    || '<tr><td colspan="6">No residents yet</td></tr>';
  $('apResident').innerHTML = '<option value="">Select resident</option>' +
    residents.map(r => `<option value="${r.id}">${esc(r.name)} (${esc(r.flat.block)}-${esc(r.flat.flatNumber)})</option>`).join('');
}
function resetRes() { ['resId', 'resName', 'resPhone', 'resEmail', 'resFlat'].forEach(i => $(i).value = '');
  $('resSave').textContent = 'Add Resident'; $('resCancel').classList.add('hidden'); }
window.editRes = id => { const r = residents.find(x => x.id === id);
  $('resId').value = r.id; $('resName').value = r.name; $('resPhone').value = r.phone;
  $('resEmail').value = r.email; $('resFlat').value = r.flat.id;
  $('resSave').textContent = 'Update Resident'; $('resCancel').classList.remove('hidden'); };
window.delRes = id => confirm('Delete this resident?') && run(async () => { await api('/api/residents/' + id, 'DELETE');
  toast('Resident deleted'); await loadResidents(); });
$('resCancel').onclick = resetRes;
$('resSave').onclick = () => run(async () => {
  const id = $('resId').value;
  const body = { name: $('resName').value, phone: $('resPhone').value, email: $('resEmail').value,
    flatId: $('resFlat').value ? Number($('resFlat').value) : null };
  await api(id ? '/api/residents/' + id : '/api/residents', id ? 'PUT' : 'POST', body);
  toast(id ? 'Resident updated' : 'Resident added'); resetRes(); await loadResidents();
});

/* ---------- PRE-APPROVALS ---------- */
async function loadApprovals() {
  await loadResidents();
  approvals = await api('/api/approvals');
  renderApprovals();
}
function renderApprovals() {
  const f = $('apFilter').value, list = approvals.filter(a => !f || a.status === f);
  $('apRows').innerHTML = list.map(a => `<tr><td>${a.id}</td><td>${esc(a.residentName)} (${esc(a.flat)})</td>
    <td>${esc(a.visitorName)}<br><small>${esc(a.visitorPhone)}</small></td><td>${esc(a.purpose)}</td>
    <td>${fmt(a.expectedStart)}<br>to ${fmt(a.expectedEnd)}</td><td class="otp">${esc(a.otpCode)}</td>
    <td><span class="badge ${a.status}">${a.status}</span></td>
    <td>${a.status === 'PENDING' ? `<button class="small" onclick="editAp(${a.id})">Edit</button>
      <button class="small warn" onclick="revokeAp(${a.id})">Revoke</button>` : ''}
      <button class="small danger" onclick="delAp(${a.id})">Delete</button></td></tr>`).join('')
    || '<tr><td colspan="8">No pre-approvals</td></tr>';
}
function resetAp() { ['apId', 'apResident', 'apName', 'apPhone', 'apPurpose', 'apStart', 'apEnd'].forEach(i => $(i).value = '');
  $('apResident').disabled = false; $('apSave').textContent = 'Pre-Approve & Generate OTP'; $('apCancel').classList.add('hidden'); }
window.editAp = id => { const a = approvals.find(x => x.id === id);
  $('apId').value = a.id; $('apResident').value = a.residentId; $('apResident').disabled = true;
  $('apName').value = a.visitorName; $('apPhone').value = a.visitorPhone; $('apPurpose').value = a.purpose;
  $('apStart').value = a.expectedStart.slice(0, 16); $('apEnd').value = a.expectedEnd.slice(0, 16);
  $('apSave').textContent = 'Update Pre-Approval'; $('apCancel').classList.remove('hidden'); window.scrollTo(0, 0); };
window.revokeAp = id => confirm('Revoke this pre-approval?') && run(async () => {
  await api('/api/approvals/' + id + '/revoke', 'PUT'); toast('Pre-approval revoked'); await loadApprovals(); });
window.delAp = id => confirm('Delete this pre-approval?') && run(async () => {
  await api('/api/approvals/' + id, 'DELETE'); toast('Pre-approval deleted'); await loadApprovals(); });
$('apCancel').onclick = resetAp;
$('apFilter').onchange = renderApprovals;
$('apRefresh').onclick = () => run(loadApprovals);
$('apSave').onclick = () => run(async () => {
  const id = $('apId').value;
  const body = { residentId: $('apResident').value ? Number($('apResident').value) : null,
    visitorName: $('apName').value, visitorPhone: $('apPhone').value, purpose: $('apPurpose').value,
    expectedStart: $('apStart').value || null, expectedEnd: $('apEnd').value || null };
  const res = await api(id ? '/api/approvals/' + id : '/api/approvals', id ? 'PUT' : 'POST', body);
  toast(id ? 'Pre-approval updated' : 'Pre-approved! OTP: ' + res.otpCode); resetAp(); await loadApprovals();
});

/* ---------- GATE ---------- */
async function loadLog() {
  const log = await api('/api/gate/entries');
  $('logRows').innerHTML = log.map(a => `<tr><td>${fmt(a.entryTime)}</td><td>${esc(a.visitorName)}</td>
    <td>${esc(a.visitorPhone)}</td><td>${esc(a.residentName)}</td><td>${esc(a.flat)}</td><td>${esc(a.purpose)}</td></tr>`).join('')
    || '<tr><td colspan="6">No entries logged yet</td></tr>';
}
$('otpValidate').onclick = async () => {
  const box = $('gateResult');
  try {
    const a = await api('/api/gate/validate', 'POST', { otp: $('otpInput').value.trim() });
    box.innerHTML = `<div class="result ok">✅ <b>ENTRY ALLOWED</b> - ${esc(a.visitorName)} visiting ${esc(a.residentName)}
      (Flat ${esc(a.flat)}). Entry logged at ${fmt(a.entryTime)}.</div>`;
    $('otpInput').value = ''; await loadLog();
  } catch (e) {
    box.innerHTML = `<div class="result err">❌ <b>ENTRY DENIED</b> - ${esc(e.message)}</div>`;
  }
};
$('otpInput').addEventListener('keydown', e => e.key === 'Enter' && $('otpValidate').click());

run(loadFlats);
