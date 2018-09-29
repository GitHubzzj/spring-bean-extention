- ##### initApplicationEventMulticaster

```

public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster"

	/**
	 * Initialize the ApplicationEventMulticaster.
	 * Uses SimpleApplicationEventMulticaster if none defined in the context.
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		// 如果用户自定义了 applicationEventMulticaster 就用自定义的,bean名称必须为: applicationEventMulticaster
		if (beanFactory.containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster =
					beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, ApplicationEventMulticaster.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
		    //默认没有配置的用户会用,SimpleApplicationEventMulticaster
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
			beanFactory.registerSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, this.applicationEventMulticaster);
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate ApplicationEventMulticaster with name '" +
						APPLICATION_EVENT_MULTICASTER_BEAN_NAME +
						"': using default [" + this.applicationEventMulticaster + "]");
			}
		}
	}
```

####什么时候用事件机制呢？

- 第一，当有多个代码块中需要引用相同代码的时候。
- 第二，发生的这个操作或者代码不需要立即执行的，可以异步的

#####springboot实现异步的要点
1. 在启动类上加上 @EnableAsync注解
2. 在Listener方法或类上加上@Async

---

- #####SimpleApplicationEventMulticaster
该类 multicastEvent 负责调 listener.onApplicationEvent(event); 方法,为入口.

可以手工设置 taskExecutor 和 errorHandler.如:
```
    <!-- 定义一个固定大小的线程，采用factory-method和静态方法的形式，参数注入使用构造函数注入 -->
    <bean name="executor" class="java.util.concurrent.Executors" factory-method="newFixedThreadPool">
            <constructor-arg index="0"><value>5</value></constructor-arg>
    </bean>
    <!-- 定义applicationEventMulticaster，注入线程池和errorHandler,此处使用系统自带的广播器，也可以注入其他广播器， -->
    <bean name="applicationEventMulticaster" class="org.springframework.context.event.SimpleApplicationEventMulticaster">
        <property name="taskExecutor" ref="executor"></property>
        <property name="errorHandler" ref="errorHandler"></property>
    </bean>
    <!-- 定义一个errorHandler，统一处理异常信息 -->
    <bean name="errorHandler" class="com.byedbl.MyErrorHandler"></bean>
```

---
#####什么时候注册listener呢?
在 `initApplicationEventMulticaster`后有个 `registerListeners` 方法
```

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 */
	protected void registerListeners() {
	    //处理硬编码方法注册的监听器
		// Register statically specified listeners first.
		for (ApplicationListener<?> listener : getApplicationListeners()) {
			getApplicationEventMulticaster().addApplicationListener(listener);
		}
        
        //处理配置文件注册的监听器
        //我们定义的监听器在这里注册
		// Do not initialize FactoryBeans here: We need to leave all regular beans
		// uninitialized to let post-processors apply to them!
		String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
		for (String listenerBeanName : listenerBeanNames) {
			getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
		}

		// Publish early application events now that we finally have a multicaster...
		Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
		this.earlyApplicationEvents = null;
		if (earlyEventsToProcess != null) {
			for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
				getApplicationEventMulticaster().multicastEvent(earlyEvent);
			}
		}
	}
```

`getApplicationEventMulticaster().multicastEvent(earlyEvent);` 调
`listener的 onApplicationEvent` 方法

---
#####context.publishEvent(event)干了啥
```
	protected void publishEvent(Object event, @Nullable ResolvableType eventType) {
		Assert.notNull(event, "Event must not be null");
		if (logger.isTraceEnabled()) {
			logger.trace("Publishing event in " + getDisplayName() + ": " + event);
		}

		// Decorate event as an ApplicationEvent if necessary
		ApplicationEvent applicationEvent;
		//支持两种事件
		//1、直接继承ApplicationEvent，
		//2、其他事件，会被包装为PayloadApplicationEvent，可以使用getPayload获取真实的通知内容
		if (event instanceof ApplicationEvent) {
			applicationEvent = (ApplicationEvent) event;
		}
		else {
			applicationEvent = new PayloadApplicationEvent<>(this, event);
			if (eventType == null) {
				eventType = ((PayloadApplicationEvent) applicationEvent).getResolvableType();
			}
		}

		// Multicast right now if possible - or lazily once the multicaster is initialized
		if (this.earlyApplicationEvents != null) {
			this.earlyApplicationEvents.add(applicationEvent);
		}
		else {
		    //广播event事件
			getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
		}
        
        //父类广播
		// Publish event via parent context as well...
		if (this.parent != null) {
			if (this.parent instanceof AbstractApplicationContext) {
				((AbstractApplicationContext) this.parent).publishEvent(event, eventType);
			}
			else {
				this.parent.publishEvent(event);
			}
		}
	}
```