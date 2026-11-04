package com.facturacion;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.tngtech.archunit.base.DescribedPredicate.describe;
import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.facturacion", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaTest {

    private static final String RAIZ = "com.facturacion.";
    private static final String CONFIGURACION_TRANSVERSAL = "config";

    @ArchTest
    static final ArchRule dominioIndependienteDeFrameworks = noClasses()
            .that().resideInAPackage("..internal.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "jakarta.validation..",
                    "com.fasterxml.jackson..",
                    "..persistence..",
                    "..internal.application..",
                    "..config..",
                    "..api..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule entidadesSoloEnPersistencia = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..internal.persistence.entity..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule persistenciaNoUsadaFueraDePersistencia = noClasses()
            .that().resideOutsideOfPackage("..internal.persistence..")
            .should().dependOnClassesThat().resideInAPackage("..internal.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule persistenciaSoloDependeDelDominio = noClasses()
            .that().resideInAPackage("..internal.persistence..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..internal.application..",
                    "..api..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule daoSoloUsadoPorImplementacionesDeRepository = classes()
            .that().resideInAPackage("..internal.persistence.dao..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage(
                    "..internal.persistence.repository..",
                    "..internal.persistence.dao..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dtoSonRecords = classes()
            .that().resideInAnyPackage("..api.facade.dto..", "..api.http.dto..")
            .should().beRecords()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dtoDeContratoSoloDependenDeJavaYOtrosDtoDeContrato = classes()
            .that().resideInAPackage("..api.facade.dto..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "..api.facade.dto..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dtoHttpSoloDependenDeJavaValidacionYOtrosDtoHttp = classes()
            .that().resideInAPackage("..api.http.dto..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "jakarta.validation..", "..api.http.dto..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controladoresSinRepositoryNiServiceInterno = noClasses()
            .that().resideInAPackage("..api.http..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..internal.domain.repository..",
                    "..internal.domain.service..",
                    "..internal.persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controladoresSoloUsanExcepcionesDelDominio = noClasses()
            .that().resideInAPackage("..api.http..")
            .should().dependOnClassesThat(
                    resideInAPackage("..internal.domain..").and(not(assignableTo(RuntimeException.class))))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aplicacionSinControladores = noClasses()
            .that().resideInAPackage("..internal.application..")
            .should().dependOnClassesThat().resideInAPackage("..api.http..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule transaccionesSoloEnAplicacion = noClasses()
            .that().resideOutsideOfPackage("..internal.application..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.transaction..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule casosDeUsoTransaccionales = methods()
            .that().arePublic()
            .and().areDeclaredInClassesThat().resideInAPackage("..internal.application..")
            .and().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
            .should().beAnnotatedWith(Transactional.class)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule casosDeUsoSinModelosNiEntidadesEnSuFirma = methods()
            .that().arePublic()
            .and().areDeclaredInClassesThat().resideInAPackage("..internal.application..")
            .and().areDeclaredInClassesThat().arePublic()
            .should().notHaveRawReturnType(resideInAnyPackage("..internal.domain..", "..internal.persistence.."))
            .andShould().notHaveRawParameterTypes(describe(
                    "algún parámetro de dominio o persistencia",
                    tipos -> tipos.stream()
                            .anyMatch(resideInAnyPackage("..internal.domain..", "..internal.persistence..")::test)))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule modulosSinCiclos = slices()
            .matching("com.facturacion.(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule modulosSoloUsanLaFacadeDeOtrosDesdeAplicacion = classes()
            .that().resideInAPackage("com.facturacion.*..")
            .should(accederAOtrosModulosSoloDesdeAplicacionYPorSuFacade())
            .allowEmptyShould(true);

    private static ArchCondition<JavaClass> accederAOtrosModulosSoloDesdeAplicacionYPorSuFacade() {
        return new ArchCondition<>("acceder a otros módulos solo desde internal.application y solo a su api.facade") {
            @Override
            public void check(JavaClass origen, ConditionEvents eventos) {
                Optional<String> moduloOrigen = modulo(origen);
                if (moduloOrigen.isEmpty()) {
                    return;
                }
                for (Dependency dependencia : origen.getDirectDependenciesFromSelf()) {
                    JavaClass destino = dependencia.getTargetClass().getBaseComponentType();
                    Optional<String> moduloDestino = modulo(destino);
                    if (moduloDestino.isEmpty() || moduloDestino.equals(moduloOrigen)) {
                        continue;
                    }
                    boolean permitida = estaEnCapa(origen, moduloOrigen.get(), "internal.application")
                            && estaEnCapa(destino, moduloDestino.get(), "api.facade");
                    if (!permitida) {
                        eventos.add(SimpleConditionEvent.violated(dependencia, dependencia.getDescription()));
                    }
                }
            }
        };
    }

    private static Optional<String> modulo(JavaClass clase) {
        String paquete = clase.getPackageName();
        if (!paquete.startsWith(RAIZ)) {
            return Optional.empty();
        }
        String modulo = paquete.substring(RAIZ.length()).split("\\.")[0];
        return modulo.equals(CONFIGURACION_TRANSVERSAL) ? Optional.empty() : Optional.of(modulo);
    }

    private static boolean estaEnCapa(JavaClass clase, String modulo, String... capas) {
        String paquete = clase.getPackageName();
        for (String capa : capas) {
            String paqueteCapa = RAIZ + modulo + "." + capa;
            if (paquete.equals(paqueteCapa) || paquete.startsWith(paqueteCapa + ".")) {
                return true;
            }
        }
        return false;
    }
}
