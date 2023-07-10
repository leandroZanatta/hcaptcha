package br.com.slimbot.hcaptcha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.slimbot.hcaptcha.dto.ClassificacaoDTO;
import br.com.slimbot.hcaptcha.dto.ModelDTO;
import br.com.slimbot.hcaptcha.dto.ReclassificacaoDTO;
import br.com.slimbot.hcaptcha.service.ModelService;

@RestController
@RequestMapping("/api/v1/model")
@CrossOrigin()
public class ModelController {

	@Autowired
	private ModelService modelService;

	@PostMapping("/")
	public ResponseEntity<Void> saveModel(@RequestBody ModelDTO modelDTO) {

		modelService.salvarModel(modelDTO);

		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/pastas")
	public ResponseEntity<List<String>> buscarPastasImagens() {

		return ResponseEntity.ok(modelService.buscarPastaImagens());
	}

	@GetMapping(value = "/limpar-imagens")
	public ResponseEntity<Void> limparImagens() {

		modelService.limparImagens();

		return ResponseEntity.ok().build();
	}

	@GetMapping(value = "/pastassemmodelo")
	public ResponseEntity<List<String>> buscarPastasImagensSemModelo() {

		return ResponseEntity.ok(modelService.buscarPastasImagensSemModelo());
	}

	@GetMapping(value = "/images", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<byte[]> buscarImagens(@RequestParam(name = "pasta") String pasta) {

		byte[] zipBytes = modelService.buscarImagens(pasta);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", pasta + ".zip");
		headers.setContentLength(zipBytes.length);

		return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);

	}

	@GetMapping(value = "/classificacao")
	public ResponseEntity<List<ClassificacaoDTO>> buscarClassificacao(@RequestParam(name = "pasta") String pasta,
			@RequestParam(name = "subpasta") String subpasta) {

		return new ResponseEntity<>(modelService.buscarClassificacao(pasta, subpasta), HttpStatus.OK);
	}

	@PostMapping(value = "/reclassificar/{pasta}")
	public ResponseEntity<Void> reclassificar(@PathVariable(name = "pasta") String pasta,
			@RequestBody ReclassificacaoDTO reclassificacaoDTO) {

		modelService.reclassificar(pasta, reclassificacaoDTO);

		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/treinar/{pasta}/epochs/{epochs}")
	public ResponseEntity<Void> treinar(@PathVariable(name = "pasta") String pasta,
			@PathVariable(name = "epochs") Long epochs) {

		modelService.treinar(pasta, epochs);

		return ResponseEntity.ok().build();
	}

}
