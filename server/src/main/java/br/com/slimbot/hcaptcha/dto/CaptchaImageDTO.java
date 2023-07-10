package br.com.slimbot.hcaptcha.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class CaptchaImageDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String imagem;
	private String uuid;

}
