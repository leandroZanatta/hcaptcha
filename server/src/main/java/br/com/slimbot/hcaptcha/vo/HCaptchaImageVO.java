package br.com.slimbot.hcaptcha.vo;

import java.io.File;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HCaptchaImageVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String imagem;
	private File localizacao;
	private String uuid;
	private boolean valido;
}
