package de.evoxy.antivpn.papermc.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public class PluginDependencyLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {

        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("evoxyRepositorySnapshots", "default", "https://repo.evoxy.de/snapshots").build());

        resolver.addDependency(new Dependency(new DefaultArtifact("de.evoxy:easyjsonconfig:1.0.1-SNAPSHOT"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("de.evoxy:fluxsql:1.0.4-SNAPSHOT"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
