# 1-spring功能扩展解读-obtainFreshBeanFactory
入口:
`AbstractApplicationContext.refresh()`


下面分析方法:
`obtainFreshBeanFactory()`
* 作用: 加载 BeanFactory ,经过这个函数后ApplicationContext就有了BeanFactory的所有功能

* 代码:
    ```
        protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
            refreshBeanFactory();
            ConfigurableListableBeanFactory beanFactory = getBeanFactory();
            if (logger.isDebugEnabled()) {
                logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
            }
            return beanFactory;
        }
    ```
`obtainFreshBeanFactory()` 方法将主要功能委托给了`refreshBeanFactory()`,自己就实现了一个日志功能.

---
`refreshBeanFactory()`
- 作用: 处理 BeanFactory

- 代码:

有两种实现 AbstractRefreshableApplicationContext
```
    @Override
    protected final void refreshBeanFactory() throws BeansException {
        if (hasBeanFactory()) {
            destroyBeans();
            closeBeanFactory();
        }
        try {
            //创建 DefaultListableBeanFactory
            DefaultListableBeanFactory beanFactory = createBeanFactory();
            //  设置序列化ID,如果需要的话,让这个BeanFactory 从  id 反序列化到 BeanFactory对象
            beanFactory.setSerializationId(getId());
            // 定制 beanFactory ,设置相关属性
            //1. 是否允许覆盖同名称的不同定义的对象
            //2. 循环依赖
            //3. 设置  @Autowired 和 @Qualifier 注解解析器 QualifierAnnotationAutowireCandidateResolver,spring5已经修改了            
            customizeBeanFactory(beanFactory);
            //加载 beanDefinition ,初始化 DocumentReader,并进行文件 读取和解析
            loadBeanDefinitions(beanFactory);
            //保证线程安全的对象锁
            synchronized (this.beanFactoryMonitor) {
                this.beanFactory = beanFactory;
            }
        }
        catch (IOException ex) {
            throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
        }
    }
`````
    
另一种 GenericApplicationContext

```
    @Override
    protected final void refreshBeanFactory() throws IllegalStateException {
        if (!this.refreshed.compareAndSet(false, true)) {
            throw new IllegalStateException(
                    "GenericApplicationContext does not support multiple refresh attempts: just call 'refresh' once");
        }
        this.beanFactory.setSerializationId(getId());
    }
```

---
- 定制beanFactory`customizeBeanFactory`
```
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
	    // 是否允许覆盖同名称的不同定义的对象
		if (this.allowBeanDefinitionOverriding != null) {
			beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
		}
		//是否允许bean之间存在循环依赖
		if (this.allowCircularReferences != null) {
			beanFactory.setAllowCircularReferences(this.allowCircularReferences);
		}
	}
```
上面两个值是在子类中赋值的,比如自定义 一个 MyClassPathXmlApplicationContext,然后覆写 customizeBeanFactory 方法 
- 加载bean`loadBeanDefinitions`
实现类有5个
1. AnnotationConfigReactiveWebApplicationContext
2. AbstractXmlApplicationContext
3. AnnotationConfigWebApplicationContext
```
		AnnotatedBeanDefinitionReader reader = getAnnotatedBeanDefinitionReader(beanFactory);
		//ClassPathBeanDefinitionScanner 扫描bean
		// 具体实现扫描的在: ClassPathScanningCandidateComponentProvider.scanCandidateComponents 
		// getResourcePatternResolver().getResources(packageSearchPath)
		//  ResourcePatternResolver接口处理扫描的事
		//   PathMatchingResourcePatternResolver.doFindAllClassPathResources(path)
		ClassPathBeanDefinitionScanner scanner = getClassPathBeanDefinitionScanner(beanFactory);

		BeanNameGenerator beanNameGenerator = getBeanNameGenerator();
		if (beanNameGenerator != null) {
			reader.setBeanNameGenerator(beanNameGenerator);
			scanner.setBeanNameGenerator(beanNameGenerator);
			//注册一个bean,用于生成bean的名称
			beanFactory.registerSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR, beanNameGenerator);
		}

		ScopeMetadataResolver scopeMetadataResolver = getScopeMetadataResolver();
		if (scopeMetadataResolver != null) {
			reader.setScopeMetadataResolver(scopeMetadataResolver);
			scanner.setScopeMetadataResolver(scopeMetadataResolver);
		}

		if (!this.annotatedClasses.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Registering annotated classes: [" +
						StringUtils.collectionToCommaDelimitedString(this.annotatedClasses) + "]");
			}
			reader.register(this.annotatedClasses.toArray(new Class<?>[this.annotatedClasses.size()]));
		}

		if (!this.basePackages.isEmpty()) {
			if (logger.isInfoEnabled()) {
				logger.info("Scanning base packages: [" +
						StringUtils.collectionToCommaDelimitedString(this.basePackages) + "]");
			}
			//扫描包
			scanner.scan(StringUtils.toStringArray(this.basePackages));
		}

		String[] configLocations = getConfigLocations();
		if (configLocations != null) {
			for (String configLocation : configLocations) {
				try {
					Class<?> clazz = ClassUtils.forName(configLocation, getClassLoader());
					if (logger.isInfoEnabled()) {
						logger.info("Successfully resolved class for [" + configLocation + "]");
					}
					reader.register(clazz);
				}
				catch (ClassNotFoundException ex) {
					if (logger.isDebugEnabled()) {
						logger.debug("Could not load class for config location [" + configLocation +
								"] - trying package scan. " + ex);
					}
					int count = scanner.scan(configLocation);
					if (logger.isInfoEnabled()) {
						if (count == 0) {
							logger.info("No annotated classes found for specified class/package [" + configLocation + "]");
						}
						else {
							logger.info("Found " + count + " annotated classes in package [" + configLocation + "]");
						}
					}
				}
			}
		}
```
4. GroovyWebApplicationContext
5. XmlWebApplicationContext
```
	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
		// Create a new XmlBeanDefinitionReader for the given BeanFactory.
		//
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

		// Configure the bean definition reader with this context's
		// resource loading environment.
		beanDefinitionReader.setEnvironment(getEnvironment());
		beanDefinitionReader.setResourceLoader(this);
		beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

		// Allow a subclass to provide custom initialization of the reader,
		// then proceed with actually loading the bean definitions.
		//一个空方法,留给子类覆盖
		initBeanDefinitionReader(beanDefinitionReader);
		//一个一个配置文件读bean
		loadBeanDefinitions(beanDefinitionReader);
	}
```

- 扫描文件,先得到路径URL,用 ClassLoader.getResources,path = com/byedbl/

//url = 完整路径 如: file:/D:/IdeaProject/spring-bean-extention/target/classes/com/byedbl/

// rootDir = Resource.getFile().getAbsoluteFile() 得到file
```
		Set<Resource> result = new LinkedHashSet<>(16);
		ClassLoader cl = getClassLoader();
		Enumeration<URL> resourceUrls = (cl != null ? cl.getResources(path) : ClassLoader.getSystemResources(path));
		while (resourceUrls.hasMoreElements()) {
			URL url = resourceUrls.nextElement();
			result.add(convertClassLoaderURL(url));
		}
```

```
			else if (ResourceUtils.isJarURL(rootDirUrl) || isJarResource(rootDirResource)) {
				result.addAll(doFindPathMatchingJarResources(rootDirResource, rootDirUrl, subPattern));
			}
			else {
				result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
			}
```

`retrieveMatchingFiles` 方法先处理一些读取文件的异常和判断再递归读取文件

递归扫描包
```
    // fullPattern = D:/IdeaProject/spring-bean-extention/target/classes/com/byedbl/**/*.class
	protected void doRetrieveMatchingFiles(String fullPattern, File dir, Set<File> result) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Searching directory [" + dir.getAbsolutePath() +
					"] for files matching pattern [" + fullPattern + "]");
		}
		File[] dirContents = dir.listFiles();
		if (dirContents == null) {
			if (logger.isWarnEnabled()) {
				logger.warn("Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
			}
			return;
		}
		Arrays.sort(dirContents);
		for (File content : dirContents) {
			String currPath = StringUtils.replace(content.getAbsolutePath(), File.separator, "/");
			// 用 AntPathMatcher 匹配模式 
			if (content.isDirectory() && getPathMatcher().matchStart(fullPattern, currPath + "/")) {
				if (!content.canRead()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Skipping subdirectory [" + dir.getAbsolutePath() +
								"] because the application is not allowed to read the directory");
					}
				}
				else {
					doRetrieveMatchingFiles(fullPattern, content, result);
				}
			}
			if (getPathMatcher().match(fullPattern, currPath)) {
				result.add(content);
			}
		}
	}
```

- 注册 一个 bean
```
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}
```