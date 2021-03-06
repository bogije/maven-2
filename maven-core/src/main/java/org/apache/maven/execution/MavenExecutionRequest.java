package org.apache.maven.execution;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.monitor.event.EventDispatcher;
import org.apache.maven.monitor.event.EventMonitor;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.settings.Settings;

import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public interface MavenExecutionRequest
{
    ArtifactRepository getLocalRepository();

    List getGoals();

    void setSession( MavenSession session );

    MavenSession getSession();

    void addEventMonitor( EventMonitor monitor );

    EventDispatcher getEventDispatcher();

    Settings getSettings();

    String getBaseDirectory();

    void setRecursive( boolean recursive );

    boolean isRecursive();

    void setReactorActive( boolean reactorActive );

    boolean isReactorActive();

    void setResumeFrom( String resumeFrom );

    String getResumeFrom();
    
    void setSelectedProjects( List projects );
    
    List getSelectedProjects();
    
    void setMakeBehavior( String makeBehavior );
    
    String getMakeBehavior();
    
    void setPomFile( String pomFile );

    String getPomFile();

    void setFailureBehavior( String failureBehavior );

    String getFailureBehavior();

    ProfileManager getGlobalProfileManager();

    Properties getExecutionProperties();

    Properties getUserProperties();

    Date getStartTime();

    boolean isShowErrors();

    ProjectBuilderConfiguration getProjectBuilderConfiguration();
}
