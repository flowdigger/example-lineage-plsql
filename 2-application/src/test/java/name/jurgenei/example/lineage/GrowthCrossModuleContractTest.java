package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrowthCrossModuleContractTest {

    @Test
    void shouldDefineArchimateExportContract() throws Exception {
        Path root = ProjectRootPaths.root();
        String architectureBuild = Files.readString(root.resolve(Path.of("1-architecture", "build.gradle")), StandardCharsets.UTF_8);
        assertThat(architectureBuild).contains("assembleArchitectureModel");
        assertThat(architectureBuild).contains("name.jurgenei.gradle.archi");
        assertThat(architectureBuild).contains("name.jurgenei.gradle.xml");
        assertThat(architectureBuild).contains("test-lineage.export.xml");
        assertThat(architectureBuild).contains("test-lineage.xir");
        assertThat(architectureBuild).contains("xirFormat.set(\"beautified\")");
        assertThat(architectureBuild).contains("test-lineage.xlsx");
        assertThat(architectureBuild).contains("archi-export/pdf");
        assertThat(architectureBuild).contains("tasks.register('assembleArchitecture')");
    }

    @Test
    void shouldDefineLineageContractAgainstApplicationAssets() throws Exception {
        Path root = ProjectRootPaths.root();
        String applicationBuild = Files.readString(root.resolve(Path.of("2-application", "build.gradle")), StandardCharsets.UTF_8);
        String lineageBuild = Files.readString(root.resolve(Path.of("3-lineage", "build.gradle")), StandardCharsets.UTF_8);
        String lineagePipeline = Files.readString(root.resolve(Path.of("3-lineage", "lineage-pipeline.gradle")), StandardCharsets.UTF_8);
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        String settings = Files.readString(root.resolve("settings.gradle"), StandardCharsets.UTF_8);
        assertThat(applicationBuild).contains("prepareApplicationAssets");
        assertThat(applicationBuild).contains("application-assets.txt");
        assertThat(lineageBuild).contains("dependsOn(':publishLineage')");
        assertThat(lineageBuild).doesNotContain("XmlAstPlsqlGradleTask", "XsltTask");
        assertThat(settings).contains("id 'org.flowdigger.lineage' version '1.0.2'");
        assertThat(settings).contains("id 'org.flowdigger.xml' version '1.0.1'");
        assertThat(settings).contains("dependencyResolutionManagement", "mavenCentral()");
        assertThat(rootBuild).contains("id 'org.flowdigger.lineage'");
        assertThat(rootBuild).contains("id 'org.flowdigger.xml' apply false");
        assertThat(rootBuild).contains("apply from: '3-lineage/lineage-pipeline.gradle'");
        assertThat(rootBuild).contains("dependsOn(':lineage:generateLineage')");
        assertThat(rootBuild).contains("dependsOn(':lineage:verifyExampleLineage')");
        assertThat(rootBuild).doesNotContain("lineagePipeline {", "tasks.named('syncLineageRepos')", "doLast {");
        assertThat(lineagePipeline).contains("parserPlugin.set('org.flowdigger.antlr.plsql')");
        assertThat(lineagePipeline).contains("dependsOn(':application:prepareApplicationAssets')");
        assertThat(lineagePipeline).contains("dependsOn(':architecture:assembleArchitectureModel')");
        assertThat(lineageBuild).contains("dependsOn(':application:prepareApplicationAssets', ':architecture:assembleArchitectureModel', ':publishLineage')");
    }

    @Test
    void shouldCentralizeRepositoryAndPluginDefaults() throws Exception {
        Path root = ProjectRootPaths.root();
        String settings = Files.readString(root.resolve("settings.gradle"), StandardCharsets.UTF_8);
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        String applicationBuild = Files.readString(root.resolve(Path.of("2-application", "build.gradle")), StandardCharsets.UTF_8);
        String graphBuild = Files.readString(root.resolve(Path.of("4-knowledge-graph", "build.gradle")), StandardCharsets.UTF_8);
        assertThat(settings).contains("dependencyResolutionManagement", "mavenCentral()");
        assertThat(settings).contains("id 'org.springframework.boot' version '4.2.0'");
        assertThat(settings).contains("id 'io.spring.dependency-management' version '1.1.7'");
        assertThat(settings).contains("id 'name.jurgenei.gradle.archi' version '0.1.10'");
        assertThat(settings).contains("id 'org.flowdigger.gradle.archi' version archiPluginVersion");
        assertThat(rootBuild).doesNotContain("mavenCentral()");
        assertThat(graphBuild).doesNotContain("mavenCentral()");
        assertThat(applicationBuild).contains("id 'org.springframework.boot'");
        assertThat(graphBuild).contains("id 'org.springframework.boot'");
    }

    @Test
    void shouldDefineKnowledgeGraphContractAgainstArchimateAndLineageOutputs() throws Exception {
        Path root = ProjectRootPaths.root();
        String knowledgeGraphBuild = Files.readString(root.resolve(Path.of("4-knowledge-graph", "build.gradle")), StandardCharsets.UTF_8);
        String rootBuild = Files.readString(root.resolve("build.gradle"), StandardCharsets.UTF_8);
        assertThat(knowledgeGraphBuild).contains("startKnowledgeGraphContainer");
        assertThat(knowledgeGraphBuild).contains("loadKnowledgeGraphStubData");
        assertThat(knowledgeGraphBuild).contains("dumpKnowledgeGraph");
        assertThat(knowledgeGraphBuild).contains("verification-report.json");
        assertThat(knowledgeGraphBuild).contains("KnowledgeGraphFlowRunner");
        assertThat(rootBuild).contains("dependsOn(':knowledgeGraph:loadKnowledgeGraph')");
        assertThat(rootBuild).contains("dependsOn(':architecture:assembleArchitecture')");
        assertThat(rootBuild).contains("dependsOn(':knowledgeGraph:verifyKnowledgeGraph')");
        assertThat(knowledgeGraphBuild).contains("dependsOn(':architecture:assembleArchitecture', ':lineage:generateLineage', 'startKnowledgeGraph', 'loadKnowledgeGraphStubData')");
    }
}
