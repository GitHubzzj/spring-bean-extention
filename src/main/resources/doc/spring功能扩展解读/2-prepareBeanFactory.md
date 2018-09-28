功能扩展 prepareBeanFactory
```
	protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// Tell the internal bean factory to use the context's class loader etc.
		//设置 ClassLoader为当前上下文的
		beanFactory.setBeanClassLoader(getClassLoader());
		//设置BeanFactory 的表达式语言处理器,默认 可以使用 #{bean.xxx} 的形式调用相关属性值,增加对SpEL语言的支持
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
		// 增加了一个默认的propertyEditor,属性编辑器
		beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

		// Configure the bean factory with context callbacks.
		// 添加  ApplicationContextAwareProcessor 的
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
		//设置 几个忽略自动装配的接口
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);

		// BeanFactory interface not registered as resolvable type in a plain factory.
		// MessageSource registered (and found for autowiring) as a bean.
		// 设置了几个自动装配 的特殊 规则
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

		// Register early post-processor for detecting inner beans as ApplicationListeners.
		beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

        // 增加对 AspectJ 的支持
		// Detect a LoadTimeWeaver and prepare for weaving, if found.
		if (beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
			beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
			// Set a temporary ClassLoader for type matching.
			beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
		}

		// Register default environment beans.
		//将相关环境变量及属性以单例模式注册
		if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
		}
		if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
			beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
		}
	}
```

- StandardBeanExpressionResolver 在 `AbstractBeanFactory` 中调用
```
	protected Object evaluateBeanDefinitionString(@Nullable String value, @Nullable BeanDefinition beanDefinition) {
		if (this.beanExpressionResolver == null) {
			return value;
		}

		Scope scope = null;
		if (beanDefinition != null) {
			String scopeName = beanDefinition.getScope();
			if (scopeName != null) {
				scope = getRegisteredScope(scopeName);
			}
		}
		return this.beanExpressionResolver.evaluate(value, new BeanExpressionContext(this, scope));
	}
```

- `beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()))`

注册属性编辑器  
- 实现自定义属性编辑器的两个方法:
1. 实现一个 `PropertyEditorSupport`,覆写其 `setAsText` 方法,再配置 CustomEditorConfigurer 的属性 customEditors
 customEditors 是一个map,key 为 类全称,值为实现的bean
2. 实现一个 `PropertyEditorRegistrar`  , 再配置 CustomEditorConfigurer 的属性 propertyEditorRegistrars

`PropertyEditorRegistrySupport.createDefaultEditors` 有各种类型的转换实现

`TypeConverterDelegate.convertIfNecessary(@Nullable String propertyName, @Nullable Object oldValue, @Nullable Object newValue,
                      			@Nullable Class<T> requiredType, @Nullable TypeDescriptor typeDescriptor) throws IllegalArgumentException`
调用editor转换值                      			 
                      			 
- ResourceEditorRegistrar 注册了一系列的常用资源类属性编辑器

- `ApplicationContextAwareProcessor` 实现了  
`EnvironmentAware
EmbeddedValueResolverAware
ResourceLoaderAware
ApplicationEventPublisherAware
MessageSourceAware
ApplicationContextAware`
的bean 在此处调用其 `setApplicationContext` 方法将 `applicationContext` 赋值进去.
这就是我们常用的 `ApplicationContextAware` 能拿到  `applicationContext` 的原因.

- 设置几个忽略自动装配的接口
上面的 `ApplicationContextAwareProcessor` 处理了这几个自动装配的接口
```
		beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
		beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
		beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
		beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
		beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
```

- 注册依赖 

当bean的属性注册的时候,检测到属性为 `BeanFactory,ResourceLoader,
ApplicationEventPublisher,ApplicationContext`,会将对应的实例注入进去
```
		beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
		beanFactory.registerResolvableDependency(ResourceLoader.class, this);
		beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
		beanFactory.registerResolvableDependency(ApplicationContext.class, this);

```