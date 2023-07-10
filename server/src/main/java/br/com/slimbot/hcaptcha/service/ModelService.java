package br.com.slimbot.hcaptcha.service;

import java.util.List;

import br.com.slimbot.hcaptcha.dto.ClassificacaoDTO;
import br.com.slimbot.hcaptcha.dto.ModelDTO;
import br.com.slimbot.hcaptcha.dto.ReclassificacaoDTO;

public interface ModelService {

	public void salvarModel(ModelDTO modelDTO);

	public byte[] buscarImagens(String pasta);

	public List<String> buscarPastaImagens();

	public List<String> buscarPastasImagensSemModelo();

	public void limparImagens();

	public List<ClassificacaoDTO> buscarClassificacao(String pasta, String subpasta);

	public void reclassificar(String pasta, ReclassificacaoDTO reclassificacaoDTO);

	public void treinar(String pasta, Long epochs);
}
