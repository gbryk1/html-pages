// Spring Security (csrf.spa()) zapisuje token w ciasteczku XSRF-TOKEN; odsyłamy go w nagłówku X-XSRF-TOKEN.
function csrfToken() {
  const match = document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/);
  return match ? decodeURIComponent(match[1]) : '';
}

async function api(method, url, body, form) {
  const headers = { 'X-XSRF-TOKEN': csrfToken() };
  if (body && !form) headers['Content-Type'] = 'application/json';
  return fetch(url, { method, headers, body: form ? body : body && JSON.stringify(body) });
}

async function refresh() {
  const me = await fetch('/api/me');
  document.getElementById('notes').hidden = !me.ok;
  document.getElementById('login').hidden = me.ok;
  if (!me.ok) return;
  document.getElementById('who').textContent = (await me.json()).username;
  const notes = await (await fetch('/api/notes')).json();
  const list = document.getElementById('list');
  list.replaceChildren(...notes.map(n => {
    const li = document.createElement('li');
    li.textContent = n.title;       // textContent, nie innerHTML: tytuł nie wykona się jako HTML
    return li;
  }));
}

document.getElementById('login').addEventListener('submit', async e => {
  e.preventDefault();
  await fetch('/api/csrf');          // pierwszy token przed POST /api/login
  const res = await api('POST', '/api/login', new URLSearchParams(new FormData(e.target)), true);
  if (!res.ok) alert('Złe dane logowania');
  refresh();
});

document.getElementById('add').addEventListener('submit', async e => {
  e.preventDefault();
  await api('POST', '/api/notes', { title: e.target.title.value, body: '' });
  e.target.reset();
  refresh();
});

document.getElementById('logout').addEventListener('click', async () => {
  await api('POST', '/api/logout');
  refresh();
});

refresh();
