package br.com.slimbot.hcaptcha.service;

import java.util.List;

import br.com.slimbot.hcaptcha.dto.DadosCaptchaDTO;
import br.com.slimbot.hcaptcha.vo.HCaptchaBinaryVO;
import br.com.slimbot.hcaptcha.vo.HCaptchaMultipleChoiceVO;
import br.com.slimbot.hcaptcha.vo.LabelImageVO;
import br.com.slimbot.hcaptcha.vo.SiteConfigVO;

public interface HcaptchaApiService {

	public SiteConfigVO obterSiteConfig(DadosCaptchaDTO dadosCaptcha) throws Exception;

	public String obterTask(DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String autorizacao) throws Exception;

	public String resolverTaskClassificacao(HCaptchaBinaryVO task, DadosCaptchaDTO dadosCaptcha,
			SiteConfigVO siteConfig, String autorizacao) throws Exception;

	public String resolverTaskMotion(String key, String entityType, List<LabelImageVO> imagens,
			DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String autorizacao) throws Exception;

	public String resolverTaskMultipleChoice(String asString, List<HCaptchaMultipleChoiceVO> imagens,
			DadosCaptchaDTO dadosCaptcha, SiteConfigVO siteConfig, String obterAutorizacao) throws Exception;
}
