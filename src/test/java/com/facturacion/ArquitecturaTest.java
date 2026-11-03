package com.facturacion;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.Entity;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
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
                    "..persistence..",
                    "..controller..",
                    "..apln..")
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
    static final ArchRule controladoresSinRepositoryNiServiceInterno = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..domain.repository..", "..service..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aplicacionSinControladores = noClasses()
            .that().resideInAPackage("..apln..")
            .should().dependOnClassesThat().resideInAPackage("..controller..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule modulosSinCiclos = slices()
            .matching("com.facturacion.(*)..")
            .should().beFreeOfCycles()
            .allowEmptyShould(true);
}
