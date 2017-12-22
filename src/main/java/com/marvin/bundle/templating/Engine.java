package com.marvin.bundle.templating;

import com.marvin.bundle.templating.expression.ExpressionParser;
import com.marvin.bundle.templating.token.Token;
import com.marvin.bundle.templating.extention.Extension;
import com.marvin.bundle.templating.extention.Directive;
import com.marvin.bundle.templating.extention.Filter;
import com.marvin.bundle.templating.extention.Test;
import com.marvin.bundle.templating.extention.core.CoreExtension;
import com.marvin.bundle.templating.node.support.Node;
import com.marvin.bundle.templating.node.NodeParser;
import com.marvin.bundle.templating.node.NodeParsers;
import com.marvin.bundle.templating.node.NodeVisitorFactory;
import com.marvin.bundle.templating.operator.BinaryOperator;
import com.marvin.bundle.templating.operator.UnaryOperator;
import com.marvin.bundle.templating.token.TokenParser;
import com.marvin.bundle.templating.token.TokenParsers;
import com.marvin.bundle.templating.token.TokenStream;
import com.marvin.bundle.templating.token.TokenType;
import com.marvin.bundle.templating.token.Tokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

public class Engine {

    private final Environment environment;
    
    private final Map<String, Directive> directives;
    private final Map<String, NodeParser> nodeParsers;
    private final Map<String, Filter> filters;
    private final Map<String, Test> tests;
    private final List<NodeVisitorFactory> nodeVisitorFactories;
    private final List<Class> safeNodes;
    
    private final Tokenizer tokenizer;
    private final ExpressionParser expressionParser;
    private final NodeParser rootNodeParser;

    public Engine(Environment environment, 
            ExpressionParser expressionParser,
            Map<String, Renderer> renderers, 
            Map<String, Directive> directives,
            Map<String, NodeParser> nodeParsers,
            Map<String, Filter> filters,
            Map<String, Test> tests,
            List<NodeVisitorFactory> nodeVisitorFactories,
            List<Class> safeNodes,
            Tokenizer tokenizer) {
        this.environment = environment;
        this.expressionParser = expressionParser;
        this.directives = directives;
        this.tokenizer = tokenizer;
        this.nodeParsers = nodeParsers;
        this.filters = filters;
        this.tests = tests;
        this.nodeVisitorFactories = nodeVisitorFactories;
        this.rootNodeParser = NodeParsers.root();
        this.safeNodes = safeNodes;
    }
    
    public Template load(String path) throws Exception {
        Source source = getEnvironment().load(path);
        List<Token> tokens = this.tokenizer.tokenize(source);
        Node root = this.rootNodeParser.parse(new TokenStream(tokens), getExpressionParser(), this);
        Template template = Template.builder().named(path).root(root).build();
        
        getNodeVisitorFactories().stream()
                .map(factory -> factory.create(template))
                .forEach(visitor -> root.accept(visitor));
        return template;
    }

    public void render(Template template, Renderer renderer, Map<String, Object> model) throws Exception {
        Context context = createContext(template, model);
        render(renderer, context);
    }
    
    public void render(Renderer renderer, Context context) throws Exception {
        Hierarchy<Template> hierarchy = context.getTemplateHierarchy();
        
        hierarchy.get().getRoot().render(context, renderer);
        Template parent = hierarchy.getParent();
        if (parent != null) {
            hierarchy.ascend();
            render(renderer, context);
        }
        
        renderer.finalyze();
    }
    
    private Context createContext(Template template) {
        return createContext(template, new HashMap<>());
    }
    
    private Context createContext(Template template, Map<String, Object> model) {
        return new Context(template, this, Locale.getDefault(), model, safeNodes);
    }
    
    public Environment getEnvironment() {
        return this.environment;
    }

    public ExpressionParser getExpressionParser() {
        return this.expressionParser;
    }

    public Map<String, Directive> getDirectives() {
        return this.directives;
    }
    
    public Directive getDirective(String name) {
        return this.directives.get(name);
    }

    public Map<String, NodeParser> getNodeParsers() {
        return nodeParsers;
    }
    
    public NodeParser getNodeParser(String name) {
        return this.nodeParsers.get(name);
    }

    public NodeParser getRootNodeParser() {
        return this.rootNodeParser;
    }

    public Tokenizer getTokenizer() {
        return this.tokenizer;
    }
    
      public Map<String, Filter> getFilters() {
        return this.filters;
    }
    
    public Filter getFilter(String name) {
        return this.filters.get(name);
    }

    public Map<String, Test> getTests() {
        return tests;
    }
    
    public Test getTest(String name) {
        return this.tests.get(name);
    }

    public List<NodeVisitorFactory> getNodeVisitorFactories() {
        return nodeVisitorFactories;
    }

    public static EngineBuilder builder() {
        return new EngineBuilder();
    }

    public static class EngineBuilder {
        
        private final static TokenParser NAME = TokenParsers.name();
        private final static TokenParser STRING = TokenParsers.string();
        private final static TokenParser PUNCTUATION = TokenParsers.punctuation();
        private final static TokenParser NUMBER = TokenParsers.number();
        private final static TokenParser EOF = TokenParsers.EOF();
        private final static TokenParser EXPRESSION = NAME.or(NUMBER).or(PUNCTUATION).or(STRING);

        Tokenizer.TokenizerBuilder tokenizerBuilder = Tokenizer.builder();
        private Environment environment;
        
        private final List<String> starts         = new ArrayList<>();
        
        private String printOpen    = "{{";
        private String printClose   = "}}";
        private String executeOpen  = "{%";
        private String executeClose = "%}";
        private String commentOpen  = "{#";
        private String commentClose = "#}";
        
        private final List<Extension> extensions                  = new ArrayList<>();
        private final Map<String, UnaryOperator> unaryOperators   = new LinkedHashMap<>();
        private final Map<String, BinaryOperator> binaryOperators = new LinkedHashMap<>();
        private final Map<String, Renderer> renderers             = new HashMap<>();
        private final Map<String, Directive> directives           = new HashMap<>();
        private final Map<String, NodeParser> nodeParsers         = new HashMap<>();
        private final Map<String, Filter> filters                 = new HashMap<>();
        private final Map<String, Test> tests                     = new HashMap<>();
        private final List<NodeVisitorFactory> factories          = new ArrayList<>();
        private final List<Class> safeNodes                       = new ArrayList<>();

        public EngineBuilder environment(Environment environment) {
            this.environment = environment;
            return this;
        }
        
        public EngineBuilder extension(Extension extension) {
            this.extensions.add(extension);
            return this;
        }
        
        public EngineBuilder extensions(Extension... extensions) {
            this.extensions.addAll(Arrays.asList(extensions));
            return this;
        }
        
        public EngineBuilder execute(String open, String close) {
            this.executeOpen    = open;
            this.executeClose   = close;
            return this;
        }

        public EngineBuilder comment(String open, String close) {
            this.commentOpen    = open;
            this.commentClose   = close;
            return this;
        }
        
        public EngineBuilder print(String open, String close) {
            this.printOpen    = open;
            this.printClose   = close;
            return this;
        }
        
        public Engine build() {
            // add core-extension by default
            this.extensions.add(new CoreExtension());
            
            // load extensions
            this.extensions.forEach(extension -> {
                this.renderers.putAll(extension.getRenderers());
                this.directives.putAll(extension.getDirectives());
                this.nodeParsers.putAll(extension.getNodeParsers());
                this.filters.putAll(extension.getFilters());
                this.tests.putAll(extension.getTests());
                this.unaryOperators.putAll(extension.getUnaryOperators());
                this.binaryOperators.putAll(extension.getBinaryOperators());
                this.factories.addAll(extension.getNodeVisitorFactories());
                this.safeNodes.addAll(extension.getSafeNodes());
            });
            
            // create an operator token parser
            TokenParser unaryOperatorParser = TokenParser.in(TokenType.OPERATOR, this.unaryOperators.keySet().toArray(new String[]{}));
            TokenParser binaryOperatorParser = TokenParser.in(TokenType.OPERATOR, this.binaryOperators.keySet().toArray(new String[]{}));
            TokenParser operatorParser = unaryOperatorParser.or(binaryOperatorParser);
            
            // create a execute token parser
            TokenParser executeOpenParser   = TokenParser.from(TokenType.EXECUTE_OPEN, compile(quote(this.executeOpen)), false);
            TokenParser executeCloseParser  = TokenParser.from(TokenType.EXECUTE_CLOSE, compile(quote(this.executeClose)), false);
            TokenParser executeParser       = executeOpenParser.then(NAME).then(operatorParser.or(EXPRESSION).until(executeCloseParser));
            this.starts.add(this.executeOpen);
            
            // create a print token parser
            TokenParser printOpenParser     = TokenParser.from(TokenType.PRINT_OPEN, compile(quote(this.printOpen)), false);
            TokenParser printCloseParser    = TokenParser.from(TokenType.PRINT_CLOSE, compile(quote(this.printClose)), false);
            TokenParser printParser         = printOpenParser.then(operatorParser.or(EXPRESSION).until(printCloseParser));
            this.starts.add(this.printOpen);
            
            // create a comment token parser
            TokenParser commentOpenParser   = TokenParser.from(TokenType.COMMENT_OPEN, compile(quote(this.commentOpen)), false);
            TokenParser commentCloseParser  = TokenParser.from(TokenType.COMMENT_CLOSE, compile(quote(this.commentClose)), false);
            TokenParser commentInner        = TokenParser.until(TokenType.TEXT, this.commentClose);
            TokenParser commentParser       = commentOpenParser.then(commentInner.optional()).then(commentCloseParser);
            this.starts.add(this.commentOpen);
            
            // create a text token parser
            TokenParser leadingText = TokenParser.until(TokenType.TEXT, this.starts);
            TokenParser text = TokenParser.from(TokenType.TEXT, Pattern.compile("^.*", Pattern.DOTALL | Pattern.MULTILINE), false);
            
            TokenParser principal = commentParser.or(executeParser).or(printParser).or(leadingText).zeroOrMore().then(text.optional()).then(EOF);
            this.tokenizerBuilder.parser(principal);
            Tokenizer tokenizer = this.tokenizerBuilder.build();
            
            ExpressionParser expressionParser = new ExpressionParser(this.unaryOperators, this.binaryOperators);
            
            Engine engine = new Engine(this.environment, expressionParser,
                    renderers, directives, nodeParsers, 
                    filters, tests, factories, safeNodes,
                    tokenizer);
            return engine;
        }

    }
}
