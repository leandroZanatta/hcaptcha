package br.com.slimbot.hcaptcha.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class CaptchaDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String titulo;
	private List<CaptchaImageDTO> imagens;
}
