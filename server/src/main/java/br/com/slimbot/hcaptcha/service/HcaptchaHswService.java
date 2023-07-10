package br.com.slimbot.hcaptcha.service;

import java.io.IOException;

import br.com.slimbot.hcaptcha.vo.HcaptchaConfigVO;

public interface HcaptchaHswService {

	public String obterHSW(HcaptchaConfigVO hcaptchaConfigVO) throws IOException;

}
