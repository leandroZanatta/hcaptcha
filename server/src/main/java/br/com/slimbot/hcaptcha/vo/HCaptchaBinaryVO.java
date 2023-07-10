package br.com.slimbot.hcaptcha.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class HCaptchaBinaryVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String titulo;
	private String key;
	private List<HCaptchaImageVO> imagens = new ArrayList<>();
}
