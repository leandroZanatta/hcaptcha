package br.com.slimbot.hcaptcha.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.slimbot.hcaptcha.dto.ReferenciaUsuarioDTO;
import br.com.slimbot.hcaptcha.dto.ReferenciaUsuarioParametroDTO;
import br.com.slimbot.hcaptcha.repository.UsuarioRepository;
import br.com.slimbot.hcaptcha.repository.model.ReferenciaUsuario;
import br.com.slimbot.hcaptcha.repository.model.Usuario;
import br.com.slimbot.hcaptcha.service.ReferenciaService;

@Service
public class ReferenciaServiceImpl implements ReferenciaService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public String adicionarReferencia(ReferenciaUsuarioParametroDTO referenciaUsuarioParametroDTO) {

		Usuario usuario = obterUsuario(referenciaUsuarioParametroDTO.getEmail());

		ReferenciaUsuario referenciaUsuario = obterReferenciaUsuario(usuario,
				referenciaUsuarioParametroDTO.getCarteira());

		referenciaUsuario.setReferencia(referenciaUsuarioParametroDTO.getUrlReferencia());

		usuarioRepository.save(usuario);

		return usuario.getId().toString();
	}

	@Override
	public List<ReferenciaUsuarioDTO> obterReferenciaUsuario(String codigoReferencia) {

		Usuario usuario = usuarioRepository.findById(Long.valueOf(codigoReferencia))
				.orElseThrow(() -> new RuntimeException("Não foi possível obter as referencias pelo código informado"));

		return usuario
				.getReferenciaUsuarios().stream().map(item -> ReferenciaUsuarioDTO.builder()
						.carteira(item.getCarteira()).referencia(item.getReferencia()).build())
				.collect(Collectors.toList());
	}

	private ReferenciaUsuario obterReferenciaUsuario(Usuario usuario, String carteira) {

		Optional<ReferenciaUsuario> optionalReferenciaUsuario = usuario.getReferenciaUsuarios().stream()
				.filter(item -> item.getCarteira().equals(carteira)).findFirst();

		if (optionalReferenciaUsuario.isPresent()) {
			return optionalReferenciaUsuario.get();
		}

		ReferenciaUsuario novaReferenciaUsuario = new ReferenciaUsuario();
		novaReferenciaUsuario.setUsuario(usuario);
		novaReferenciaUsuario.setCarteira(carteira);

		usuario.getReferenciaUsuarios().add(novaReferenciaUsuario);

		return novaReferenciaUsuario;
	}

	private Usuario obterUsuario(String email) {

		Optional<Usuario> optional = usuarioRepository.findByEmail(email);

		if (optional.isPresent()) {
			return optional.get();
		}

		Usuario usuario = new Usuario();
		usuario.setEmail(email);

		return usuario;
	}

}
