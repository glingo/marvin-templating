package com.marvin.bundle.templating;

import com.marvin.component.resource.ResourceService;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Environment {
    
    private ResourceService resourceService;
    
    private List<Source> sources;
    
    public List<Source> getSources() {
        return sources;
    }
    
    public Source load(String path) {
        if (null == this.sources) {
            this.sources = new ArrayList<>();
        }
        
        Optional<Source> result = this.sources.stream()
                .filter((source) -> source.getName().equals(path)).findFirst();
        
        if (!result.isPresent()) {
            InputStream stream = getResourceService().load(path);
            Reader reader = new InputStreamReader(stream);
            Source source = Source.builder(path).read(reader).build();
            this.sources.add(source);
            return source;
        }
        
        return result.get();
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }
    
    public ResourceService getResourceService() {
        return resourceService;
    }

    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public static EnvironmentBuilder builder() {
        return new EnvironmentBuilder();
    }
    
    public static class EnvironmentBuilder {
    
        private ResourceService resourceService;
        
        public EnvironmentBuilder resourceService(ResourceService service) {
            this.resourceService = service;
            return this;
        }
        
        public Environment build() {
            Environment env = new Environment();
            
            env.setResourceService(resourceService);
            
            return env;
        }
    }
}
