package br.com.slimbot.hcaptcha.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ReferenciaUsuarioParametroDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String email;

	private String carteira;

	private String urlReferencia;

}
