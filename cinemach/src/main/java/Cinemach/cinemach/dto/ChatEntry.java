package Cinemach.cinemach.dto;

import Cinemach.cinemach.model.Usuario;

public class ChatEntry {
        private Usuario destinatario; // o "outro" usuário (com quem há chat)

        public ChatEntry() {}

        public ChatEntry(Usuario destinatario) {
            this.destinatario = destinatario;
        }

        public Usuario getDestinatario() {
            return destinatario;
        }

        public void setDestinatario(Usuario destinatario) {
            this.destinatario = destinatario;
        }
    }
