package ro.redeul.google.go.runner;

import com.intellij.compiler.make.MakeUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.components.PathMacroManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import ro.redeul.google.go.runner.ui.GoRunConfigurationEditorForm;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: mtoader
 * Date: Aug 19, 2010
 * Time: 2:53:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class GoRunConfiguration extends ModuleBasedConfiguration<RunConfigurationModule> {

    public String scriptName;
    public String scriptArguments;
    public String workDir;

    public GoRunConfiguration(String name, Project project, GoRunConfigurationType configurationType) {
        super(name, new RunConfigurationModule(project), configurationType.getConfigurationFactories()[0]);
        workDir = PathUtil.getLocalPath(project.getBaseDir());
    }

    @Override
    public Collection<Module> getValidModules() {
        Module[] modules = ModuleManager.getInstance(getProject()).getModules();
        return Arrays.asList(modules);
    }

    @Override
    protected ModuleBasedConfiguration createInstance() {
        return new GoRunConfiguration(getName(), getProject(), GoRunConfigurationType.getInstance());
    }

    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new GoRunConfigurationEditorForm(getProject());
    }

    public void readExternal(final Element element) throws InvalidDataException {
        PathMacroManager.getInstance(getProject()).expandPaths(element);
        super.readExternal(element);
        RunConfigurationExtension.readSettings(this, element);
        XmlSerializer.deserializeInto(this, element);
        readModule(element);
//        EnvironmentVariablesComponent.readExternal(element, getEnvs());
    }

    public void writeExternal(final Element element) throws WriteExternalException {
        super.writeExternal(element);
        RunConfigurationExtension.writeSettings(this, element);
        XmlSerializer.serializeInto(this, element);
        writeModule(element);
//        EnvironmentVariablesComponent.writeExternal(element, getEnvs());
        PathMacroManager.getInstance(getProject()).collapsePathsRecursively(element);
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        CommandLineState state = new CommandLineState(env) {
            @Override
            protected ProcessHandler startProcess() throws ExecutionException {

                GeneralCommandLine commandLine = new GeneralCommandLine();

                commandLine.setExePath(getCompiledFileName(getModule(), scriptName));
                if ( scriptArguments != null && scriptArguments.trim().length() > 0 ) {
                    commandLine.addParameter(scriptArguments);
                }
                commandLine.setWorkDirectory(workDir);

                return GoApplicationProcessHandler.runCommandLine(commandLine);
            }
        };

        state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
        return state;
    }

    private String getCompiledFileName(Module module, String scriptName) {
        VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots();

        VirtualFile scriptFile = VirtualFileManager.getInstance().findFileByUrl("file:///" + scriptName);


        if (scriptFile != null) {
            for (VirtualFile sourceRoot : sourceRoots) {

                if (VfsUtil.isAncestor(sourceRoot, scriptFile, true)) {
                    String relativePath = VfsUtil.getRelativePath(scriptFile.getParent(), sourceRoot, '/');

                    return CompilerPaths.getModuleOutputPath(module, false) + "/" + relativePath + "/" + scriptFile.getNameWithoutExtension() + ".out";
                }
            }
        }

        return scriptName;
    }

    public Module getModule() {
        return getConfigurationModule().getModule();
    }
}