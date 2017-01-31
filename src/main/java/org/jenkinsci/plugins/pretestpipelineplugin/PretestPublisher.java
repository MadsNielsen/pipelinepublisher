package org.jenkinsci.plugins.pretestpipelineplugin;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.IOException;
import java.util.Collections;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.kohsuke.stapler.DataBoundSetter;

public class PretestPublisher extends Recorder implements SimpleBuildStep {

    private String credentialsId;
    private String integrationBranch;
    private String workspace = "";

    @DataBoundConstructor
    public PretestPublisher(String credentialsId, String integrationBranch) {
        this.credentialsId = credentialsId;
        this.integrationBranch = integrationBranch;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener tl) throws InterruptedException, IOException {
        GitClient c = PretestShared.createClient(tl, run, fp, workspace, credentialsId);
        tl.getLogger().println("Now we have a git client");
        tl.getLogger().println("Get branch information about what triggered this build");
        //run.getAction(BuildData.class)
        tl.getLogger().println("Do whatever is necessary");
    }

    /**
     * @return the credentialsId
     */
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * @param credentialsId the credentialsId to set
     */
    @DataBoundSetter
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    /**
     * @return the integrationBranch
     */
    public String getIntegrationBranch() {
        return integrationBranch;
    }

    /**
     * @param integrationBranch the integrationBranch to set
     */
    @DataBoundSetter
    public void setIntegrationBranch(String integrationBranch) {
        this.integrationBranch = integrationBranch;
    }

    /**
     * @return the workspace
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * @param workspace the workspace to set
     */
    @DataBoundSetter
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Symbol("pretestedPublisher")
    @Extension
    public static class PretestPublisherDescripter extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
           return true;
        }

        @Override
        public String getDisplayName() {
           return "Pretested Publisher";
        }

    }

}

