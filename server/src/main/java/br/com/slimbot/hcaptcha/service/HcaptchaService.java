package br.com.slimbot.hcaptcha.service;

import java.util.List;

import br.com.slimbot.hcaptcha.dto.CaptchaDTO;
import br.com.slimbot.hcaptcha.dto.DadosCaptchaDTO;

public interface HcaptchaService {

	String resolve(DadosCaptchaDTO dadosCaptcha);

	List<String> resolverCaptcha(CaptchaDTO captchaDTO);
}
