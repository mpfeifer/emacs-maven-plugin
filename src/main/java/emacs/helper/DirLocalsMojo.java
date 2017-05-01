package emacs.helper;
    
import java.io.IOException;
import java.io.File;
import java.lang.StringBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Write a .dir-locals.el file with some information from MavenProject.
 *
 */
@Mojo( name = "dirlocals")
public class DirLocalsMojo extends AbstractMojo
{
    @Parameter( property="projectel.projectFile", defaultValue=".dir-locals.el")
    private String projectFile;

    @Parameter( property="projectel.classpathVariable", defaultValue="java-classpath")
    private String classpathVariable;

    @Parameter( property="projectel.projectRootVariable", defaultValue="java-project-root")
    private String projectRootVariable;

    @Parameter( defaultValue="${project}" )
    private org.apache.maven.project.MavenProject mavenProject;
    
    public void execute() throws MojoExecutionException
    {
        /* build classpath entry */
        StringBuffer classpath = new StringBuffer();
        classpath.append("(");
        classpath.append(classpathVariable);
        classpath.append(" . ( ");
        boolean empty=true;
        Set<Artifact> elements = mavenProject.getDependencyArtifacts();
        for (Artifact elem : elements) {
            empty=false;
            if (elem.getFile() != null) {
                String absolutePath = elem.getFile().getAbsolutePath();
                classpath.append(String.format("\"%s\" ", absolutePath));
            }
        }
        classpath.append("))");

        /* build project-root entry */
        StringBuffer projectRoot = new StringBuffer();
        String projectRootDirectory = mavenProject.getBasedir().getAbsolutePath();
        projectRoot.append(String.format("(%s . \"%s\")", projectRootVariable, projectRootDirectory));

        /* build final result */
        StringBuffer result = new StringBuffer();
        result.append("((java-mode . ( ");
        result.append(String.format("%s\r\n", classpath.toString()));
        result.append(String.format("          %s)))\r\n", projectRoot.toString()));

        /* write to file */
        try {
            Files.write(Paths.get(projectFile) , result.toString().getBytes());
            getLog().info( String.format("File %s was written.", projectFile ) );
        } catch (IOException e) {
            getLog().error( String.format("Error (%s) when writing project file %s.", e.getMessage(), projectFile ) );
            getLog().info( String.format("Projectfile %s was NOT written.", projectFile ) );
            throw new MojoExecutionException("Could not write project file!");
        }
    }
}
