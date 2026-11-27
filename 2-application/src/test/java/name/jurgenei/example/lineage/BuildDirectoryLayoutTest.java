package name.jurgenei.example.lineage;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuildDirectoryLayoutTest {

    @Test
    void shouldPlaceSubprojectBuildDirectoriesUnderRootBuild() {
        Path projectsBuild = ProjectRootPaths.root().resolve(Path.of("build", "projects"));
        for (String project : List.of("architecture", "application", "lineage", "knowledgeGraph")) {
            assertThat(System.getProperty("buildDirectory." + project))
                    .as("build directory for %s", project)
                    .isEqualTo(projectsBuild.resolve(project).toString());
        }
    }
}
