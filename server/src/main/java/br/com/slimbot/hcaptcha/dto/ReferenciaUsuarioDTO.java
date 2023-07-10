package br.com.slimbot.hcaptcha.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReferenciaUsuarioDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String carteira;

	private String referencia;
}
