/*
 * The MIT License
 *
 * Copyright 2017 Mads.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.pretestpipelineplugin;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import java.io.IOException;
import java.util.Collection;
import jenkins.tasks.SimpleBuildStep;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.MergeCommand;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 *
 * @author Mads
 */
public class PretestPreparer extends Builder implements SimpleBuildStep {

    private String mode = "accumulated";
    private String workspace = "";
    private String branchPattern = "*/ready/**";
    private String integrationBranch = "master";
    private String credentialsId = "";
    private String repository = "origin";
    
    @DataBoundConstructor
    public PretestPreparer() {
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
    
    @Symbol("pretestPrepare")
    @Extension
    public static class Descripter extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Pretested Preparation";
        }
        
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> type) {
            return true;
        }
        
    }
    
    @Override
    public void perform(Run<?, ?> run, FilePath fp, Launcher lnchr, TaskListener tl) throws InterruptedException, IOException {
        //String assertions before perform
        assert mode.equals("squash") || mode.equals("accumulate");
        GitClient c = null;
        try {
            c = PretestShared.createClient(tl, run, fp, workspace, credentialsId);
            if(mode.equals("squash")) {
                tl.getLogger().println("[PRETESTED] Squash mode selected");
                ObjectId oid = c.revParse("HEAD");
                c.checkout().ref(integrationBranch).execute();
                c.merge().setRevisionToMerge(oid).setSquash(true).execute();
                //TODO
                c.commit("Whaatt?");
            } else {
                tl.getLogger().println("[PRETESTED] Accumulated mode selected");
                //Record current HEAD
                ObjectId oid = c.revParse("HEAD");
                //This checkout command is much more intelligent
                c.checkout().ref(integrationBranch).execute();
                c.merge().setRevisionToMerge(oid).setGitPluginFastForwardMode(MergeCommand.GitPluginFastForwardMode.NO_FF).execute();            
            }
        } finally {
            if (c != null) {
                c.withRepository(new RepositoryCallback<Void>() {
                    @Override
                    public Void invoke(Repository repo, VirtualChannel channel) throws IOException, InterruptedException {
                        repo.close();
                        return null;
                    }
                });
            }
        }
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> ab, BuildListener bl) {
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> ab, Launcher lnchr, BuildListener bl) throws InterruptedException, IOException {
        return true;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> ap) {
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions(AbstractProject<?, ?> ap) {
        return null;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    @DataBoundSetter
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return the branchPattern
     */
    public String getBranchPattern() {
        return branchPattern;
    }

    /**
     * @param branchPattern the branchPattern to set
     */
    @DataBoundSetter    
    public void setBranchPattern(String branchPattern) {
        this.branchPattern = branchPattern;
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
     * @return the credentials
     */
    public String getCredentialsId() {
        return credentialsId;
    }

    /**
     * @param credentialsId the credentials to set
     */
    @DataBoundSetter        
    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    /**
     * @return the repository
     */
    public String getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    @DataBoundSetter        
    public void setRepository(String repository) {
        this.repository = repository;
    }

}
