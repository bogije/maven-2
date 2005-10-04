package org.apache.maven.plugin.plugin;

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.registry.MavenPluginRegistryBuilder;
import org.apache.maven.plugin.registry.Plugin;
import org.apache.maven.plugin.registry.PluginRegistry;
import org.apache.maven.plugin.registry.PluginRegistryUtils;
import org.apache.maven.plugin.registry.io.xpp3.PluginRegistryXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Update the user plugin registry (if it's in use) to reflect the version we're installing.
 * 
 * @goal updateRegistry
 * @phase install
 */
public class UpdatePluginRegistryMojo
    extends AbstractMojo
{

    /**
     * @parameter default-value="${settings.usePluginRegistry}"
     * @required
     * @readonly
     */
    private boolean usePluginRegistry;

    /**
     * @parameter default-value="${project.groupId}"
     * @required
     * @readonly
     */
    private String groupId;

    /**
     * @parameter default-value="${project.artifactId}"
     * @required
     * @readonly
     */
    private String artifactId;

    /**
     * @parameter default-value="${project.artifact.version}"
     * @required
     * @readonly
     */
    private String version;

    /**
     * @component role="org.apache.maven.plugin.registry.MavenPluginRegistryBuilder"
     */
    private MavenPluginRegistryBuilder pluginRegistryBuilder;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( usePluginRegistry )
        {
            updatePluginVersionInRegistry( groupId, artifactId, version );
        }
    }

    private void updatePluginVersionInRegistry( String groupId, String artifactId, String version ) throws MojoExecutionException
    {
        PluginRegistry pluginRegistry;
        try
        {
            pluginRegistry = getPluginRegistry( groupId, artifactId );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Failed to read plugin registry.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new MojoExecutionException( "Failed to parse plugin registry.", e );
        }

        String pluginKey = ArtifactUtils.versionlessKey( groupId, artifactId );
        Plugin plugin = (Plugin) pluginRegistry.getPluginsByKey().get( pluginKey );

        // if we can find the plugin, but we've gotten here, the useVersion must be missing; fill it in.
        if ( plugin != null )
        {
            if ( PluginRegistry.GLOBAL_LEVEL.equals( plugin.getSourceLevel() ) )
            {
                // do nothing. We don't rewrite the globals, under any circumstances.
                getLog().warn(
                               "Cannot update registered version for plugin {" + groupId + ":" + artifactId
                                   + "}; it is specified in the global registry." );
            }
            else
            {
                plugin.setUseVersion( version );

                SimpleDateFormat format = new SimpleDateFormat(
                                                                org.apache.maven.plugin.registry.Plugin.LAST_CHECKED_DATE_FORMAT );

                plugin.setLastChecked( format.format( new Date() ) );
            }
        }
        else
        {
            plugin = new org.apache.maven.plugin.registry.Plugin();

            plugin.setGroupId( groupId );
            plugin.setArtifactId( artifactId );
            plugin.setUseVersion( version );

            pluginRegistry.addPlugin( plugin );

            pluginRegistry.flushPluginsByKey();
        }

        writeUserRegistry( groupId, artifactId, pluginRegistry );
    }

    private void writeUserRegistry( String groupId, String artifactId, PluginRegistry pluginRegistry )
    {
        File pluginRegistryFile = pluginRegistry.getRuntimeInfo().getFile();

        PluginRegistry extractedUserRegistry = PluginRegistryUtils.extractUserPluginRegistry( pluginRegistry );

        // only rewrite the user-level registry if one existed before, or if we've created user-level data here.
        if ( extractedUserRegistry != null )
        {
            FileWriter fWriter = null;

            try
            {
                pluginRegistryFile.getParentFile().mkdirs();
                fWriter = new FileWriter( pluginRegistryFile );

                PluginRegistryXpp3Writer writer = new PluginRegistryXpp3Writer();

                writer.write( fWriter, extractedUserRegistry );
            }
            catch ( IOException e )
            {
                getLog().warn(
                               "Cannot rewrite user-level plugin-registry.xml with new plugin version of plugin: \'"
                                   + groupId + ":" + artifactId + "\'.", e );
            }
            finally
            {
                IOUtil.close( fWriter );
            }
        }
    }

    private PluginRegistry getPluginRegistry( String groupId, String artifactId )
        throws IOException, XmlPullParserException
    {
        PluginRegistry pluginRegistry = null;

        pluginRegistry = pluginRegistryBuilder.buildPluginRegistry();

        if ( pluginRegistry == null )
        {
            pluginRegistry = pluginRegistryBuilder.createUserPluginRegistry();
        }

        return pluginRegistry;
    }

}