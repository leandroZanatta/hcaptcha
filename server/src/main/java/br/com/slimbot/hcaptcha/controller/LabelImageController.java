package br.com.slimbot.hcaptcha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.slimbot.hcaptcha.dto.LabelImageDTO;
import br.com.slimbot.hcaptcha.service.LabelImageService;

@RestController
@RequestMapping("/api/v1/label-image")
@CrossOrigin(origins = { "*" })
public class LabelImageController {

	@Autowired
	private LabelImageService labelImageService;

	@GetMapping
	public ResponseEntity<List<String>> buscarPastas() {

		return ResponseEntity.ok(labelImageService.buscarPastas());
	}

	@GetMapping(value = "/classificacao")
	public ResponseEntity<List<LabelImageDTO>> buscarClassificacao(@RequestParam(name = "pasta") String pasta) {

		return ResponseEntity.ok(labelImageService.buscarClassificacao(pasta));
	}

	@PostMapping
	public ResponseEntity<Void> salvarClassificacao(@RequestBody LabelImageDTO label) {

		labelImageService.salvarClassificacao(label);

		return ResponseEntity.ok().build();
	}

}
