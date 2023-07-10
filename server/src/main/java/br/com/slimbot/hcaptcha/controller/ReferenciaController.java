package br.com.slimbot.hcaptcha.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.slimbot.hcaptcha.dto.ReferenciaUsuarioDTO;
import br.com.slimbot.hcaptcha.dto.ReferenciaUsuarioParametroDTO;
import br.com.slimbot.hcaptcha.service.ReferenciaService;

@RestController
@RequestMapping("/api/v1/referencia")
@CrossOrigin()
public class ReferenciaController {

	@Autowired
	private ReferenciaService referenciaService;

	@PostMapping()
	public ResponseEntity<String> adicionarReferencia(
			@RequestBody ReferenciaUsuarioParametroDTO referenciaUsuarioParametroDTO) {

		return ResponseEntity.ok(referenciaService.adicionarReferencia(referenciaUsuarioParametroDTO));
	}

	@GetMapping(path = "/{codigoReferencia}")
	public ResponseEntity<List<ReferenciaUsuarioDTO>> obterReferenciaUsuario(
			@PathVariable(value = "codigoReferencia") String codigoReferencia) {

		return ResponseEntity.ok(referenciaService.obterReferenciaUsuario(codigoReferencia));
	}

}
