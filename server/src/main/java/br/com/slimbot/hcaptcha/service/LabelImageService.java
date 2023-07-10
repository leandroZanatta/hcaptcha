package br.com.slimbot.hcaptcha.service;

import java.util.List;

import br.com.slimbot.hcaptcha.dto.LabelImageDTO;

public interface LabelImageService {

	List<String> buscarPastas();

	List<LabelImageDTO> buscarClassificacao(String pasta);

	void salvarClassificacao(LabelImageDTO label);

}
