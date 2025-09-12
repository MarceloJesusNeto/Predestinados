document.querySelectorAll(".btn-curti").forEach(btn => {
  btn.addEventListener("click", () => {
    alert("Você curtiu este filme!");
  });
});

document.querySelectorAll(".btn-nao").forEach(btn => {
  btn.addEventListener("click", () => {
    alert("Você não curtiu este filme!");
  });
});

let filmesCurtidos = [];


document.querySelectorAll(".btn-curti").forEach(btn => {
  btn.addEventListener("click", (e) => {
    const filme = e.target.closest(".card").querySelector("h3").textContent;
    filmesCurtidos.push(filme);
    localStorage.setItem("curtidos", JSON.stringify(filmesCurtidos));
    alert(`Você curtiu: ${filme}`);
  });
});

document.querySelectorAll(".btn-nao").forEach(btn => {
  btn.addEventListener("click", (e) => {
    const filme = e.target.closest(".card").querySelector("h3").textContent;
    alert(`Você não curtiu: ${filme}`);
  });
});

document.addEventListener("DOMContentLoaded", () => {
  const btnMais = document.getElementById("btnMais");
  if (btnMais) {
    btnMais.addEventListener("click", () => {
      const catalogo = document.getElementById("catalogo");
      const qtdAtual = catalogo.querySelectorAll(".card").length;

      fetch(`/?count=${qtdAtual + 16}`)
        .then(res => res.text())
        .then(html => {
          // pega só os novos cards
          const parser = new DOMParser();
          const doc = parser.parseFromString(html, "text/html");
          const novosCards = doc.querySelectorAll(".catalogo .card");
          const totalNovos = novosCards.length;

          // limpa e adiciona só os que faltam
          catalogo.innerHTML = "";
          novosCards.forEach(c => catalogo.appendChild(c));

          // esconde botão se não tiver mais
          const temMais = doc.querySelector("#btnMais");
          if (!temMais) btnMais.style.display = "none";
        });
    });
  }
});

document.getElementById("btn-pesquisa").addEventListener("click", () => {
  alert("Abrindo pesquisa de filmes...");
});

document.getElementById("btn-chat").addEventListener("click", () => {
  alert("Abrindo chat...");
});

document.getElementById("btn-curtidos").addEventListener("click", () => {
  const curtidos = JSON.parse(localStorage.getItem("curtidos")) || [];
  alert("Seus filmes curtidos: " + curtidos.join(", "));
});

document.getElementById("btn-perfil").addEventListener("click", () => {
  alert("Abrindo seu perfil...");
});
// Mostrar filmes curtidos em curtidos.html
if (document.getElementById("lista-curtidos")) {
  const curtidos = JSON.parse(localStorage.getItem("curtidos")) || [];
  const lista = document.getElementById("lista-curtidos");

  if (curtidos.length > 0) {
    lista.innerHTML = `<ul>${curtidos.map(f => `<li>${f}</li>`).join("")}</ul>`;
  } else {
    lista.innerHTML = "<p>Você ainda não curtiu nenhum filme.</p>";
  }
}
// app.js - gerencia curtidas, curtidos.html, perfil e chat (usa localStorage)
document.addEventListener('DOMContentLoaded', () => {

  const storage = {
    get(key, def) {
      try { return JSON.parse(localStorage.getItem(key)) ?? def; } catch(e){ return def; }
    },
    set(key, val) { localStorage.setItem(key, JSON.stringify(val)); }
  };

  let curtidos = storage.get('curtidos', []);
  let perfil = storage.get('perfil', { name: '', email: '', phone: '' });
  let chatMessages = storage.get('chatMessages', []);

  // -------------------------
  // helpers
  // -------------------------
  function showTempMessage(el, text, ms = 1500) {
    if (!el) return;
    el.textContent = text;
    setTimeout(() => el.textContent = '', ms);
  }

  // -------------------------
  // INDEX: curtir / não curtir (se existirem botões na página)
  // -------------------------
  document.querySelectorAll('.btn-curti').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const card = e.target.closest('.card');
      if (!card) return;
      const title = card.querySelector('h3')?.innerText?.trim() || 'Filme sem título';
      const image = card.querySelector('img')?.src || '';
      const nota = card.querySelector('.nota')?.innerText || '';
      // evita duplicata pelo título
      if (!curtidos.some(f => f.title === title)) {
        curtidos.push({ title, image, nota });
        storage.set('curtidos', curtidos);
        // feedback visual
        e.target.textContent = 'Curtiu ❤️';
        e.target.disabled = true;
        e.target.style.opacity = '0.85';
      } else {
        e.target.textContent = 'Já curtiu';
        e.target.disabled = true;
      }
    });
  });

  document.querySelectorAll('.btn-nao').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const card = e.target.closest('.card');
      const title = card?.querySelector('h3')?.innerText || '';
      // feedback simples
      alert(`Você não curtiu: ${title}`);
    });
  });

  // -------------------------
  // curtidos.html -> renderiza lista
  // -------------------------
  const listaCurtidos = document.getElementById('lista-curtidos');
  if (listaCurtidos) {
    function renderCurtidos() {
      curtidos = storage.get('curtidos', []);
      if (!curtidos || curtidos.length === 0) {
        listaCurtidos.innerHTML = '<p>Você ainda não curtiu nenhum filme.</p>';
        return;
      }
      listaCurtidos.innerHTML = curtidos.map((f, idx) => {
        return `
          <div class="card curtido-card" data-idx="${idx}">
            <img src="${f.image}" alt="${escapeHtml(f.title)}">
            <h3>${escapeHtml(f.title)}</h3>
            <p class="nota">${escapeHtml(f.nota || '')}</p>
            <div class="botoes">
              <button class="btn-nao btn-remove">Remover</button>
            </div>
          </div>
        `;
      }).join('');
    }

    renderCurtidos();

    // delegação para remover
    listaCurtidos.addEventListener('click', (e) => {
      if (e.target.matches('.btn-remove')) {
        const card = e.target.closest('.card');
        const idx = Number(card.dataset.idx);
        if (!isNaN(idx)) {
          curtidos.splice(idx, 1);
          storage.set('curtidos', curtidos);
          renderCurtidos();
        }
      }
    });
  }

  // -------------------------
  // perfil.html -> carregar e salvar perfil
  // -------------------------
  const profileForm = document.getElementById('profile-form');
  if (profileForm) {
    // preencher inputs
    const inName = document.getElementById('perfil-name');
    const inEmail = document.getElementById('perfil-email');
    const inPhone = document.getElementById('perfil-phone');
    const exibeNome = document.getElementById('exibe-nome');
    const exibeEmail = document.getElementById('exibe-email');
    const exibePhone = document.getElementById('exibe-phone');
    const savedMsg = document.getElementById('perfil-saved');
    const resetBtn = document.getElementById('perfil-reset');

    function renderPerfil() {
      perfil = storage.get('perfil', { name:'', email:'', phone:'' });
      inName.value = perfil.name || '';
      inEmail.value = perfil.email || '';
      inPhone.value = perfil.phone || '';
      exibeNome.textContent = perfil.name || '—';
      exibeEmail.textContent = perfil.email || '—';
      exibePhone.textContent = perfil.phone || '—';
    }

    renderPerfil();

    profileForm.addEventListener('submit', (ev) => {
      ev.preventDefault();
      perfil = {
        name: inName.value.trim(),
        email: inEmail.value.trim(),
        phone: inPhone.value.trim()
      };
      storage.set('perfil', perfil);
      renderPerfil();
      showTempMessage(savedMsg, 'Perfil salvo!');
    });

    resetBtn.addEventListener('click', () => {
      if (confirm('Limpar dados do perfil?')) {
        perfil = { name: '', email: '', phone: '' };
        storage.set('perfil', perfil);
        renderPerfil();
      }
    });
  }

  // -------------------------
  // chat.html -> mensagens simples (localStorage)
  // -------------------------
  const chatList = document.getElementById('chat-messages');
  const chatForm = document.getElementById('chat-form');
  if (chatList) {
    function renderChat() {
      chatMessages = storage.get('chatMessages', []);
      if (!chatMessages || chatMessages.length === 0) {
        chatList.innerHTML = '<p class="muted">Sem mensagens ainda. Comece a conversar!</p>';
      } else {
        chatList.innerHTML = chatMessages.map(m => {
          const hora = new Date(m.ts).toLocaleTimeString();
          const cls = m.from === 'me' ? 'message me' : 'message bot';
          return `
            <div class="${cls}">
              <div class="texto">${escapeHtml(m.text)}</div>
              <div class="hora">${hora}</div>
            </div>
          `;
        }).join('');
        chatList.scrollTop = chatList.scrollHeight;
      }
    }

    renderChat();

    if (chatForm) {
      const inp = document.getElementById('chat-input');
      const btnClear = document.getElementById('chat-clear');

      chatForm.addEventListener('submit', (ev) => {
        ev.preventDefault();
        const text = inp.value.trim();
        if (!text) return;
        const msg = { text, ts: Date.now(), from: 'me' };
        chatMessages.push(msg);
        storage.set('chatMessages', chatMessages);
        inp.value = '';
        renderChat();
        // resposta automática simulada
        setTimeout(() => {
          const reply = { text: `Resposta automática: recebi "${text}"`, ts: Date.now(), from: 'bot' };
          chatMessages.push(reply);
          storage.set('chatMessages', chatMessages);
          renderChat();
        }, 700);
      });

      btnClear.addEventListener('click', () => {
        if (!confirm('Limpar todo o chat?')) return;
        chatMessages = [];
        storage.set('chatMessages', chatMessages);
        renderChat();
      });
    }
  }

  // -------------------------
  // util
  // -------------------------
  function escapeHtml(s) {
    if (!s) return '';
    return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

});
