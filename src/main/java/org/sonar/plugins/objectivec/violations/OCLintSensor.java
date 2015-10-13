package org.sonar.plugins.objectivec.violations;

import java.io.File;
import java.util.Collection;

import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.objectivec.ObjectiveCPlugin;
import org.sonar.plugins.objectivec.ObjectiveCStartup;
import org.sonar.plugins.objectivec.core.ObjectiveC;

public final class OCLintSensor implements Sensor {
    public static final String REPORT_PATH_KEY = ObjectiveCPlugin.PROPERTY_PREFIX
            + ".oclint.report";
    public static final String DEFAULT_REPORT_PATH= "/oclint.xml" ;

    private final Settings conf;
    private final FileSystem fileSystem;

    public OCLintSensor(final FileSystem moduleFileSystem, final Settings config) {
        this.conf = config;
        this.fileSystem = moduleFileSystem;
       
    }

    public boolean shouldExecuteOnProject(final Project project) {

        return project.isRoot() && fileSystem.languages().contains(ObjectiveC.KEY);

    }

    public void analyse(final Project project, final SensorContext context) {
        final String projectBaseDir = project.getFileSystem().getBasedir()
                .getPath();
        final OCLintParser parser = new OCLintParser(project, context);
        saveViolations(parseReportIn(projectBaseDir, parser), context);

    }

    private void saveViolations(final Collection<Violation> violations,
            final SensorContext context) {
        for (final Violation violation : violations) {
            context.saveViolation(violation);
        }
    }

    private Collection<Violation> parseReportIn(final String baseDir,
            final OCLintParser parser) {
        final StringBuilder reportFileName = new StringBuilder(baseDir);
        
        reportFileName.append(reportPath());

        LoggerFactory.getLogger(getClass()).info("Processing OCLint report {}",
                reportFileName);
        return parser.parseReport(new File(reportFileName.toString()));

        	
    }

    private String reportPath() {
        String reportPath = conf.getString(REPORT_PATH_KEY);
        if (reportPath == null) {
            reportPath = DEFAULT_REPORT_PATH;
        }
        return reportPath;
    }
}