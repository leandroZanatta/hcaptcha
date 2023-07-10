package br.com.slimbot.hcaptcha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.slimbot.hcaptcha.dto.CaptchaDTO;
import br.com.slimbot.hcaptcha.dto.DadosCaptchaDTO;
import br.com.slimbot.hcaptcha.service.HcaptchaService;

@RestController
@RequestMapping("/api/v1/captcha")
@CrossOrigin()
public class HcaptchaController {

	@Autowired
	private HcaptchaService hcaptchaService;

	@PostMapping("/resolver")
	public ResponseEntity<String> resolve(@RequestBody DadosCaptchaDTO dadosCaptcha) {

		return ResponseEntity.ok(hcaptchaService.resolve(dadosCaptcha));
	}

	@PostMapping()
	public ResponseEntity<List<String>> resolverCaptcha(@RequestBody CaptchaDTO captchaDTO) {

		return ResponseEntity.ok(hcaptchaService.resolverCaptcha(captchaDTO));
	}

}
