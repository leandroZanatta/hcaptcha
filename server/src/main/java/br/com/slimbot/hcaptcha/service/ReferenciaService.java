package br.com.slimbot.hcaptcha.service;

import java.util.List;

import br.com.slimbot.hcaptcha.dto.ReferenciaUsuarioDTO;
import br.com.slimbot.hcaptcha.dto.ReferenciaUsuarioParametroDTO;

public interface ReferenciaService {

	public String adicionarReferencia(ReferenciaUsuarioParametroDTO referenciaUsuarioParametroDTO);

	public List<ReferenciaUsuarioDTO> obterReferenciaUsuario(String codigoReferencia);

}
