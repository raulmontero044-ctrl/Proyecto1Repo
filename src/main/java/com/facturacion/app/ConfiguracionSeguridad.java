package com.facturacion.app;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(PropiedadesJwt.class)
public class ConfiguracionSeguridad {

    public static final String CLAIM_ROL = "rol";

    private static final String ADMINISTRADOR = "ADMINISTRADOR";
    private static final String GESTOR = "GESTOR";
    private static final String CONSULTA = "CONSULTA";

    private static final String[] OPERACIONES = {"/api/facturas/**", "/api/pagos/**", "/api/documentos/**"};
    private static final String[] MAESTROS = {"/api/terceros/**", "/api/empresas/**"};
    private static final String[] PUBLICAS = {"/api/auth/login", "/api/auth/refresh", "/api/auth/logout"};

    @Bean
    public JwtEncoder jwtEncoder(PropiedadesJwt propiedades) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave(propiedades)));
    }

    @Bean
    public JwtDecoder jwtDecoder(PropiedadesJwt propiedades, Clock reloj) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave(propiedades))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        JwtTimestampValidator caducidad = new JwtTimestampValidator(Duration.ZERO);
        caducidad.setClock(reloj);
        decoder.setJwtValidator(caducidad);
        return decoder;
    }

    @Bean
    public SecurityFilterChain cadenaSeguridad(
            HttpSecurity http, JwtAuthenticationConverter conversorAutenticacion) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(peticiones -> peticiones
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(rutas(HttpMethod.POST, PUBLICAS)).permitAll()
                        .requestMatchers(rutas(HttpMethod.GET, OPERACIONES)).hasAnyRole(ADMINISTRADOR, GESTOR, CONSULTA)
                        .requestMatchers(rutas(null, OPERACIONES)).hasAnyRole(ADMINISTRADOR, GESTOR)
                        .requestMatchers(rutas(HttpMethod.GET, MAESTROS)).hasAnyRole(ADMINISTRADOR, GESTOR, CONSULTA)
                        .requestMatchers(rutas(null, MAESTROS)).hasRole(ADMINISTRADOR)
                        .requestMatchers(rutas(HttpMethod.GET, "/api/informes/**"))
                        .hasAnyRole(ADMINISTRADOR, GESTOR, CONSULTA)
                        .requestMatchers(rutas(null, "/api/usuarios/**")).hasRole(ADMINISTRADOR)
                        .anyRequest().hasRole(ADMINISTRADOR))
                .oauth2ResourceServer(servidor -> servidor
                        .bearerTokenResolver(tokenFueraDeLasRutasPublicas())
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorAutenticacion)))
                .build();
    }

    private static BearerTokenResolver tokenFueraDeLasRutasPublicas() {
        RequestMatcher publicas = new OrRequestMatcher(rutas(HttpMethod.POST, PUBLICAS));
        DefaultBearerTokenResolver porDefecto = new DefaultBearerTokenResolver();
        return peticion -> publicas.matches(peticion) ? null : porDefecto.resolve(peticion);
    }

    private static RequestMatcher[] rutas(HttpMethod metodo, String... patrones) {
        PathPatternRequestMatcher.Builder constructor = PathPatternRequestMatcher.withDefaults();
        RequestMatcher[] matchers = new RequestMatcher[patrones.length];
        for (int indice = 0; indice < patrones.length; indice++) {
            matchers[indice] = constructor.matcher(metodo, patrones[indice]);
        }
        return matchers;
    }

    @Bean
    public JwtAuthenticationConverter conversorAutenticacion() {
        JwtGrantedAuthoritiesConverter autoridades = new JwtGrantedAuthoritiesConverter();
        autoridades.setAuthoritiesClaimName(CLAIM_ROL);
        autoridades.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(autoridades);
        return conversor;
    }

    private static SecretKey clave(PropiedadesJwt propiedades) {
        return new SecretKeySpec(propiedades.secreto().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
