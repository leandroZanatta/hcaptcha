package br.com.slimbot.hcaptcha.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import br.com.slimbot.hcaptcha.repository.model.Usuario;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

	public Optional<Usuario> findByEmail(String email);
}
