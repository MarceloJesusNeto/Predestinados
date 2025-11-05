document.addEventListener("DOMContentLoaded", async function () {

     // ========================
     // VARIÁVEIS E FUNÇÕES BASE
     // ========================
     const modal = document.getElementById("modalFilme");
     const poster = document.getElementById("posterFilme");
     const titulo = document.getElementById("tituloFilme");
     const descricao = document.getElementById("descricaoFilme");
     const nota = document.getElementById("notaFilme");
     const fechar = modal.querySelector(".fechar");
     const btnFavoritarModal = document.getElementById("btnFavoritarModal");
     const btnSalvarModal = document.getElementById("btnSalvarModal");
     let filmeAtual = null;
     let favoritosIds = [];
     let salvosIds = [];

     // ========================
     // FUNÇÕES DE FAVORITAR/SALVAR
     // ========================
     window.favoritarFilme = async function (botao) {
       const imdbId = botao.dataset.id;
       const titulo = botao.dataset.titulo;
       const imagem = botao.dataset.imagem;
       const genero = botao.dataset.genero;

       try {
         const resposta = await fetch("/favoritar", {
           method: "POST",
           headers: { "Content-Type": "application/x-www-form-urlencoded" },
           body: `imdbId=${encodeURIComponent(imdbId)}&titulo=${encodeURIComponent(titulo)}&imagem=${encodeURIComponent(imagem)}&genero=${encodeURIComponent(genero)}`
         });

         const resultado = await resposta.text();

         if (resultado === "ADICIONADO") {
           favoritosIds.push(imdbId);
           atualizarEstadoBotaoFavorito(imdbId, true);
         } else if (resultado === "REMOVIDO") {
           favoritosIds = favoritosIds.filter(id => id !== imdbId);
           atualizarEstadoBotaoFavorito(imdbId, false);
         }
       } catch (erro) {
         console.error("Erro ao favoritar:", erro);
       }
     };

     window.salvarFilme = async function (botao) {
       const imdbId = botao.getAttribute("data-id") || "";
       const titulo = botao.getAttribute("data-titulo") || "Título não disponível";
       const imagem = botao.getAttribute("data-imagem") || "/img/placeholder.jpg";
       const genero = botao.getAttribute("data-genero") || "Desconhecido";

       try {
         const resposta = await fetch("/perfil/salvarFilme", {
           method: "POST",
           headers: { "Content-Type": "application/x-www-form-urlencoded" },
           body: new URLSearchParams({
             imdbId: imdbId,
             titulo: titulo,
             imagem: imagem,
             genero: genero
           })
         });

         const resultado = await resposta.text();

         if (resultado === "ADICIONADO") {
           console.log(`✅ Filme salvo: ${titulo}`);
         } else if (resultado === "REMOVIDO") {
           console.log(`❌ Filme removido: ${titulo}`);
         } else {
           console.warn("⚠️ Resposta inesperada:", resultado);
         }
       } catch (erro) {
         console.error("Erro ao salvar:", erro);
       }
     };

     // ========================
     // FUNÇÕES DE ATUALIZAÇÃO VISUAL
     // ========================
     function atualizarEstadoBotaoFavorito(imdbId, ativo) {
       document.querySelectorAll(`[data-id="${imdbId}"].btn-favoritar`).forEach(btn => {
         if (ativo) {
           btn.classList.add("favoritado");
           btn.innerHTML = '<i class="fa-solid fa-heart-circle-check"></i> Favoritado';
         } else {
           btn.classList.remove("favoritado");
           btn.innerHTML = '<i class="fa-solid fa-heart"></i> Favoritar';
         }
       });
     }

     function atualizarEstadoBotaoSalvar(imdbId, ativo) {
       document.querySelectorAll(`[data-id="${imdbId}"].btn-salvar`).forEach(btn => {
         if (ativo) {
           btn.classList.add("salvo");
           btn.innerHTML = '<i class="fa-solid fa-bookmark-circle-check"></i> Salvo';
         } else {
           btn.classList.remove("salvo");
           btn.innerHTML = '<i class="fa-solid fa-bookmark"></i> Salvar';
         }
       });
     }

     // ========================
     // CARREGA ESTADOS AO ENTRAR NA PÁGINA
     // ========================
     try {
       const [respFav, respSalvo] = await Promise.all([
         fetch("/favoritos/ids"),
         fetch("/perfil/salvos/ids")
       ]);

       if (respFav.ok) favoritosIds = await respFav.json();
       if (respSalvo.ok) salvosIds = await respSalvo.json();

       favoritosIds.forEach(id => atualizarEstadoBotaoFavorito(id, true));
       salvosIds.forEach(id => atualizarEstadoBotaoSalvar(id, true));
     } catch (e) {
       console.warn("Usuário não logado ou erro ao carregar favoritos/salvos:", e);
     }

     // ========================
     // ABRE O MODAL AO CLICAR NO CARD
     // ========================
     document.querySelectorAll(".card").forEach(card => {
       card.addEventListener("click", async (e) => {
         if (e.target.closest(".btn-favoritar") || e.target.closest(".btn-salvar")) return;

         const imdbId = card.querySelector(".btn-favoritar")?.dataset.id;
         if (!imdbId) return;

         try {
           const resposta = await fetch(`/detalhes/${imdbId}`);
           const filme = await resposta.json();
           filmeAtual = filme;

           poster.src = filme.imagem || card.querySelector("img").src;
           titulo.textContent = filme.titulo || "Título não disponível";
           descricao.textContent = filme.descricao || "Descrição não disponível.";
           nota.textContent = filme.nota || "—";

           // Atualiza dados dos botões do modal
           [btnFavoritarModal, btnSalvarModal].forEach(btn => {
             btn.dataset.id = imdbId;
             btn.dataset.titulo = filme.titulo;
             btn.dataset.imagem = filme.imagem;
             btn.dataset.genero = filme.genero;
           });

           // Aplica o estado visual atual
           atualizarEstadoBotaoFavorito(imdbId, favoritosIds.includes(imdbId));
           atualizarEstadoBotaoSalvar(imdbId, salvosIds.includes(imdbId));

           modal.style.display = "flex";
           document.body.classList.add("modal-ativo");
         } catch (e) {
           console.error("Erro ao buscar detalhes:", e);
         }
       });
     });

     // ========================
     // BOTÕES DENTRO DO MODAL
     // ========================
     btnFavoritarModal.addEventListener("click", (e) => {
       e.stopPropagation();
       favoritarFilme(btnFavoritarModal);
     });

     btnSalvarModal.addEventListener("click", (e) => {
       e.stopPropagation();
       salvarFilme(btnSalvarModal);
     });

     // Fecha o modal
     fechar.addEventListener("click", () => {
       modal.style.display = "none";
       document.body.classList.remove("modal-ativo");
     });

     modal.addEventListener("click", e => {
       if (e.target === modal) {
         modal.style.display = "none";
         document.body.classList.remove("modal-ativo");
       }
     });
   });

   window.addEventListener("load", () => {
     document.getElementById("loading").style.display = "none";

     });

