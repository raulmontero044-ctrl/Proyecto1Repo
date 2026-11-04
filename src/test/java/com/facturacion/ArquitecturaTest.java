package com.facturacion;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @ArchTest
    static final ArchRule dominioIndependienteDeFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "org.hibernate..",
                    "jakarta.validation..",
                    "com.fasterxml.jackson..",
                    "..persistence..",
                    "..controller..",
                    "..apln..",
                    "..service..",
                    "..config..",
                    "..api..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule entidadesSoloEnPersistencia = classes()
            .that().areAnnotatedWith(Entity.class)
            .should().resideInAPackage("..persistence.entity..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule persistenciaNoUsadaFueraDePersistencia = noClasses()
            .that().resideOutsideOfPackage("..persistence..")
            .should().dependOnClassesThat().resideInAPackage("..persistence..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule persistenciaSoloDependeDelDominio = noClasses()
            .that().resideInAPackage("..persistence..")
            .should().dependOnClassesThat().resideInAnyPackage("..apln..", "..controller..", "..service..", "..api..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule daoSoloUsadoPorImplementacionesDeRepository = classes()
            .that().resideInAPackage("..persistence.dao..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage("..persistence", "..persistence.dao..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dtoSonRecords = classes()
            .that().resideInAPackage("..api.dto..")
            .should().beRecords()
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dtoSoloDependenDeJavaValidacionYOtrosDto = classes()
            .that().resideInAPackage("..api.dto..")
            .should().onlyDependOnClassesThat().resideInAnyPackage("java..", "jakarta.validation..", "..api.dto..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controladoresSinRepositoryNiServiceInterno = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..domain.repository..", "..service..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule controladoresSoloUsanExcepcionesDelDominio = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat(resideInAPackage("..domain..").and(not(assignableTo(RuntimeException.class))))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aplicacionSinControladores = noClasses()
            .that().resideInAPackage("..apln..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule transaccionesSoloEnAplicacion = noClasses()
            .that().resideOutsideOfPackage("..apln..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework.transaction..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule casosDeUsoTransaccionales = methods()
            .that().arePublic()
            .and().areDeclaredInClassesThat().resideInAPackage("..apln..")
            .and().areDeclaredInClassesThat().areAnnotatedWith(Service.class)
            .should().beAnnotatedWith(Transactional.class)
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule casosDeUsoSinModelosNiEntidadesEnSuFirma = methods()
            .that().arePublic()
            .and().areDeclaredInClassesThat().resideInAPackage("..apln..")
            .and().areDeclaredInClassesThat().arePublic()
            .should().notHaveRawReturnType(resideInAnyPackage("..domain..", "..persistence.."))
            .andShould().notHaveRawParameterTypes(describe(
                    "algún parámetro de dominio o persistencia",
                    tipos -> tipos.stream().anyMatch(resideInAnyPackage("..domain..", "..persistence..")::test)))
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule modulosSinCiclos = slices()
            .matching("com.facturacion.(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);
}
