package com.marvin.bundle.templating;

import com.marvin.component.resource.ResourceService;
import com.marvin.component.resource.loader.ClasspathResourceLoader;
import com.marvin.component.resource.reference.ResourceReference;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TemplatingDemo {
    
    static class App {
        public String env = "test";
    }
    
    public static void main(String[] args) throws Exception {
        
        // init resource service.
        ResourceService resourceService = ResourceService.builder()
                .with(ResourceReference.CLASSPATH, new ClasspathResourceLoader(TemplatingDemo.class))
                .build();
        
        // init Environment.
        Environment env = Environment.builder()
                .resourceService(resourceService)
                .build();

        // init Engine
        Engine engine = Engine.builder()
                .environment(env)
//                .execute("{%", "%}")
//                .comment("{#", "#}")
//                .print("{{", "}}")
                .build();
        
        Map<String, Object> model = new HashMap<>();
        model.put("app", new App());
        model.put("date", new Date());
        model.put("names", Arrays.asList("Alex", "Joe", "Bob"));

//        Template demo = engine.load("classpath:demo.view");  
        Template demo = engine.load("demo.view");  
        engine.render(demo, new WriterRenderer(new OutputStreamWriter(System.out)), model);
//        engine.render(demo, Renderer.debug());    
//        engine.render(demo, CoreExtension.debug());   
    }
}