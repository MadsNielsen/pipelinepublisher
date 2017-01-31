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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.IOException;
import java.util.Collections;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitclient.RepositoryCallback;

/**
 *
 * @author Mads
 */
public class PretestShared {
    
    public static GitClient createClient(TaskListener tl, Run<?,?> run, FilePath fp, String workspace, String credentialsId) throws IOException, InterruptedException {        
        StandardUsernameCredentials uc = CredentialsProvider.findCredentialById(credentialsId, StandardUsernameCredentials.class, run, Collections.EMPTY_LIST);
        GitClient c = Git.with(tl, run.getEnvironment(tl)).in(new FilePath(fp, workspace)).getClient();
        if(uc == null) {
            tl.getLogger().println("Unable to find credential with id '"+credentialsId+"'");
        } else {
            c.setCredentials(uc);
        }                
        return c;
    }

    public static class FindCommitAuthorCallback implements RepositoryCallback<String> {
        /**
         * The commit Id
         */
        public final ObjectId id;
        public final TaskListener listener;

        /**
         * Constructor for FindCommitAuthorCallback
         * @param listener The TaskListener
         * @param id The Commit id of the commit of which to find the author.
         */
        public FindCommitAuthorCallback(TaskListener listener, final ObjectId id) {
            this.listener = listener;
            this.id = id;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String invoke(Repository repository, VirtualChannel channel) throws IOException, InterruptedException {
            RevWalk walk = new RevWalk(repository);
            RevCommit commit = walk.parseCommit(id);
            walk.dispose();
            return commit.getAuthorIdent().toExternalString();
        }
    }
}
