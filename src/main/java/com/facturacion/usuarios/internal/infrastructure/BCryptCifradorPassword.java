package com.facturacion.usuarios.internal.infrastructure;

import com.facturacion.usuarios.internal.domain.model.PasswordEnClaro;
import com.facturacion.usuarios.internal.domain.service.CifradorPassword;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BCryptCifradorPassword implements CifradorPassword {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String cifrar(PasswordEnClaro password) {
        return encoder.encode(password.valor());
    }

    @Override
    public boolean coincide(String passwordEnClaro, String passwordHash) {
        if (passwordEnClaro == null || passwordHash == null) {
            return false;
        }
        return encoder.matches(passwordEnClaro, passwordHash);
    }
}
