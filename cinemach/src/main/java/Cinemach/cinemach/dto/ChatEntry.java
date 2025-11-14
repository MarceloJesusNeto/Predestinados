package Cinemach.cinemach.dto;

import Cinemach.cinemach.model.Usuario;

public class ChatEntry {
    private Usuario destinatario;
    private String ultimaMensagem;
    private boolean naoLida;

    public ChatEntry() {}

    public ChatEntry(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public ChatEntry(Usuario destinatario, String ultimaMensagem, boolean naoLida) {
        this.destinatario = destinatario;
        this.ultimaMensagem = ultimaMensagem;
        this.naoLida = naoLida;
    }


    public Usuario getDestinatario() { return destinatario; }
    public void setDestinatario(Usuario destinatario) { this.destinatario = destinatario; }

    public String getUltimaMensagem() { return ultimaMensagem; }
    public void setUltimaMensagem(String ultimaMensagem) { this.ultimaMensagem = ultimaMensagem; }

    public boolean isNaoLida() { return naoLida; }
    public void setNaoLida(boolean naoLida) { this.naoLida = naoLida; }
}
